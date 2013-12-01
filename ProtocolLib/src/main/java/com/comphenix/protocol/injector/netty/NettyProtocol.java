package com.comphenix.protocol.injector.netty;

import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a way of accessing the new netty Protocol enum.
 * @author Kristian
 */
public class NettyProtocol {
	private Class<?> enumProtocol;
	
	
	
	public NettyProtocol() {
		enumProtocol = MinecraftReflection.getEnumProtocolClass();
		
		
	}
	
	/**
	 * Load the packet lookup tables in each protocol.
	 */
	private void initialize() {
		for (Object protocol : enumProtocol.getEnumConstants()) {
			
		}
	}
}
