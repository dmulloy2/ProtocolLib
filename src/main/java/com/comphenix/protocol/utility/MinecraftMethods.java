package com.comphenix.protocol.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Static methods for accessing Minecraft methods.
 *
 * @author Kristian
 */
public final class MinecraftMethods {

    // For player connection
    private volatile static MethodAccessor playerConnectionSendMethod;
    private volatile static MethodAccessor playerConnectionDisconnectMethod;

    // For network manager
    private volatile static MethodAccessor networkManagerSend;
    private volatile static MethodAccessor networkManagerPacketRead;
    private volatile static MethodAccessor networkManagerDisconnect;

    // For packet
    private volatile static MethodAccessor packetReadByteBuf;
    private volatile static MethodAccessor packetWriteByteBuf;

    // Decorated PacketSerializer to identify methods
    private volatile static ConstructorAccessor decoratedDataSerializerAccessor;

    private volatile static Function<ByteBuf, Object> friendlyBufBufConstructor;

    private MinecraftMethods() {
        // sealed
    }
    
    public static Function<ByteBuf, Object> getFriendlyBufBufConstructor() {
    	if (friendlyBufBufConstructor == null) {
    		Optional<Class<?>> registryByteBuf = MinecraftReflection.getRegistryFriendlyByteBufClass();
    		
    		if (registryByteBuf.isPresent()) {
    			ConstructorAccessor accessor = Accessors.getConstructorAccessor(FuzzyReflection.fromClass(registryByteBuf.get()).getConstructor(FuzzyMethodContract.newBuilder()
    					.parameterDerivedOf(ByteBuf.class)
    					.parameterDerivedOf(MinecraftReflection.getRegistryAccessClass())
    					.build()));
    			friendlyBufBufConstructor = (byteBuf) -> accessor.invoke(byteBuf, MinecraftRegistryAccess.get());
    		} else {
    			ConstructorAccessor accessor = Accessors.getConstructorAccessor(MinecraftReflection.getPacketDataSerializerClass(), ByteBuf.class);
    			friendlyBufBufConstructor = (byteBuf) -> accessor.invoke(byteBuf);
    		}
    	}
    	return friendlyBufBufConstructor;
    }

    /**
     * Retrieve the send packet method in PlayerConnection/NetServerHandler.
     *
     * @return The send packet method.
     */
    public static MethodAccessor getPlayerConnectionSendMethod() {
        if (playerConnectionSendMethod == null) {
            FuzzyReflection serverHandlerClass = FuzzyReflection.fromClass(MinecraftReflection.getPlayerConnectionClass());

            try {
                playerConnectionSendMethod = Accessors.getMethodAccessor(serverHandlerClass.getMethod(FuzzyMethodContract.newBuilder()
                        .parameterCount(1)
                        .returnTypeVoid()
                        .parameterExactType(MinecraftReflection.getPacketClass(), 0)
                        .build()));
            } catch (IllegalArgumentException e) {
                playerConnectionSendMethod = Accessors.getMethodAccessor(serverHandlerClass.getMethod(FuzzyMethodContract.newBuilder()
                        .nameRegex("sendPacket.*")
                        .returnTypeVoid()
                        .parameterCount(1)
                        .build()));
            }
        }

        return playerConnectionSendMethod;
    }

    /**
     * Retrieve the disconnect method for a given player connection.
     *
     * @return The
     */
    public static MethodAccessor getPlayerConnectionDisconnectMethod() {
        if (playerConnectionDisconnectMethod == null) {
            FuzzyReflection playerConnectionClass = FuzzyReflection.fromClass(MinecraftReflection.getPlayerConnectionClass());

            if (MinecraftVersion.v1_21_0.atOrAbove()) {
                playerConnectionDisconnectMethod = Accessors.getMethodAccessor(playerConnectionClass.getMethod(FuzzyMethodContract.newBuilder()
                        .returnTypeVoid()
                        .parameterCount(1)
                        .parameterExactType(MinecraftReflection.getIChatBaseComponentClass(), 0)
                        .build()));
            } else {
                try {
                    playerConnectionDisconnectMethod = Accessors.getMethodAccessor(playerConnectionClass.getMethod(FuzzyMethodContract.newBuilder()
                            .returnTypeVoid()
                            .nameRegex("disconnect.*")
                            .parameterCount(1)
                            .parameterExactType(String.class, 0)
                            .build()));
                } catch (IllegalArgumentException e) {
                    // Just assume it's the first String method
                    Method disconnect = playerConnectionClass.getMethodByParameters("disconnect", String.class);
                    playerConnectionDisconnectMethod = Accessors.getMethodAccessor(disconnect);
                }
            }
        }

        return playerConnectionDisconnectMethod;
    }

    /**
     * Retrieve the handle/send packet method of network manager.
     *
     * @return The handle method.
     */
    public static MethodAccessor getNetworkManagerSendMethod() {
        if (networkManagerSend == null) {
            Method handleMethod = FuzzyReflection
                    .fromClass(MinecraftReflection.getNetworkManagerClass(), true)
                    .getMethod(FuzzyMethodContract.newBuilder()
                            .banModifier(Modifier.STATIC)
                            .returnTypeVoid()
                            .parameterCount(1)
                            .parameterExactType(MinecraftReflection.getPacketClass(), 0)
                            .build());
            networkManagerSend = Accessors.getMethodAccessor(handleMethod);
        }

        return networkManagerSend;
    }

