package com.comphenix.protocol.injector.packet;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;

/**
 * A singleton factory for creating incoming packet injectors.
 * 
 * @author Kristian
 */
public class InjectorFactory {
	private static final InjectorFactory INSTANCE = new InjectorFactory();
	
	private InjectorFactory() {
		// No need to construct this
	}
	
	/**
	 * Retrieve the factory singleton.
	 * @return Factory singleton.
	 */
	public static InjectorFactory getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Create a packet injector that intercepts packets by overriding the packet registry.
	 * @param classLoader - current class loader.
	 * @param manager - packet invoker.
	 * @param playerInjection - to lookup Player by DataInputStream.
	 * @param reporter - error reporter.
	 * @return A packet injector with these features.
	 * @throws IllegalAccessException If we fail to create the injector.
	 */
	public PacketInjector createProxyInjector(
				ClassLoader classLoader, ListenerInvoker manager, 
			  	PlayerInjectionHandler playerInjection, ErrorReporter reporter) throws IllegalAccessException {
		
		return new ProxyPacketInjector(classLoader, manager, playerInjection, reporter);
	}
}
