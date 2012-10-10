package com.comphenix.protocol.injector.player;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.server.Packet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.ListenerInvoker;

/**
 * Injection method that overrides the NetworkHandler itself, and it's sendPacket-method.
 * 
 * @author Kristian
 */
class NetworkObjectInjector extends PlayerInjector {
	// Determine if we're listening
	private Set<Integer> sendingFilters;
	
	public NetworkObjectInjector(Logger logger, Player player, ListenerInvoker invoker, Set<Integer> sendingFilters) throws IllegalAccessException {
		super(logger, player, invoker);
		this.sendingFilters = sendingFilters;
	}

	@Override
	protected boolean hasListener(int packetID) {
		return sendingFilters.contains(packetID);
	}
	
	@Override
	public void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException {
		Object networkDelegate = filtered ? networkManagerRef.getValue() : networkManagerRef.getOldValue();
		
		if (networkDelegate != null) {
			try {
				// Note that invocation target exception is a wrapper for a checked exception
				queueMethod.invoke(networkDelegate, packet);
				
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to access queue method.", e);
			}
		} else {
			throw new IllegalStateException("Unable to load network mananager. Cannot send packet.");
		}
	}
	
	@Override
	public void checkListener(PacketListener listener) {
		// Unfortunately, we don't support chunk packets
		if (ListeningWhitelist.containsAny(listener.getSendingWhitelist(), 
				Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK)) {
			throw new IllegalStateException("The NETWORK_FIELD_INJECTOR hook doesn't support map chunk listeners.");
		}
	}
	
	@Override
	public void injectManager() {
		
		if (networkManager != null) {
			final Class<?> networkInterface = networkManagerField.getType();
			final Object networkDelegate = networkManagerRef.getOldValue();
			
			if (!networkInterface.isInterface()) {
				throw new UnsupportedOperationException(
						"Must use CraftBukkit 1.3.0 or later to inject into into NetworkMananger.");
			}
			
			// Create our proxy object
			Object networkProxy = Proxy.newProxyInstance(networkInterface.getClassLoader(), 
					new Class<?>[] { networkInterface }, new InvocationHandler() {
				
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					// OH OH! The queue method!
					if (method.equals(queueMethod)) {
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
						return method.invoke(networkDelegate, args);
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				}
			});
		
			// Inject it, if we can.
			networkManagerRef.setValue(networkProxy);
		}
	}
	
	@Override
	public void cleanupAll() {
		// Clean up
		networkManagerRef.revertValue();
	}

	@Override
	public boolean canInject() {
		return true;
	}
}
