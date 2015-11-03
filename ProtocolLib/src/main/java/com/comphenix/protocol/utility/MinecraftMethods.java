package com.comphenix.protocol.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.compat.netty.Netty;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FuzzyReflection;

/**
 * Static methods for accessing Minecraft methods.
 * 
 * @author Kristian
 */
public class MinecraftMethods {
	// For player connection
	private volatile static Method sendPacketMethod;
	
	// For network manager
	private volatile static Method networkManagerHandle;
	private volatile static Method networkManagerPacketRead;
	
	// For packet
	private volatile static Method packetReadByteBuf;
	private volatile static Method packetWriteByteBuf;
	
	/**
	 * Retrieve the send packet method in PlayerConnection/NetServerHandler.
	 * @return The send packet method.
	 */
	public static Method getSendPacketMethod() {
		if (sendPacketMethod == null) {
			Class<?> serverHandlerClass = MinecraftReflection.getPlayerConnectionClass();

			try {
				sendPacketMethod = FuzzyReflection.fromClass(serverHandlerClass).getMethodByName("sendPacket.*");
			} catch (IllegalArgumentException e) {
				// We can't use the method below on Netty
				if (MinecraftReflection.isUsingNetty()) {
					sendPacketMethod = FuzzyReflection.fromClass(serverHandlerClass).
						getMethodByParameters("sendPacket", MinecraftReflection.getPacketClass());
					return sendPacketMethod;
				}
				
				Map<String, Method> netServer = getMethodList(
						serverHandlerClass, MinecraftReflection.getPacketClass());
				Map<String, Method> netHandler = getMethodList(
						MinecraftReflection.getNetHandlerClass(), MinecraftReflection.getPacketClass());
				
				// Remove every method in net handler from net server
				for (String methodName : netHandler.keySet()) {
					netServer.remove(methodName);
				}
				
				// The remainder is the send packet method
				if (netServer.size() ==  1) {
					Method[] methods = netServer.values().toArray(new Method[0]);
					sendPacketMethod = methods[0];
				} else {
					throw new IllegalArgumentException("Unable to find the sendPacket method in NetServerHandler/PlayerConnection.");
				}
			}
		}
		return sendPacketMethod;
	}
	
	/**
	 * Retrieve the disconnect method for a given player connection.
	 * @param playerConnection - the player connection.
	 * @return The
	 */
	public static Method getDisconnectMethod(Class<? extends Object> playerConnection) {
		try {
			return FuzzyReflection.fromClass(playerConnection).getMethodByName("disconnect.*");
		} catch (IllegalArgumentException e) {
			// Just assume it's the first String method
			return FuzzyReflection.fromObject(playerConnection).getMethodByParameters("disconnect", String.class);
		}
	}
	
	/**
	 * Retrieve the handle(Packet, GenericFutureListener[]) method of network manager.
	 * <p>
	 * This only exists in version 1.7.2 and above.
	 * @return The handle method.
	 */
	public static Method getNetworkManagerHandleMethod() {
		if (networkManagerHandle == null) {
			networkManagerHandle = FuzzyReflection.fromClass(MinecraftReflection.getNetworkManagerClass(), true).
					getMethodByParameters("handle", MinecraftReflection.getPacketClass(), Netty.getGenericFutureListenerArray());
			networkManagerHandle.setAccessible(true);
		}
		return networkManagerHandle;
	}
	
	/**
	 * Retrieve the packetRead(ChannelHandlerContext, Packet) method of NetworkMananger.
	 * <p>
	 * This only exists in version 1.7.2 and above.
	 * @return The packetRead method.
	 */
	public static Method getNetworkManagerReadPacketMethod() {
		if (networkManagerPacketRead == null) {
			networkManagerPacketRead = FuzzyReflection.fromClass(MinecraftReflection.getNetworkManagerClass(), true).
					getMethodByParameters("packetRead", Netty.getChannelHandlerContext(), MinecraftReflection.getPacketClass());
			networkManagerPacketRead.setAccessible(true);
		}
		return networkManagerPacketRead;
	}

	/**
	 * Retrieve a method mapped list of every method with the given signature.
	 * @param source - class source.
	 * @param params - parameters.
	 * @return Method mapped list.
	 */
	private static Map<String, Method> getMethodList(Class<?> source, Class<?>... params) {
		FuzzyReflection reflect = FuzzyReflection.fromClass(source, true);
		
		return reflect.getMappedMethods(
			reflect.getMethodListByParameters(Void.TYPE, params)
		);
	}

	/**
	 * Retrieve the Packet.read(PacketDataSerializer) method.
	 * <p>
	 * This only exists in version 1.7.2 and above.
	 * @return The packet read method.
	 */
	public static Method getPacketReadByteBufMethod()  {
		initializePacket();
		return packetReadByteBuf;
	}
	
	/**
	 * Retrieve the Packet.write(PacketDataSerializer) method.
	 * <p>
	 * This only exists in version 1.7.2 and above.
	 * @return The packet write method.
	 */
	public static Method getPacketWriteByteBufMethod()  {
		initializePacket();
		return packetWriteByteBuf;
	}
	
	/**
	 * Initialize the two read() and write() methods.
	 */
	private static void initializePacket() {
		// Initialize the methods
		if (packetReadByteBuf == null || packetWriteByteBuf == null) {
			// This object will allow us to detect which methods were called
			Enhancer enhancer = EnhancerFactory.getInstance().createEnhancer();
			enhancer.setSuperclass(MinecraftReflection.getPacketDataSerializerClass());
			enhancer.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					if (method.getName().contains("read"))
						throw new ReadMethodException();
					if (method.getName().contains("write"))
						throw new WriteMethodException();
					return proxy.invokeSuper(obj, args);
				}
			});

			// Create our proxy object
			Object javaProxy = enhancer.create(
					new Class<?>[] { MinecraftReflection.getByteBufClass() },
					new Object[] { Netty.buffer().getHandle() }
			);

			Object lookPacket = new PacketContainer(PacketType.Play.Client.CLOSE_WINDOW).getHandle();
			List<Method> candidates = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass())
					.getMethodListByParameters(Void.TYPE, new Class<?>[] { MinecraftReflection.getPacketDataSerializerClass() });

			// Look through all the methods
			for (Method method : candidates) {
				try {
					method.invoke(lookPacket, javaProxy);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof ReadMethodException) {
						// Must be the reader
						packetReadByteBuf = method;
					} else if (e.getCause() instanceof WriteMethodException) {
						packetWriteByteBuf = method;
					} else {
						// throw new RuntimeException("Inner exception.", e);
					}
				} catch (Exception e) {
					throw new RuntimeException("Generic reflection error.", e);
				}
			}

			if (packetReadByteBuf == null)
				throw new IllegalStateException("Unable to find Packet.read(PacketDataSerializer)");
			if (packetWriteByteBuf == null)
				throw new IllegalStateException("Unable to find Packet.write(PacketDataSerializer)");
		}
	}
	
	/**
	 * An internal exception used to detect read methods.
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
	 * @author Kristian
	 */
	private static class WriteMethodException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		public WriteMethodException() {
			super("A write method was executed.");
		}
	}
}
