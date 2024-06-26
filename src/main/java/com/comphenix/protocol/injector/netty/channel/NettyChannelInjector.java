package com.comphenix.protocol.injector.netty.channel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerManager;
import com.comphenix.protocol.injector.NetworkProcessor;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.AttributeKey;

public class NettyChannelInjector implements Injector {

    private static final String INBOUND_INTERCEPTOR_NAME = "protocol_lib_inbound_interceptor";
    private static final String INBOUND_PROTOCOL_GETTER_NAME = "protocol_lib_inbound_protocol_getter";
    private static final String WIRE_PACKET_ENCODER_NAME = "protocol_lib_wire_packet_encoder";

    // all registered channel handlers to easier make sure we unregister them all from the pipeline
    private static final String[] NETTY_HANDLER_NAMES = new String[]{
            WIRE_PACKET_ENCODER_NAME, INBOUND_INTERCEPTOR_NAME, INBOUND_PROTOCOL_GETTER_NAME
    };

    private static final ReportType REPORT_CANNOT_SEND_PACKET = new ReportType("Unable to send packet %s to %s");
    private static final ReportType REPORT_CANNOT_SEND_WRITE_PACKET = new ReportType("Unable to send wire packet %s to %s");
    private static final ReportType REPORT_CANNOT_READ_PACKET = new ReportType("Unable to read packet %s for %s");
    private static final ReportType REPORT_CANNOT_DISCONNECT = new ReportType("Unable to disconnect %s for %s");

    private static final WirePacketEncoder WIRE_PACKET_ENCODER = new WirePacketEncoder();
    private static final Map<Class<?>, FieldAccessor> PACKET_ACCESSORS = new ConcurrentHashMap<>(16, 0.9f);

    // use random attribute name because they need to be unique and would throw on reload
    private static final AttributeKey<Integer> PROTOCOL_VERSION = AttributeKey.valueOf(getRandomKey());
    private static final AttributeKey<NettyChannelInjector> INJECTOR = AttributeKey.valueOf(getRandomKey());

    static NettyChannelInjector findInjector(Channel channel) {
        return channel.attr(INJECTOR).get();
    }

    private static String getRandomKey() {
        return "ProtocolLib-" + ThreadLocalRandom.current().nextLong();
    }

    // protocol lib stuff we need
    private final ErrorReporter errorReporter;
    private final InjectionFactory injectionFactory;
    private final ListenerManager listenerManager;
    private final NetworkProcessor networkProcessor;
    private final PacketListenerInvoker listenerInvoker;

    // references
    private final Object networkManager;
    private final Channel channel;

    private final FieldAccessor channelField;

    // packet marking
    private final Map<Object, NetworkMarker> savedMarkers = new WeakHashMap<>(16, 0.9f);
    private final Set<Object> skippedPackets = ConcurrentHashMap.newKeySet();
    protected final ThreadLocal<Boolean> processedPackets = ThreadLocal.withInitial(() -> Boolean.FALSE);

    // status of this injector
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private volatile boolean injected = false;

    // information about the player belonging to this injector
    private String playerName;
    private Player player;

    // lazy initialized fields, if we don't need them we don't bother about them
    private volatile InboundProtocolReader inboundProtocolReader;

    public NettyChannelInjector(
            Player player,
            Object networkManager,
            Channel channel,
            ListenerManager listenerManager,
            InjectionFactory injector,
            ErrorReporter errorReporter
    ) {
        // bukkit stuff
        this.player = player;

        // protocol lib stuff
        this.errorReporter = errorReporter;
        this.networkProcessor = new NetworkProcessor(errorReporter);
        this.listenerInvoker = new PacketListenerInvoker(networkManager);

        // references
        this.networkManager = networkManager;
        this.channel = channel;
        this.listenerManager = listenerManager;
        this.injectionFactory = injector;

        // register us into the channel
        this.channel.attr(INJECTOR).set(this);

        // read the channel field from the network manager given to this method
        // we re-read this field every time as plugins/spigot forks might give us different network manager types
        Field channelField = FuzzyReflection.fromObject(networkManager, true).getField(FuzzyFieldContract.newBuilder()
                .typeExact(Channel.class)
                .banModifier(Modifier.STATIC)
                .build());
        this.channelField = Accessors.getFieldAccessor(channelField);

        // hook here into the close future to be 100% sure that this injector gets closed when the channel we wrap gets closed
        // normally we listen to the disconnect event, but there is a very small period of time, between the login and actual
        // join that is not covered by the disconnect event and may lead to unexpected injector states...
        this.channel.closeFuture().addListener(future -> this.close());
    }

