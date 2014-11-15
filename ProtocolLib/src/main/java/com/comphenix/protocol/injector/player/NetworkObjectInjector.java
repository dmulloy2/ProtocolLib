/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.injector.player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.utility.EnhancerFactory;
import com.comphenix.protocol.utility.MinecraftVersion;

/**
 * Injection method that overrides the NetworkHandler itself, and its queue-method.
 * 
 * @author Kristian
 */
public class NetworkObjectInjector extends PlayerInjector {
	// Determine if we're listening
	private IntegerSet sendingFilters;
	
	// After commit 336a4e00668fd2518c41242755ed6b3bdc3b0e6c (Update CraftBukkit to Minecraft 1.4.4.), 
	// CraftBukkit stopped redirecting map chunk and map chunk bulk packets to a separate queue.
	// Thus, NetworkFieldInjector can safely handle every packet (though not perfectly - some packets
	// will be slightly processed).
	private MinecraftVersion safeVersion = new MinecraftVersion("1.4.4");
	
	// Shared callback filter - avoid creating a new class every time
	private volatile static CallbackFilter callbackFilter;
	
	// Temporary player factory
	private static volatile TemporaryPlayerFactory tempPlayerFactory;
	
	/**
	 * Create a new network object injector.
	 * <p>
	 * Note: This class is intended to be internal. Do not use.
	 * @param reporter - the error reporter.
	 * @param player - the player Bukkit entity.
	 * @param invoker - the packet invoker.
	 * @param sendingFilters - list of permitted packet IDs.
	 * @throws IllegalAccessException If reflection failed.
	 */
	public NetworkObjectInjector(ErrorReporter reporter, Player player, 
								 ListenerInvoker invoker, IntegerSet sendingFilters) throws IllegalAccessException {
		
		super(reporter, player, invoker);
		this.sendingFilters = sendingFilters;
	}

	@Override
	protected boolean hasListener(int packetID) {
		return sendingFilters.contains(packetID);
	}
	
	/**
	 * Create a temporary player for use during login.
	 * @param server - Bukkit server.
	 * @return The temporary player.
	 */
	public Player createTemporaryPlayer(Server server) {
		if (tempPlayerFactory == null)
			tempPlayerFactory = new TemporaryPlayerFactory();
		
		// Create and associate the fake player with this network injector
		return tempPlayerFactory.createTemporaryPlayer(server, this);
	}
	
	@Override
	public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) throws InvocationTargetException {
		Object networkDelegate = filtered ? networkManagerRef.getValue() : networkManagerRef.getOldValue();
		
		if (networkDelegate != null) {
			try {
				if (marker != null) {
					queuedMarkers.put(packet, marker);
				}
				
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
	public UnsupportedListener checkListener(MinecraftVersion version, PacketListener listener) {
		if (version != null && version.compareTo(safeVersion) > 0) {
			return null;
			
		} else {
			@SuppressWarnings("deprecation")
			int[] unsupported = { Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK };
			
			// Unfortunately, we don't support chunk packets
			if (ListeningWhitelist.containsAny(listener.getSendingWhitelist(), unsupported)) {
				return new UnsupportedListener("The NETWORK_OBJECT_INJECTOR hook doesn't support map chunk listeners.", unsupported);
			} else {
				return null;
			}
		}
	}
	
	@Override
	public void injectManager() {
		
		if (networkManager != null) {
			final Class<?> networkInterface = networkManagerRef.getField().getType();
			final Object networkDelegate = networkManagerRef.getOldValue();
			
			if (!networkInterface.isInterface()) {
				throw new UnsupportedOperationException(
						"Must use CraftBukkit 1.3.0 or later to inject into into NetworkMananger.");
			}
			
			Callback queueFilter = new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					Object packet = args[0];
					
					if (packet != null) {
						packet = handlePacketSending(packet);
						
						// A NULL packet indicate cancelling
						if (packet != null)
							args[0] = packet;
						else
							return null;
					}
					
					// Delegate to our underlying class
					return proxy.invokeSuper(networkDelegate, args);
				}
			};
			Callback dispatch = new LazyLoader() {
				@Override
				public Object loadObject() throws Exception {
					return networkDelegate;
				}
			};
			
			// Share callback filter - that way, we avoid generating a new class every time.
			if (callbackFilter == null) {
				callbackFilter = new CallbackFilter() {
					@Override
					public int accept(Method method) {
						if (method.equals(queueMethod))
							return 0;
						else
							return 1;
					}
				};
			}
			
			// Create our proxy object
			Enhancer ex = EnhancerFactory.getInstance().createEnhancer();
			ex.setSuperclass(networkInterface);
			ex.setCallbacks(new Callback[] { queueFilter, dispatch });
			ex.setCallbackFilter(callbackFilter);
			
			// Inject it, if we can.
			networkManagerRef.setValue(ex.create());
		}
	}
	
	@Override
	protected void cleanHook() {
		// Clean up
		if (networkManagerRef != null && networkManagerRef.isCurrentSet()) {
			networkManagerRef.revertValue();
		}
	}

	@Override
	public void handleDisconnect() {
		// No need to do anything
	}
	
	@Override
	public boolean canInject(GamePhase phase) {
		// Works for all phases
		return true;
	}

	@Override
	public PlayerInjectHooks getHookType() {
		return PlayerInjectHooks.NETWORK_MANAGER_OBJECT;
	}
}
