package com.comphenix.protocol.utility;

import java.lang.reflect.Method;
import java.util.Map;

import com.comphenix.protocol.reflect.FuzzyReflection;

/**
 * Static methods for accessing Minecraft methods.
 * 
 * @author Kristian
 */
public class MinecraftMethods {
	// For player connection
	private volatile static Method sendPacketMethod;
	
	/**
	 * Retrieve the send packet method in PlayerConnection/NetServerHandler.
	 * @return The send packet method.
	 */
	public static Method getSendPacketMethod() {
		if (sendPacketMethod == null) {
			Class<?> serverHandlerClass = MinecraftReflection.getNetServerHandlerClass();
			
			try {
				sendPacketMethod = FuzzyReflection.fromObject(serverHandlerClass).getMethodByName("sendPacket.*");
			} catch (IllegalArgumentException e) {
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
	
}