    @Override
    public SocketAddress getAddress() {
        return this.channel.remoteAddress();
    }

    @Override
    public int getProtocolVersion() {
        Integer protocolVersion = this.channel.attr(PROTOCOL_VERSION).get();
        return protocolVersion == null ? MinecraftProtocolVersion.getCurrentVersion() : protocolVersion;
    }

    @Override
    public void inject() {
        // ensure we are in the channel's event loop to be "thread-safe" within netty
        // and our own code
        if (!this.channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(this::inject);
            return;
        }

        // don't need to inject if injector or channel got closed
        if (this.closed.get() || !this.channel.isActive()) {
            return;
        }

        // wrap channel inside the NetworkMananger to proxy write calls to our
        // processOutbound method, this way we try to minimize the amount of thread
        // jumps from event loop to minecraft main thread in order to process packets in
        // main thread
        Object networkManangerChannel = this.channelField.get(this.networkManager);
        if (!(networkManangerChannel instanceof NettyChannelProxy)) {
            EventLoop proxyEventLoop = new NettyEventLoopProxy(this.channel.eventLoop(), this);
            Channel proxyChannel = new NettyChannelProxy(this.channel, proxyEventLoop, this);
            this.channelField.set(this.networkManager, proxyChannel);
        }

        ChannelPipeline pipeline = this.channel.pipeline();

        // since 1.20.5 the en-/decoder is renamed to out-/inbound_config when the
        // channel is waiting for the next protocol phase (after terminal packet)
        String encoderName = pipeline.get("outbound_config") != null
                ? "outbound_config"
                : "encoder";
        String decoderName = pipeline.get("inbound_config") != null
                ? "inbound_config"
                : "decoder";

        // try to add wire packet encoder
        if (pipeline.context(WIRE_PACKET_ENCODER_NAME) == null) {
            pipeline.addAfter(encoderName, WIRE_PACKET_ENCODER_NAME, WIRE_PACKET_ENCODER);
        }

        // try to add protocol reader, this is necessary because the en-/decoder will
        // remove or reconfigure the protocol for terminal since 1.20.2+
        if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove() && pipeline.context(INBOUND_PROTOCOL_GETTER_NAME) == null) {
            this.inboundProtocolReader = new InboundProtocolReader(this);
            pipeline.addBefore(decoderName, INBOUND_PROTOCOL_GETTER_NAME, this.inboundProtocolReader);
        }

        // try to add inbound packet interceptor
        if (pipeline.context(INBOUND_INTERCEPTOR_NAME) == null) {
            pipeline.addAfter(decoderName, INBOUND_INTERCEPTOR_NAME, new InboundPacketInterceptor(this));
        }

