package com.comphenix.protocol.injector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.bukkit.entity.Player;

import com.comphenix.protocol.reflect.FuzzyReflection;

/**
 * Represents a player hook into the NetServerHandler class. 
 * 
 * @author Kristian
 */
public class NetworkServerInjector extends PlayerInjector {

	private static Method sendPacket;
	
	public NetworkServerInjector(Player player, PacketFilterManager manager, Set<Integer> sendingFilters) throws IllegalAccessException {
		super(player, manager, sendingFilters);
	}
	
	@Override
	protected void initialize() throws IllegalAccessException {
		super.initialize();
		
		// Get the send packet method!
		if (hasInitialized) {
			if (sendPacket == null)
				sendPacket = FuzzyReflection.fromObject(serverHandler).getMethodByParameters("sendPacket", Packet.class);
		}
	}

	@Override
	public void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException {
		Object serverDeleage = filtered ? serverHandlerRef.getValue() : serverHandlerRef.getOldValue();
		
		if (serverDeleage != null) {
			try {
				// Note that invocation target exception is a wrapper for a checked exception
				sendPacket.invoke(serverDeleage, packet);
				
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to access send packet method.", e);
			}
		} else {
			throw new IllegalStateException("Unable to load server handler. Cannot send packet.");
		}
	}

	@Override
	public void injectManager() {
		if (serverHandlerRef == null)
			throw new IllegalStateException("Cannot find server handler.");
		// Don't inject twice
		if (serverHandlerRef.getValue() instanceof Factory)
			return;
		
		Enhancer ex = new Enhancer();
		ex.setClassLoader(manager.getClassLoader());
		ex.setSuperclass(serverHandler.getClass());
		ex.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				// The send packet method!
				if (method.equals(sendPacket)) {
					Packet packet = (Packet) args[0];
					
					if (packet != null) {
						packet = handlePacketRecieved(packet);
						
						// A NULL packet indicate cancelling
						if (packet != null)
							args[0] = packet;
						else
							return null;
					}
				}
				
				// Delegate to our underlying class
				try {
					return method.invoke(serverHandler, args);
				} catch (InvocationTargetException e) {
					throw e.getCause();
				}
			}
		});
		
		// Inject it now
		serverHandlerRef.setValue(ex.create());
	}

	@Override
	public void cleanupAll() {
		if (serverHandlerRef != null)
			serverHandlerRef.revertValue();
	}

	@Override
	public boolean canInject() {
		// Probably always
		return true;
	}
}
