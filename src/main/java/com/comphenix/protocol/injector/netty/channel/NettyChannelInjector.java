package com.comphenix.protocol.injector.netty.channel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.NetworkProcessor;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.ByteBuddyGenerated;
import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.AttributeKey;

public class NettyChannelInjector implements Injector {

    // an accessor used when we're unable to retrieve the actual packet field in an outbound packet send
    private static final FieldAccessor NO_OP_ACCESSOR = new FieldAccessor() {
        @Override
        public Object get(Object instance) {
            return null;
        }

        @Override
        public void set(Object instance, Object value) {
        }

        @Override
        public Field getField() {
            return null;
        }
    };

    private static final String INTERCEPTOR_NAME = "protocol_lib_inbound_interceptor";
    private static final String PROTOCOL_READER_NAME = "protocol_lib_protocol_reader";
    private static final String WIRE_PACKET_ENCODER_NAME = "protocol_lib_wire_packet_encoder";

    // all registered channel handlers to easier make sure we unregister them all from the pipeline
    private static final String[] PROTOCOL_LIB_HANDLERS = new String[]{
            WIRE_PACKET_ENCODER_NAME, INTERCEPTOR_NAME, PROTOCOL_READER_NAME
    };

    private static final ReportType REPORT_CANNOT_SEND_PACKET = new ReportType("Unable to send packet %s to %s");

    private static final WirePacketEncoder WIRE_PACKET_ENCODER = new WirePacketEncoder();
    private static final Map<Class<?>, FieldAccessor> PACKET_ACCESSORS = new ConcurrentHashMap<>(16, 0.9f);

    private static final Class<?> LOGIN_PACKET_START_CLASS = PacketType.Login.Client.START.getPacketClass();
    private static final Class<?> PACKET_PROTOCOL_CLASS = PacketType.Handshake.Client.SET_PROTOCOL.getPacketClass();

    private static final AttributeKey<Integer> PROTOCOL_VERSION = AttributeKey.valueOf(getRandomKey());
    private static final AttributeKey<NettyChannelInjector> INJECTOR = AttributeKey.valueOf(getRandomKey());

    // lazy initialized fields, if we don't need them we don't bother about them
    private static FieldAccessor LOGIN_PROFILE_ACCESSOR;
    private static FieldAccessor PROTOCOL_VERSION_ACCESSOR;

    // bukkit stuff
    private final Server server;

    // protocol lib stuff we need
    private final ErrorReporter errorReporter;
    private final NetworkProcessor networkProcessor;

    // references
    private final Object networkManager;
    private final Channel wrappedChannel;
    private final ChannelListener channelListener;
    private final InjectionFactory injectionFactory;

    private final FieldAccessor channelField;

    // packet marking
    private final Map<Object, NetworkMarker> savedMarkers = new WeakHashMap<>(16, 0.9f);
    private final Set<Object> skippedPackets = ConcurrentHashMap.newKeySet();
    protected final ThreadLocal<Boolean> processedPackets = ThreadLocal.withInitial(() -> Boolean.FALSE);

    // status of this injector
    private volatile boolean closed = false;
    private volatile boolean injected = false;

    // information about the player belonging to this injector
    private String playerName;
    private Player resolvedPlayer;

    // lazy initialized fields, if we don't need them we don't bother about them
    private Object playerConnection;
    
    private InboundProtocolReader inboundProtocolReader;

    public NettyChannelInjector(
            Player player,
            Server server,
            Object netManager,
            Channel channel,
            ChannelListener listener,
            InjectionFactory injector,
            ErrorReporter errorReporter
    ) {
        // bukkit stuff
        this.server = server;
        this.resolvedPlayer = player;

        // protocol lib stuff
        this.errorReporter = errorReporter;
        this.networkProcessor = new NetworkProcessor(errorReporter);

        // references
        this.networkManager = netManager;
        this.wrappedChannel = channel;
        this.channelListener = listener;
        this.injectionFactory = injector;

        // register us into the channel
        this.wrappedChannel.attr(INJECTOR).set(this);

        // read the channel field from the network manager given to this method
        // we re-read this field every time as plugins/spigot forks might give us different network manager types
        Field channelField = FuzzyReflection.fromObject(netManager, true).getField(FuzzyFieldContract.newBuilder()
                .typeExact(Channel.class)
                .banModifier(Modifier.STATIC)
                .build());
        this.channelField = Accessors.getFieldAccessor(channelField);

        // hook here into the close future to be 100% sure that this injector gets closed when the channel we wrap gets closed
        // normally we listen to the disconnect event, but there is a very small period of time, between the login and actual
        // join that is not covered by the disconnect event and may lead to unexpected injector states...
        this.wrappedChannel.closeFuture().addListener(future -> this.close());
    }

