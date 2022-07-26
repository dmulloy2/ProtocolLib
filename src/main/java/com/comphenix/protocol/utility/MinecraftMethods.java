package com.comphenix.protocol.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.Callable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;

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
	private volatile static MethodAccessor sendPacketMethod;
	private volatile static MethodAccessor disconnectMethod;

	// For network manager
	private volatile static MethodAccessor networkManagerHandle;
	private volatile static MethodAccessor networkManagerPacketRead;

	// For packet
	private volatile static MethodAccessor packetReadByteBuf;
	private volatile static MethodAccessor packetWriteByteBuf;

	// Decorated PacketSerializer to identify methods
	private volatile static ConstructorAccessor decoratedDataSerializerAccessor;

	private MinecraftMethods() {
		// sealed
	}

	/**
	 * Retrieve the send packet method in PlayerConnection/NetServerHandler.
	 *
	 * @return The send packet method.
	 */
	public static MethodAccessor getSendPacketMethod() {
		if (sendPacketMethod == null) {
			FuzzyReflection serverHandlerClass = FuzzyReflection.fromClass(MinecraftReflection.getPlayerConnectionClass());

			try {
				sendPacketMethod = Accessors.getMethodAccessor(serverHandlerClass.getMethod(FuzzyMethodContract.newBuilder()
						.parameterCount(1)
						.returnTypeVoid()
						.parameterExactType(MinecraftReflection.getPacketClass(), 0)
						.build()));
			} catch (IllegalArgumentException e) {
				sendPacketMethod = Accessors.getMethodAccessor(serverHandlerClass.getMethod(FuzzyMethodContract.newBuilder()
						.nameRegex("sendPacket.*")
						.returnTypeVoid()
						.parameterCount(1)
						.build()));
			}
		}

		return sendPacketMethod;
	}

	/**
	 * Retrieve the disconnect method for a given player connection.
	 *
	 * @param playerConnection - the player connection.
	 * @return The
	 */
	public static MethodAccessor getDisconnectMethod(Class<?> playerConnection) {
		if (disconnectMethod == null) {
			FuzzyReflection playerConnectionClass = FuzzyReflection.fromClass(playerConnection);
			try {
				disconnectMethod = Accessors.getMethodAccessor(playerConnectionClass.getMethod(FuzzyMethodContract.newBuilder()
						.returnTypeVoid()
						.nameRegex("disconnect.*")
						.parameterCount(1)
						.parameterExactType(String.class, 0)
						.build()));
			} catch (IllegalArgumentException e) {
				// Just assume it's the first String method
				Method disconnect = playerConnectionClass.getMethodByParameters("disconnect", String.class);
				disconnectMethod = Accessors.getMethodAccessor(disconnect);
			}
		}

		return disconnectMethod;
	}

	/**
	 * Retrieve the handle/send packet method of network manager.
	 *
	 * @return The handle method.
	 */
	public static MethodAccessor getNetworkManagerHandleMethod() {
		if (networkManagerHandle == null) {
			Method handleMethod = FuzzyReflection
					.fromClass(MinecraftReflection.getNetworkManagerClass(), true)
					.getMethod(FuzzyMethodContract.newBuilder()
							.banModifier(Modifier.STATIC)
							.returnTypeVoid()
							.parameterCount(1)
							.parameterExactType(MinecraftReflection.getPacketClass(), 0)
							.build());
			networkManagerHandle = Accessors.getMethodAccessor(handleMethod);
		}

		return networkManagerHandle;
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
	 * Retrieve the Packet.read(PacketDataSerializer) method.
	 *
	 * @return The packet read method.
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
						public Object delegate(@SuperCall Callable<?> zuper, @Origin Method method) throws Exception {
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
		// Initialize the methods
		if (packetReadByteBuf == null || packetWriteByteBuf == null) {
			// setups a decorated PacketDataSerializer which we can use to identity read/write methods in the packet class
			if (decoratedDataSerializerAccessor == null) {
				decoratedDataSerializerAccessor = Accessors.getConstructorAccessor(setupProxyConstructor());
			}

			// constructs a new decorated serializer
			Object decoratedSerializer = decoratedDataSerializerAccessor.invoke(Unpooled.EMPTY_BUFFER);

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
