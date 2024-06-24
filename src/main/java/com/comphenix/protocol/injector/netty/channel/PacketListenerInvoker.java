package com.comphenix.protocol.injector.netty.channel;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import io.netty.channel.ChannelHandlerContext;

/**
 * This class facilitates the invocation of methods on the current packet listener.
 * It attempts to execute the <code>send</code>, <code>read</code>, and <code>disconnect</code> 
 * methods and, upon failure (either due to the absence of the method or the packet 
 * listener being of an incorrect type), it delegates the call to the network manager.
 * 
 * <p>Supported packet listener types include CONFIGURATION and PLAY. If the packet
 * listener does not match these types, or if the required method is missing, the 
 * operation falls back to similar methods available in the network manager.
 * 
 * <p>It is important to note that this class does not handle exceptions internally.
 * Instead, it propagates them to the caller. During the initialization phase, the
 * class will throw an exception if the necessary methods in the network manager are
 * not available, ensuring that these dependencies are addressed early in the runtime.
 */
public class PacketListenerInvoker {

    private static final Class<?> PACKET_LISTENER_CLASS = MinecraftReflection.getMinecraftClass("network.PacketListener", "PacketListener");
    private static final Class<?> GAME_PACKET_LISTENER_CLASS = MinecraftReflection.getPlayerConnectionClass();
    private static final Class<?> COMMON_PACKET_LISTENER_CLASS = MinecraftReflection.getNullableNMS("server.network.ServerCommonPacketListenerImpl");
    private static final Class<?> PREFERRED_PACKET_LISTENER_CLASS = COMMON_PACKET_LISTENER_CLASS != null ? COMMON_PACKET_LISTENER_CLASS : GAME_PACKET_LISTENER_CLASS;

    private static final MethodAccessor PACKET_LISTENER_SEND = getPacketListenerSend();
    private static final MethodAccessor PACKET_LISTENER_DISCONNECT = getPacketListenerDisconnect();
    private static final boolean DOES_PACKET_LISTENER_DISCONNECT_USE_COMPONENT = doesPacketListenerDisconnectUseComponent();

    private static final MethodAccessor NETWORK_MANAGER_SEND = getNetworkManagerSend();
    private static final MethodAccessor NETWORK_MANAGER_READ = getNetworkManagerRead();
    private static final MethodAccessor NETWORK_MANAGER_DISCONNECT = getNetworkManagerDisconnect();
    private static final MethodAccessor NETWORK_MANAGER_PACKET_LISTENER = getNetworkManagerPacketListener();

    public static void ensureStaticInitializedWithoutError() {
    }

    private static MethodAccessor getPacketListenerSend() {
        FuzzyReflection packetListener = FuzzyReflection.fromClass(PREFERRED_PACKET_LISTENER_CLASS);

        List<Method> send = packetListener.getMethodList(FuzzyMethodContract.newBuilder()
                .banModifier(Modifier.STATIC)
                .returnTypeVoid()
                .parameterCount(1)
                .parameterExactType(MinecraftReflection.getPacketClass(), 0)
                .build());

        if (send.isEmpty()) {
            ProtocolLogger.debug("Can't get packet listener send method");
            return null;
        }

        return Accessors.getMethodAccessor(send.get(0));
    }

    private static MethodAccessor getPacketListenerDisconnect() {
        FuzzyReflection packetListener = FuzzyReflection.fromClass(PREFERRED_PACKET_LISTENER_CLASS);

        List<Method> disconnect = packetListener.getMethodList(FuzzyMethodContract.newBuilder()
                .banModifier(Modifier.STATIC)
                .returnTypeVoid()
                .parameterCount(1)
                .parameterExactType(MinecraftReflection.getIChatBaseComponentClass(), 0)
                .build());

        if (disconnect.isEmpty()) {
            disconnect = packetListener.getMethodList(FuzzyMethodContract.newBuilder()
                    .banModifier(Modifier.STATIC)
                    .returnTypeVoid()
                    .nameRegex("disconnect.*")
                    .parameterCount(1)
                    .parameterExactType(String.class, 0)
                    .build());
        }

        if (disconnect.isEmpty()) {
            ProtocolLogger.debug("Can't get packet listener disconnect method");
            return null;
        }

        return Accessors.getMethodAccessor(disconnect.get(0));
    }

    private static boolean doesPacketListenerDisconnectUseComponent() {
        if (PACKET_LISTENER_DISCONNECT != null) {
            Parameter reason = PACKET_LISTENER_DISCONNECT.getMethod().getParameters()[0];
            return MinecraftReflection.isIChatBaseComponent(reason.getClass());
        }
        return false;
    }