    static NettyChannelInjector findInjector(Channel channel) {
        return channel.attr(INJECTOR).get();
    }

    static Object findChannelHandler(Channel channel, Class<?> type) {
        for (Entry<String, ChannelHandler> entry : channel.pipeline()) {
            if (type.isAssignableFrom(entry.getValue().getClass())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String getRandomKey() {
        return "ProtocolLib-" + ThreadLocalRandom.current().nextLong();
    }

    private static boolean hasProtocolLibHandler(Channel channel) {
        for (String handler : PROTOCOL_LIB_HANDLERS) {
            if (channel.pipeline().get(handler) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getProtocolVersion() {
        Integer protocolVersion = this.wrappedChannel.attr(PROTOCOL_VERSION).get();
        return protocolVersion == null ? MinecraftProtocolVersion.getCurrentVersion() : protocolVersion;
    }

    @Override
    public boolean inject() {
        // we only do this on the channel event loop to prevent blocking the main server thread
        // and to be sure that the netty pipeline view we get is up-to-date
        if (this.wrappedChannel.eventLoop().inEventLoop()) {
            // ensure that we should actually inject into the channel
            if (this.closed || this.wrappedChannel instanceof ByteBuddyGenerated || !this.wrappedChannel.isActive()) {
                return false;
            }

            // check here if we need to rewrite the channel field and do so
            // minecraft overrides the channel field when the channel actually becomes active, so we need to ensure that our
            // proxied channel is always on that field - therefore this rewrite is event before we check if we're already
            // injected into the channel
            this.rewriteChannelField();

            // check if we already injected into the channel
            if (hasProtocolLibHandler(this.wrappedChannel)) {
                return false;
            }

            ChannelPipeline pipeline = this.wrappedChannel.pipeline();

            // since 1.20.5 the encoder is renamed to outbound_config only in the handshake phase
            String encoderName = pipeline.get("outbound_config") != null
            		? "outbound_config" : "encoder";

            // inject our handlers
            pipeline.addAfter(
                    encoderName,
                    WIRE_PACKET_ENCODER_NAME,
                    WIRE_PACKET_ENCODER);
            if (MinecraftVersion.v1_20_5.atOrAbove()) {
            	this.inboundProtocolReader = new InboundProtocolReader(this);
                pipeline.addBefore(
                		"decoder",
                		PROTOCOL_READER_NAME,
                		this.inboundProtocolReader);
            }
            pipeline.addAfter(
                    "decoder",
                    INTERCEPTOR_NAME,
                    new InboundPacketInterceptor(this, this.channelListener));

            this.injected = true;
            return true;
        } else {
            // re-run in event loop, return false as we cannot be sure if the injection actually worked
            this.ensureInEventLoop(this::inject);
            return false;
        }
    }

    @Override
    public void uninject() {
        // ensure that we injected into the channel before trying to remove anything from it
        if (this.injected) {
            // uninject on the event loop to ensure the instant visibility of the change and prevent blocks of other threads
            if (this.wrappedChannel.eventLoop().inEventLoop()) {
                this.injected = false;

                // remove known references to us
                this.wrappedChannel.attr(INJECTOR).remove();
                this.channelField.set(this.networkManager, this.wrappedChannel);

                for (String handler : PROTOCOL_LIB_HANDLERS) {
                    try {
                        this.wrappedChannel.pipeline().remove(handler);
                    } catch (NoSuchElementException ignored) {
                        // ignore that one, probably an edge case
                    }
                }
            } else {
                this.ensureInEventLoop(this::uninject);
            }
        }
    }

    @Override
    public void close() {
        // ensure that the injector wasn't close before
        if (!this.closed) {
            this.closed = true;

            // remove all of our references from the channel
            this.uninject();

            // cleanup
            this.savedMarkers.clear();
            this.skippedPackets.clear();

            // wipe this injector completely
            this.injectionFactory.invalidate(this.getPlayer(), this.playerName);
        }
    }

    @Override
    public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) {
        // do not send the packet if this injector was already closed / is not injected yet
        if (this.closed || !this.injected) {
            return;
        }

        // register the packet as filtered if we shouldn't post it to any listener
        if (!filtered) {
            this.skippedPackets.add(packet);
        }

        // save the given packet marker and send the packet
        this.saveMarker(packet, marker);
        try {
            if (this.resolvedPlayer instanceof ByteBuddyGenerated) {
                MinecraftMethods.getNetworkManagerHandleMethod().invoke(this.networkManager, packet);
            } else {
                // ensure that the player is properly connected before sending
                Object playerConnection = this.getPlayerConnection();
                if (playerConnection != null) {
                    MinecraftMethods.getSendPacketMethod().invoke(playerConnection, packet);
                }
            }
        } catch (Exception exception) {
            this.errorReporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_SEND_PACKET)
                    .messageParam(packet, this.playerName)
                    .error(exception)
                    .build());
        }
    }

    @Override
    public void receiveClientPacket(Object packet) {
        // do not do that if we're not injected or this injector was closed
        if (this.closed || !this.injected) {
            return;
        }

        Runnable receiveAction = () -> {
            try {
                // try to invoke the method, this should normally not fail
                MinecraftMethods.getNetworkManagerReadPacketMethod().invoke(this.networkManager, null, packet);
            } catch (Exception exception) {
                // 99% the user gave wrong information to the server
                this.errorReporter.reportMinimal(this.injectionFactory.getPlugin(), "receiveClientPacket", exception);
            }
        };

        // execute the action on the event loop rather than any thread which we should potentially not block
        if (this.wrappedChannel.eventLoop().inEventLoop()) {
            receiveAction.run();
        } else {
            this.ensureInEventLoop(receiveAction);
        }
    }
    
    public PacketType.Protocol getInboundProtocol() {
    	if (this.inboundProtocolReader != null) {
        	return this.inboundProtocolReader.getProtocol();
    	}
    	return getCurrentProtocol(PacketType.Sender.CLIENT);
    }

    @Override
    public Protocol getCurrentProtocol(PacketType.Sender sender) {
        return ChannelProtocolUtil.PROTOCOL_RESOLVER.apply(this.wrappedChannel, sender);
    }

    @Override
    public NetworkMarker getMarker(Object packet) {
        return this.savedMarkers.get(packet);
    }

    @Override
    public void saveMarker(Object packet, NetworkMarker marker) {
        if (marker != null && !this.closed) {
            this.savedMarkers.put(packet, marker);
        }
    }

    @Override
    public Player getPlayer() {
        // if the player was already resolved there is no need to do further lookups
        if (this.resolvedPlayer != null) {
            return this.resolvedPlayer;
        }

        // check if the name of the player is already known to the injector
        if (this.playerName != null) {
            this.resolvedPlayer = this.server.getPlayerExact(this.playerName);
        }

        // either we resolved it or we didn't...
        return this.resolvedPlayer;
    }

    @Override
    public void setPlayer(Player player) {
        this.resolvedPlayer = player;
        this.playerName = player.getName();
    }

    @Override
    public void disconnect(String message) {
        // we're still during pre-login, just close the connection
        if (this.playerConnection == null || this.resolvedPlayer instanceof ByteBuddyGenerated) {
            this.wrappedChannel.disconnect();
        } else {
            try {
                // try to call the disconnect method on the player
                MinecraftMethods.getDisconnectMethod(this.playerConnection.getClass()).invoke(this.playerConnection, message);
            } catch (Exception exception) {
                throw new IllegalArgumentException("Unable to disconnect the current injector", exception);
            }
        }
    }

    @Override
    public boolean isInjected() {
        return this.injected;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    void tryProcessLogin(Object packet) {
        // check if the given packet is a login packet
        if (LOGIN_PACKET_START_CLASS != null && LOGIN_PACKET_START_CLASS.equals(packet.getClass())) {
            if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                // 1.19 removed the profile from the packet and now sends the plain username directly
                // ensure that the game profile accessor is available
                if (LOGIN_PROFILE_ACCESSOR == null) {
                    LOGIN_PROFILE_ACCESSOR = Accessors.getFieldAccessor(LOGIN_PACKET_START_CLASS, String.class, true);
                }

                // get the username from the packet
                String username = (String) LOGIN_PROFILE_ACCESSOR.get(packet);

                // cache the injector and the player name
                this.playerName = username;
                this.injectionFactory.cacheInjector(username, this);
            } else {
                // ensure that the game profile accessor is available
                if (LOGIN_PROFILE_ACCESSOR == null) {
                    LOGIN_PROFILE_ACCESSOR = Accessors.getFieldAccessor(
                            LOGIN_PACKET_START_CLASS,
                            MinecraftReflection.getGameProfileClass(),
                            true);
                }

                // the client only sends the name but the server wraps it into a GameProfile, so here we are
                WrappedGameProfile profile = WrappedGameProfile.fromHandle(LOGIN_PROFILE_ACCESSOR.get(packet));

                // cache the injector and the player name
                this.playerName = profile.getName();
                this.injectionFactory.cacheInjector(profile.getName(), this);
            }

            return;
        }

        // protocol version begin
        if (PACKET_PROTOCOL_CLASS != null && PACKET_PROTOCOL_CLASS.equals(packet.getClass())) {
            // ensure the protocol version accessor is available
            if (PROTOCOL_VERSION_ACCESSOR == null) {
                try {
                    Field ver = FuzzyReflection.fromClass(PACKET_PROTOCOL_CLASS, true).getField(FuzzyFieldContract.newBuilder()
                            .banModifier(Modifier.STATIC)
                            .typeExact(int.class)
                            .build());
                    PROTOCOL_VERSION_ACCESSOR = Accessors.getFieldAccessor(ver);
                } catch (IllegalArgumentException exception) {
                    // unable to resolve that field, continue no-op
                    PROTOCOL_VERSION_ACCESSOR = NO_OP_ACCESSOR;
                }
            }

            // read the protocol version from the field if available
            if (PROTOCOL_VERSION_ACCESSOR != NO_OP_ACCESSOR) {
                int protocolVersion = (int) PROTOCOL_VERSION_ACCESSOR.get(packet);
                this.wrappedChannel.attr(PROTOCOL_VERSION).set(protocolVersion);
            }
        }
    }

    private void rewriteChannelField() {
        // check if we need to rewrite the channel or if the channel is already correct (prevent wrapping a wrapped channel)
        Object currentChannel = this.channelField.get(this.networkManager);
        if (currentChannel instanceof NettyChannelProxy) {
            return;
        }

        // the field is not correct, rewrite now to our handler
        Channel ch = new NettyChannelProxy(this.wrappedChannel, new NettyEventLoopProxy(this.wrappedChannel.eventLoop(), this) {
            @Override
            protected Runnable doProxyRunnable(Runnable original) {
                return NettyChannelInjector.this.processOutbound(original);
            }

            @Override
            protected <T> Callable<T> doProxyCallable(Callable<T> original) {
                return NettyChannelInjector.this.processOutbound(original);
            }
        }, this);
        this.channelField.set(this.networkManager, ch);
    }

    private void ensureInEventLoop(Runnable runnable) {
        this.ensureInEventLoop(this.wrappedChannel.eventLoop(), runnable);
    }

    private void ensureInEventLoop(EventLoop eventLoop, Runnable runnable) {
        if (eventLoop.inEventLoop()) {
            runnable.run();
        } else {
            eventLoop.execute(runnable);
        }
    }

    void processInboundPacket(ChannelHandlerContext ctx, Object packet, PacketType packetType) {
        if (this.channelListener.hasMainThreadListener(packetType) && !this.server.isPrimaryThread()) {
            // not on the main thread but we are required to be - re-schedule the packet on the main thread
            ProtocolLibrary.getScheduler().runTask(
                    () -> this.processInboundPacket(ctx, packet, packetType));
            return;
        }

        // call packet handlers, a null result indicates that we shouldn't change anything
        PacketContainer packetContainer = new PacketContainer(packetType, packet);
        PacketEvent interceptionResult = this.channelListener.onPacketReceiving(this, packetContainer, null);
        if (interceptionResult == null) {
            this.ensureInEventLoop(ctx.channel().eventLoop(), () -> ctx.fireChannelRead(packet));
            return;
        }

        // fire the intercepted packet down the pipeline if it wasn't cancelled
        if (!interceptionResult.isCancelled()) {
            this.ensureInEventLoop(
                    ctx.channel().eventLoop(),
                    () -> ctx.fireChannelRead(interceptionResult.getPacket().getHandle()));

            // check if there were any post events added the packet after we fired it down the pipeline
            // we use this way as we don't want to construct a new network manager accidentally
            NetworkMarker marker = NetworkMarker.getNetworkMarker(interceptionResult);
            if (marker != null) {
                this.networkProcessor.invokePostEvent(interceptionResult, marker);
            }
        }
    }

    <T> T processOutbound(T action) {
        // get the accessor to the packet field
        // if we are unable to look up the accessor then just return the runnable, probably nothing of our business
        FieldAccessor packetAccessor = this.lookupPacketAccessor(action);
        if (packetAccessor == NO_OP_ACCESSOR) {
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
		PacketType packetType = PacketRegistry.getPacketType(protocol, packet.getClass());
		
		// TODO: ignore packet or throw error?
		if (packetType == null) {
			return action;
		}

        // no listener and no marker - no magic :)
        if (!this.channelListener.hasOutboundListener(packetType) && marker == null && !MinecraftReflection.isBundlePacket(packet.getClass())) {
            return action;
        }

        // ensure that we are on the main thread if we need to
        if (this.channelListener.hasMainThreadListener(packetType) && !this.server.isPrimaryThread()) {
            // not on the main thread but we are required to be - re-schedule the packet on the main thread
            ProtocolLibrary.getScheduler().runTask(
                    () -> this.sendServerPacket(packet, null, true));
            return null;
        }

        // call all listeners which are listening to the outbound packet, if any
        // null indicates that no listener was affected by the packet, meaning that we can directly send the original packet
        PacketContainer packetContainer = new PacketContainer(packetType, packet);
        PacketEvent event = this.channelListener.onPacketSending(this, packetContainer, marker);
        if (event == null) {
            return action;
        }

        // if the event wasn't cancelled by this action we must recheck if the packet changed during the method call
        if (!event.isCancelled()) {
            // rewrite the packet in the given action if the packet was changed during the event call
            Object interceptedPacket = event.getPacket().getHandle();
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
        return PACKET_ACCESSORS.computeIfAbsent(action.getClass(), clazz -> {
            try {
                // find the field
                Field packetField = FuzzyReflection.fromClass(clazz, true).getField(FuzzyFieldContract
                        .newBuilder()
                        .typeSuperOf(MinecraftReflection.getPacketClass())
                        .build());
                return Accessors.getFieldAccessor(packetField);
            } catch (IllegalArgumentException exception) {
                // no such field found :(
                return NO_OP_ACCESSOR;
            }
        });
    }

    private Object getPlayerConnection() {
        // resolve the player connection if needed
        if (this.playerConnection == null) {
            Player target = this.getPlayer();
            if (target == null) {
                return null;
            }

            this.playerConnection = MinecraftFields.getPlayerConnection(target);
        }

        // cannot be null at this point
        return this.playerConnection;
    }

    public Channel getWrappedChannel() {
        return this.wrappedChannel;
    }
}
