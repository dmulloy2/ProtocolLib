package com.comphenix.protocol.injector.netty.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.channel.InjectionFactory;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.Pair;
import io.netty.channel.ChannelFuture;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class NetworkManagerInjector implements ChannelListener {

    private static final String INBOUND_INJECT_HANDLER_NAME = "protocol_lib_inbound_inject";
    private static final TemporaryPlayerFactory PLAYER_FACTORY = new TemporaryPlayerFactory();

    private final PacketTypeSet inboundListeners = new PacketTypeSet();
    private final PacketTypeSet outboundListeners = new PacketTypeSet();
    private final PacketTypeSet mainThreadListeners = new PacketTypeSet();

    // all list fields which we've overridden and need to revert to a non-proxying list afterwards
    private final Set<Pair<Object, FieldAccessor>> overriddenLists = new HashSet<>();

    private final ErrorReporter errorReporter;
    private final ListenerInvoker listenerInvoker;
    private final InjectionFactory injectionFactory;

    // injectors based on this "global" injector
    private final PacketInjector packetInjector;
    private final PlayerInjectionHandler playerInjectionHandler;

    private final InjectionChannelInitializer pipelineInjectorHandler;

    private boolean debug = false;

    // status of this injector
    private boolean closed = false;
    private boolean injected = false;

    public NetworkManagerInjector(Plugin plugin, Server server, ListenerInvoker listenerInvoker, ErrorReporter reporter) {
        this.errorReporter = reporter;
        this.listenerInvoker = listenerInvoker;
        this.injectionFactory = new InjectionFactory(plugin, server, reporter);

        // hooking netty handlers
        InjectionChannelInboundHandler injectionHandler = new InjectionChannelInboundHandler(
                this.injectionFactory,
                this,
                PLAYER_FACTORY);
        this.pipelineInjectorHandler = new InjectionChannelInitializer(INBOUND_INJECT_HANDLER_NAME, injectionHandler);

        // other injectors
        this.playerInjectionHandler = new NetworkManagerPlayerInjector(
                this.outboundListeners,
                this,
                this.injectionFactory,
                this.mainThreadListeners);
        this.packetInjector = new NetworkManagerPacketInjector(
                this.inboundListeners,
                this.listenerInvoker,
                this,
                this.mainThreadListeners);
    }

    @Override
    public PacketEvent onPacketSending(Injector injector, Object packet, NetworkMarker marker) {
        // check if we need to intercept the packet
        Class<?> packetClass = packet.getClass();
        if (marker != null || MinecraftReflection.isBundlePacket(packetClass) || outboundListeners.contains(packetClass)) {
            // wrap packet and construct the event
            PacketType.Protocol currentProtocol = injector.getCurrentProtocol(PacketType.Sender.SERVER);
            PacketContainer container = new PacketContainer(PacketRegistry.getPacketType(currentProtocol, packetClass), packet);
            PacketEvent packetEvent = PacketEvent.fromServer(this, container, marker, injector.getPlayer());

            // post to all listeners, then return the packet event we constructed
            this.listenerInvoker.invokePacketSending(packetEvent);
            return packetEvent;
        }

        // no listener so there is no change we need to apply
        return null;
    }

    @Override
    public PacketEvent onPacketReceiving(Injector injector, Object packet, NetworkMarker marker) {
        // check if we need to intercept the packet
        Class<?> packetClass = packet.getClass();
        if (marker != null || inboundListeners.contains(packetClass)) {
            // wrap the packet and construct the event
            PacketType.Protocol currentProtocol = injector.getCurrentProtocol(PacketType.Sender.CLIENT);
            PacketType packetType = PacketRegistry.getPacketType(currentProtocol, packetClass);

            // if packet type could not be found, fallback to HANDSHAKING protocol
            // temporary workaround for https://github.com/dmulloy2/ProtocolLib/issues/2601
            if (packetType == null) {
                packetType = PacketRegistry.getPacketType(PacketType.Protocol.HANDSHAKING, packetClass);
            }

            PacketContainer container = new PacketContainer(packetType, packet);
            PacketEvent packetEvent = PacketEvent.fromClient(this, container, marker, injector.getPlayer());

            // post to all listeners, then return the packet event we constructed
            this.listenerInvoker.invokePacketReceiving(packetEvent);
            return packetEvent;
        }

        // no listener so there is no change we need to apply
        return null;
    }

    @Override
    public boolean hasListener(Class<?> packetClass) {
        return this.outboundListeners.contains(packetClass) || this.inboundListeners.contains(packetClass);
    }

    @Override
    public boolean hasMainThreadListener(Class<?> packetClass) {
        return this.mainThreadListeners.contains(packetClass);
    }

    @Override
    public boolean hasMainThreadListener(PacketType type) {
        return this.mainThreadListeners.contains(type);
    }

    @Override
    public ErrorReporter getReporter() {
        return this.errorReporter;
    }

    @Override
    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @SuppressWarnings("unchecked")
    public void inject() {
        if (this.closed || this.injected) {
            return;
        }

        // get all "server connections" defined in the minecraft server class
        FuzzyReflection server = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());
        List<Method> serverConnectionGetter = server.getMethodList(FuzzyMethodContract.newBuilder()
                .parameterCount(0)
                .banModifier(Modifier.STATIC)
                .returnTypeExact(MinecraftReflection.getServerConnectionClass())
                .build());

        // get the first available server connection
        Object serverInstance = server.getSingleton();
        Object serverConnection = null;

        for (Method method : serverConnectionGetter) {
            try {
                // use all methods until the first one actually returns a server connection instance
                serverConnection = method.invoke(serverInstance);
                if (serverConnection != null) {
                    break;
                }
            } catch (Exception exception) {
                ProtocolLogger.debug("Exception invoking getter for server connection " + method, exception);
            }
        }

        // check if we got the server connection to use
        if (serverConnection == null) {
            throw new IllegalStateException("Unable to retrieve ServerConnection instance from MinecraftServer");
        }

        FuzzyReflection serverConnectionFuzzy = FuzzyReflection.fromObject(serverConnection, true);
        List<Field> listFields = serverConnectionFuzzy.getFieldList(FuzzyFieldContract.newBuilder()
                .typeDerivedOf(List.class)
                .banModifier(Modifier.STATIC)
                .build());

        // loop over all fields which we need to override and try to do so if needed
        for (Field field : listFields) {
            // ensure that the generic type of the field is actually a channel future, rather than guessing
            // by peeking objects from the list
            if (field.getGenericType().getTypeName().contains(ChannelFuture.class.getName())) {
                // we can only guess if we need to override it, but it looks like we should.
                // we now need the old value of the field to wrap it into a new collection
                FieldAccessor accessor = Accessors.getFieldAccessor(field);
                List<Object> value = (List<Object>) accessor.get(serverConnection);

                // mark down that we've overridden the field
                this.overriddenLists.add(new Pair<>(serverConnection, accessor));

                // we need to synchronize accesses to the list ourselves, see Collections.SynchronizedCollection
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (value) {
                    // override the list field with our list
                    List<Object> newList = new ListeningList(value, this.pipelineInjectorHandler);
                    accessor.set(serverConnection, newList);
                }
            }
        }

        // mark as injected
        this.injected = true;
    }

    public void close() {
        if (this.closed || !this.injected) {
            return;
        }

        // change the state first to prevent further injections / closes
        this.closed = true;
        this.injected = false;

        // undo changes we did to any field
        for (Pair<Object, FieldAccessor> list : this.overriddenLists) {
            // get the value of the field we've overridden, if it is no longer a ListeningList someone probably jumped in
            // and replaced the field himself - we are out safely as the other person needs to clean the mess...
            Object currentFieldValue = list.getSecond().get(list.getFirst());
            if (currentFieldValue instanceof ListeningList) {
                // just reset to the list we wrapped originally
                ListeningList ourList = (ListeningList) currentFieldValue;
                List<Object> original = ourList.getOriginal();
                synchronized (original) {
                    // revert the injection from all values of the list
                    ourList.unProcessAll();
                    list.getSecond().set(list.getFirst(), original);
                }
            }
        }

        // clear up
        this.overriddenLists.clear();
        this.injectionFactory.close();
    }

    public PacketInjector getPacketInjector() {
        return this.packetInjector;
    }

    public PlayerInjectionHandler getPlayerInjectionHandler() {
        return this.playerInjectionHandler;
    }
}