    private static MethodAccessor getNetworkManagerSend() {
        FuzzyReflection networkManager = FuzzyReflection.fromClass(MinecraftReflection.getNetworkManagerClass());

        Method send = networkManager.getMethod(FuzzyMethodContract.newBuilder()
                .banModifier(Modifier.STATIC)
                .returnTypeVoid()
                .parameterCount(1)
                .parameterExactType(MinecraftReflection.getPacketClass(), 0)
                .build());

        return Accessors.getMethodAccessor(send);
    }

    private static MethodAccessor getNetworkManagerRead() {
        FuzzyReflection networkManager = FuzzyReflection.fromClass(MinecraftReflection.getNetworkManagerClass(), true);

        Method read = networkManager
            .getMethodByParameters("read", ChannelHandlerContext.class, MinecraftReflection.getPacketClass());

        return Accessors.getMethodAccessor(read);
    }

    private static MethodAccessor getNetworkManagerDisconnect() {
        FuzzyReflection networkManager = FuzzyReflection.fromClass(MinecraftReflection.getNetworkManagerClass());

        Method disconnect = networkManager.getMethod(FuzzyMethodContract.newBuilder()
                .banModifier(Modifier.STATIC)
                .returnTypeVoid()
                .parameterCount(1)
                .parameterExactType(MinecraftReflection.getIChatBaseComponentClass(), 0)
                .build());

        return Accessors.getMethodAccessor(disconnect);
    }

    private static MethodAccessor getNetworkManagerPacketListener() {
        FuzzyReflection networkManager = FuzzyReflection.fromClass(MinecraftReflection.getNetworkManagerClass());

        Method packetListener = networkManager.getMethod(FuzzyMethodContract.newBuilder()
                .banModifier(Modifier.STATIC)
                .returnTypeExact(PACKET_LISTENER_CLASS)
                .parameterCount(0)
                .build());

        return Accessors.getMethodAccessor(packetListener);
    }

    private final Object networkManager;
    private final AtomicReference<Object> packetListener = new AtomicReference<>(null);

    PacketListenerInvoker(Object networkManager) {
        if (!MinecraftReflection.is(MinecraftReflection.getNetworkManagerClass(), networkManager)) {
            throw new IllegalArgumentException("Given NetworkManager isn't an isntance of NetworkManager");
        }
        this.networkManager = networkManager;
    }
   
    /**
     * Retrieves the current packet listener associated with this network manager.
     *
     * <p>This method ensures thread-safety and returns the packet listener only if it is an
     * instance of the preferred class. If the packet listener has changed or does not match
     * the preferred class, it returns {@code null}.
     *
     * @return the current packet listener if it meets the required criteria, otherwise {@code null}.
     */
    private Object getPacketListener() {
        // Retrieve the current packet listener from the network manager using reflection.
        Object packetListener = NETWORK_MANAGER_PACKET_LISTENER.invoke(this.networkManager);

        // Perform a thread-safe check to see if the packet listener has changed since the last retrieval.
        if (!this.packetListener.compareAndSet(packetListener, packetListener)) {
            // If the packet listener has changed, attempt to update the cached listener to the new instance,
            // or invalidate the cached object if it does not match the preferred type.
            if (PREFERRED_PACKET_LISTENER_CLASS.isInstance(packetListener)) {
                this.packetListener.set(packetListener);
            } else {
                this.packetListener.set(null);
            }
        }

        // Return the currently cached packet listener, which may be null if it does not match the preferred type.
        return this.packetListener.get();
    }

    /**
     * Sends a packet using the current packet listener if available and valid; otherwise, 
     * falls back to the network manager.
     *
     * @param packet The packet to be sent.
     */
    public void send(Object packet) {
        Object packetListener = this.getPacketListener();
        if (PACKET_LISTENER_SEND != null && packetListener != null) {
            PACKET_LISTENER_SEND.invoke(packetListener, packet);
        } else {
            NETWORK_MANAGER_SEND.invoke(this.networkManager, packet);
        }
    }

    /**
     * Reads a packet directly using the network manager.
     *
     * @param packet The packet to be read.
     */
    public void read(Object packet) {
        NETWORK_MANAGER_READ.invoke(this.networkManager, null, packet);
    }

    /**
     * Disconnects the player using the current packet listener if available and valid; otherwise, 
     * falls back to the network manager.
     *
     * @param reason The reason for the disconnection.
     */
    public void disconnect(String reason) {
        Object packetListener = this.getPacketListener();
        boolean hasPacketListener = PACKET_LISTENER_DISCONNECT != null && packetListener != null;

        Object wrapped = reason;
        if (!hasPacketListener || DOES_PACKET_LISTENER_DISCONNECT_USE_COMPONENT) {
            wrapped = WrappedChatComponent.fromText(reason).getHandle();
        }

        if (hasPacketListener) {
            PACKET_LISTENER_DISCONNECT.invoke(packetListener, wrapped);
        } else {
            NETWORK_MANAGER_DISCONNECT.invoke(this.networkManager, wrapped);
        }
    }
}