    /**
     * Retrieve the packetRead(ChannelHandlerContext, Packet) method of NetworkManager.
     *
     * @return The packetRead method.
     */
    public static MethodAccessor getNetworkManagerReadPacketMethod() {
        if (networkManagerPacketRead == null) {
            Method messageReceived = FuzzyReflection
                    .fromClass(MinecraftReflection.getNetworkManagerClass(), true)
                    .getMethodByParameters("packetRead", ChannelHandlerContext.class, MinecraftReflection.getPacketClass());
            networkManagerPacketRead = Accessors.getMethodAccessor(messageReceived);
        }

        return networkManagerPacketRead;
    }

    /**
     * Retrieve the handle/send packet method of network manager.
     *
     * @return The handle method.
     */
    public static MethodAccessor getNetworkManagerDisconnectMethod() {
        if (networkManagerDisconnect == null) {
            Method handleMethod = FuzzyReflection
                    .fromClass(MinecraftReflection.getNetworkManagerClass(), true)
                    .getMethod(FuzzyMethodContract.newBuilder()
                            .banModifier(Modifier.STATIC)
                            .returnTypeVoid()
                            .parameterCount(1)
                            .parameterExactType(MinecraftReflection.getIChatBaseComponentClass(), 0)
                            .build());
            networkManagerDisconnect = Accessors.getMethodAccessor(handleMethod);
        }

        return networkManagerDisconnect;
    }

    /**
     * Retrieve the Packet.read(PacketDataSerializer) method.
     *
     * @return The packet read method.
     * @deprecated no longer works since 1.20.5
     */
    public static MethodAccessor getPacketReadByteBufMethod() {
        initializePacket();
        return packetReadByteBuf;
    }

    /**
     * Retrieve the Packet.write(PacketDataSerializer) method.
     * <p>
     * This only exists in version 1.7.2 and above.
     *
     * @return The packet write method.
     * @deprecated no longer works since 1.20.5
     */
    public static MethodAccessor getPacketWriteByteBufMethod() {
        initializePacket();
        return packetWriteByteBuf;
    }

    private static Constructor<?> setupProxyConstructor() {
        try {
            return ByteBuddyFactory.getInstance()
                    .createSubclass(MinecraftReflection.getPacketDataSerializerClass())
                    .name(MinecraftMethods.class.getPackage().getName() + ".PacketDecorator")
                    .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.to(new Object() {
                        @RuntimeType
                        public Object delegate(@SuperCall(nullIfImpossible = true) Callable<?> zuper, @Origin Method method) throws Exception {
                            if (method.getName().contains("read")) {
                                throw new ReadMethodException();
                            }

                            if (method.getName().contains("write")) {
                                throw new WriteMethodException();
                            }
                            return zuper.call();
                        }
                    }))
                    .make()
                    .load(ByteBuddyFactory.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor(MinecraftReflection.getByteBufClass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find constructor!", e);
        }
    }

    /**
     * Initialize the two read() and write() methods.
     */
    private static void initializePacket() {
    	// write and read methods are no longer part of the packet interface since 1.20.5
        if (MinecraftVersion.v1_20_5.atOrAbove()) {
        	throw new IllegalStateException("can't access packet read/write method after 1.20.5");
        }

        // Initialize the methods
        if (packetReadByteBuf == null || packetWriteByteBuf == null) {
            // setups a decorated PacketDataSerializer which we can use to identity read/write methods in the packet class
            if (decoratedDataSerializerAccessor == null) {
                decoratedDataSerializerAccessor = Accessors.getConstructorAccessor(setupProxyConstructor());
            }

            // constructs a new decorated serializer
            Object serializerBacking = decoratedDataSerializerAccessor.invoke(Unpooled.EMPTY_BUFFER);
            Object decoratedSerializer = decoratedDataSerializerAccessor.invoke(serializerBacking);

            // find all methods which might be the read or write methods
            List<Method> candidates = FuzzyReflection
                    .fromClass(MinecraftReflection.getPacketClass())
                    .getMethodListByParameters(Void.TYPE, MinecraftReflection.getPacketDataSerializerClass());
            // a constructed, empty packet on which we can call the methods
            Object dummyPacket = new PacketContainer(PacketType.Play.Client.CLOSE_WINDOW).getHandle();

            for (Method candidate : candidates) {
                // invoke the method and see if it's a write or read method
                try {
                    candidate.invoke(dummyPacket, decoratedSerializer);
                } catch (InvocationTargetException exception) {
                    // check for the cause of the exception
                    if (exception.getCause() instanceof ReadMethodException) {
                        // must the read method
                        packetReadByteBuf = Accessors.getMethodAccessor(candidate);
                    } else if (exception.getCause() instanceof WriteMethodException) {
                        // must be the write method
                        packetWriteByteBuf = Accessors.getMethodAccessor(candidate);
                    }
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException("Unable to invoke " + candidate, exception);
                }
            }

            // write must be there, read is gone since 1.18 (handled via constructor)
            if (packetWriteByteBuf == null) {
                throw new IllegalStateException("Unable to find Packet.write(PacketDataSerializer)");
            }
        }
    }

    /**
     * An internal exception used to detect read methods.
     *
     * @author Kristian
     */
    private static class ReadMethodException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ReadMethodException() {
            super("A read method was executed.");
        }
    }

    /**
     * An internal exception used to detect write methods.
     *
     * @author Kristian
     */
    private static class WriteMethodException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public WriteMethodException() {
            super("A write method was executed.");
        }
    }
}