        // mark injector as injected
        this.injected = true;
    }

    private void uninject() {
        // ensure we are in the channel's event loop to be "thread-safe" within netty
        // and our own code
        if (!this.channel.eventLoop().inEventLoop()) {
            channel.eventLoop().execute(this::uninject);
            return;
        }

        // replace wrapped channel in NetworkManager with original channel
        this.channelField.set(this.networkManager, this.channel);

        // remove all ProtocolLib netty handler
        ChannelPipeline pipeline = this.channel.pipeline();
        for (String handlerName : NETTY_HANDLER_NAMES) {
            if (pipeline.context(handlerName) != null) {
                pipeline.remove(handlerName);
            }
        }

        // mark injector as uninjected
        this.injected = false;
    }

    @Override
    public void close() {
        // ensure that the injector wasn't close before
        if (this.closed.compareAndSet(false, true)) {
            // remove all of our references from the channel
            this.uninject();

            // remove any outgoing references
            this.channel.attr(INJECTOR).remove();

            // cleanup
            this.savedMarkers.clear();
            this.skippedPackets.clear();

            // wipe this injector completely
            this.injectionFactory.invalidate(this.getPlayer(), this.playerName);
        }
    }

    @Override
    public void sendClientboundPacket(Object packet, NetworkMarker marker, boolean filtered) {
        // ignore call if the injector is closed or not injected
        if (this.closed.get() || !this.injected) {
            return;
        }

        // register the packet as filtered if we shouldn't post it to any listener
        if (!filtered) {
            this.skippedPackets.add(packet);
        }

        // save the given packet marker and send the packet
        if (marker != null) {
            this.savedMarkers.put(packet, marker);
        }

        try {
            this.listenerInvoker.send(packet);
        } catch (Exception exception) {
            this.errorReporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_SEND_PACKET)
                    .messageParam(packet, this.playerName)
                    .error(exception)
                    .build());
        }
    }

    @Override
    public void readServerboundPacket(Object packet) {
        // ignore call if the injector is closed or not injected
        if (this.closed.get() || !this.injected) {
            return;
        }
        
        this.ensureInEventLoop(() -> {
            try {
                // try to invoke the method, this should normally not fail
                this.listenerInvoker.read(packet);
            } catch (Exception exception) {
                this.errorReporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_READ_PACKET)
                        .messageParam(packet, this.playerName)
                        .error(exception)
                        .build());
            }
        });
    }

    @Override
    public void sendWirePacket(WirePacket packet) {
        // ignore call if the injector is closed or not injected
        if (this.closed.get() || !this.injected) {
            return;
        }

        this.ensureInEventLoop(() -> {
            try {
                this.channel.writeAndFlush(packet);
            } catch (Exception exception) {
                this.errorReporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_SEND_WRITE_PACKET)
                        .messageParam(packet, this.playerName)
                        .error(exception)
                        .build());
            }
        });
    }

    @Override
    public void disconnect(String message) {
        // ignore call if the injector is closed or not injected
        if (this.closed.get() || !this.injected) {
            return;
        }

        try {
            this.listenerInvoker.disconnect(message);
        } catch (Exception exception) {
            this.errorReporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_DISCONNECT)
                    .messageParam(this.playerName, message)
                    .error(exception)
                    .build());
        }
    }

    @Override
    public Protocol getCurrentProtocol(PacketType.Sender sender) {
        return ChannelProtocolUtil.PROTOCOL_RESOLVER.apply(this.channel, sender);
    }

    @Override
    public Player getPlayer() {
        // if the player was already resolved there is no need to do further lookups
        if (this.player != null) {
            return this.player;
        }

        // check if the name of the player is already known to the injector
        if (this.playerName != null) {
            this.player = Bukkit.getPlayerExact(this.playerName);
            if (this.player != null) {
                this.injectionFactory.cacheInjector(this.player, this);
            }
        }

        // either we resolved it or we didn't...
        return this.player;
    }

    @Override
    public void setPlayer(Player player) {
        this.injectionFactory.invalidate(this.player, this.playerName);

        this.player = player;
        this.playerName = player.getName();

        this.injectionFactory.cacheInjector(player, this);
        this.injectionFactory.cacheInjector(player.getName(), this);
    }

    @Override
    public boolean isConnected() {
        return this.channel.isActive();
    }

    @Override
    public boolean isInjected() {
        return this.injected;
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

    PacketType.Protocol getInboundProtocol() {
        if (this.inboundProtocolReader != null) {
            return this.inboundProtocolReader.getProtocol();
        }
        return getCurrentProtocol(PacketType.Sender.CLIENT);
    }

    /**
     * Returns true if any plugin registed listeners or built-in listeners for the
     * given packet type exist. See {@link #processInbound(ChannelHandlerContext, PacketContainer)}
     * 
     * @param type the packet type
     * @return true if the packet type has listeners; otherwise false
     */
    boolean hasInboundListener(PacketType type) {
        // always return true for types of the built-in listener (see #processInbound)
        return type == PacketType.Handshake.Client.SET_PROTOCOL ||
                type == PacketType.Login.Client.START ||
                // check for plugin registed listener
                this.listenerManager.hasInboundListener(type);
    }

    void processInbound(ChannelHandlerContext ctx, PacketContainer packet) {
        // process set protocol packets for the protocol version
        if (packet.getType() == PacketType.Handshake.Client.SET_PROTOCOL) {
            Integer protocolVersion = packet.getIntegers().readSafely(0);
            if (protocolVersion != null) {
                this.channel.attr(PROTOCOL_VERSION).set(protocolVersion);
            }
        }

        // process login packets for the player name
        if (packet.getType() == PacketType.Login.Client.START) {
            String username;
            if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                // 1.19 replaced the gameprofile with username and uuid field
                username = packet.getStrings().readSafely(0);
            } else {
                WrappedGameProfile profile = packet.getGameProfiles().readSafely(0);
                username = profile != null ? profile.getName() : null;
            }

            if (username != null) {
                this.playerName = username;
                this.injectionFactory.cacheInjector(username, this);
            }
        }

        // invoke plugin listeners
        if (this.listenerManager.hasInboundListener(packet.getType())) {
            this.processInboundInternal(ctx, packet);
        } else {
            ctx.fireChannelRead(packet.getHandle());
        }
    }

    private void processInboundInternal(ChannelHandlerContext ctx, PacketContainer packetContainer) {
        if (this.listenerManager.hasMainThreadListener(packetContainer.getType()) && !Bukkit.isPrimaryThread()) {
            // not on the main thread but we are required to be reschedule the packet on the
            // main thread
            ProtocolLibrary.getScheduler().runTask(() -> this.processInboundInternal(ctx, packetContainer));
            return;
        }

        // create event and invoke listeners
        PacketEvent event = PacketEvent.fromClient(this, packetContainer, this.player);
        this.listenerManager.invokeInboundPacketListeners(event);

        // get packet of event
        Object packet = event.getPacket().getHandle();

        // fire the intercepted packet down the pipeline if it wasn't cancelled and isn't null
        if (!event.isCancelled() && packet != null) {
            this.ensureInEventLoop(ctx.channel().eventLoop(), () -> ctx.fireChannelRead(packet));

            // check if there were any post events added the packet after we fired it down the pipeline
            // we use this way as we don't want to construct a new network manager accidentally
            NetworkMarker marker = NetworkMarker.getNetworkMarker(event);
            if (marker != null) {
                this.networkProcessor.invokePostEvent(event, marker);
            }
        }
    }

    <T> T processOutbound(T action) {
        // try to get packet field accessor or return on failure
        FieldAccessor packetAccessor = this.lookupPacketAccessor(action);
        if (packetAccessor == FieldAccessor.NO_OP_ACCESSOR) {
            return action;
        }

        // get the packet field and ensure that the field is actually present (should always be, just to be sure)
        Object packet = packetAccessor.get(action);
        if (packet == null) {
            return action;
        }

        // filter out all packets which were explicitly send to not be processed by any event
        // pre-checking isEmpty will reduce the need of hashing packets which don't override the
        // hashCode method; this presents calls to the very slow identityHashCode default implementation
        NetworkMarker marker = this.savedMarkers.isEmpty() ? null : this.savedMarkers.remove(packet);
        if (!this.skippedPackets.isEmpty() && this.skippedPackets.remove(packet)) {
            // if a marker was set there might be scheduled packets to execute after the packet send
            // for this to work we need to proxy the input action to provide access to them
            if (marker != null) {
                return this.proxyAction(action, null, marker);
            }

            // nothing special, just no processing
            return action;
        }

        PacketType.Protocol protocol = this.getCurrentProtocol(PacketType.Sender.SERVER);
        if (protocol == Protocol.UNKNOWN) {
            ProtocolLogger.debug("skipping unknown outbound protocol for {0}", packet.getClass());
            return action;
        }
        
        PacketType packetType = PacketRegistry.getPacketType(protocol, packet.getClass());
        if (packetType == null) {
            ProtocolLogger.debug("skipping unknown outbound packet type for {0}", packet.getClass());
            return action;
        }

        // no listener and no marker - no magic :)
        if (!this.listenerManager.hasOutboundListener(packetType) && marker == null && !MinecraftReflection.isBundlePacket(packet.getClass())) {
            return action;
        }

        // ensure that we are on the main thread if we need to
        if (this.listenerManager.hasMainThreadListener(packetType) && !Bukkit.isPrimaryThread()) {
            // not on the main thread but we are required to be - re-schedule the packet on the main thread
            ProtocolLibrary.getScheduler().runTask(() -> this.sendClientboundPacket(packet, null, true));
            return null;
        }

        // create event and invoke listeners
        PacketContainer packetContainer = new PacketContainer(packetType, packet);
        PacketEvent event = PacketEvent.fromServer(this, packetContainer, marker, this.player);
        this.listenerManager.invokeOutboundPacketListeners(event);

        // get packet of event
        Object interceptedPacket = event.getPacket().getHandle();

        // if the event wasn't cancelled by this action we must recheck if the packet changed during the method call
        if (!event.isCancelled() && interceptedPacket != null) {
            // rewrite the packet in the given action if the packet was changed during the event call
            if (interceptedPacket != packet) {
                packetAccessor.set(action, interceptedPacket);
            }

            // this is essential to do this way as a call to getMarker on the event will construct a new marker instance if needed
            // we just want to know here if there is a marker to proceed correctly
            // if the marker is null we can just schedule the action as we don't need to do anything after the packet was sent
            NetworkMarker eventMarker = NetworkMarker.getNetworkMarker(event);
            if (eventMarker == null) {
                return action;
            }

            // we need to wrap the action to call the listeners set in the marker
            return this.proxyAction(action, event, eventMarker);
        }

        // return null if the event was cancelled to schedule a no-op event
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxyAction(T action, PacketEvent event, NetworkMarker marker) {
        // hack - we only know that the given action is either a runnable or callable, but we need to work out which thing
        // it is exactly to proceed correctly here.
        if (action instanceof Runnable) {
            // easier thing to do - just wrap the runnable in a new one
            return (T) (Runnable) () -> {
                // notify the outbound handler that the packets are processed
                this.processedPackets.set(Boolean.TRUE);
                // execute the action & invoke the post event
                ((Runnable) action).run();
                this.networkProcessor.invokePostEvent(event, marker);
            };
        } else if (action instanceof Callable<?>) {
            // okay this is a bit harder now - we need to wrap the action and return the value of it
            return (T) (Callable<Object>) () -> {
                // notify the outbound handler that the packets are processed
                this.processedPackets.set(Boolean.TRUE);
                // execute the action & invoke the post event
                Object value = ((Callable<Object>) action).call();
                this.networkProcessor.invokePostEvent(event, marker);
                return value;
            };
        } else {
            throw new IllegalStateException("Unexpected input action of type " + action.getClass());
        }
    }

    private FieldAccessor lookupPacketAccessor(Object action) {
        return PACKET_ACCESSORS.computeIfAbsent(action.getClass(), key -> {
            try {
                Field packetField = FuzzyReflection.fromClass(key, true).getField(
                        FuzzyFieldContract.newBuilder()
                        .typeSuperOf(MinecraftReflection.getPacketClass())
                        .build());
                return Accessors.getFieldAccessor(packetField);
            } catch (IllegalArgumentException exception) {
                // return noop accessor because computeIfAbsent interprets null as a missing key
                return FieldAccessor.NO_OP_ACCESSOR;
            }
        });
    }

    private void ensureInEventLoop(Runnable runnable) {
        this.ensureInEventLoop(this.channel.eventLoop(), runnable);
    }

    private void ensureInEventLoop(EventLoop eventLoop, Runnable runnable) {
        if (eventLoop.inEventLoop()) {
            runnable.run();
        } else {
            eventLoop.execute(runnable);
        }
    }
}
