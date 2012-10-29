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

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;

/**
 * Injection method that overrides the NetworkHandler itself, and it's sendPacket-method.
 * 
 * @author Kristian
 */
class NetworkObjectInjector extends PlayerInjector {
	// Determine if we're listening
	private IntegerSet sendingFilters;
	
	// Used to construct proxy objects
	private ClassLoader classLoader;

	// Shared callback filter - avoid creating a new class every time
	private static CallbackFilter callbackFilter;
	
	public NetworkObjectInjector(ClassLoader classLoader, ErrorReporter reporter, Player player, 
								 ListenerInvoker invoker, IntegerSet sendingFilters) throws IllegalAccessException {
		super(reporter, player, invoker);
		this.sendingFilters = sendingFilters;
		this.classLoader = classLoader;
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
			final Class<?> networkInterface = networkManagerRef.getField().getType();
			final Object networkDelegate = networkManagerRef.getOldValue();
			
			if (!networkInterface.isInterface()) {
				throw new UnsupportedOperationException(
						"Must use CraftBukkit 1.3.0 or later to inject into into NetworkMananger.");
			}
			
			Callback queueFilter = new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					Packet packet = (Packet) args[0];
					
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
			Enhancer ex = new Enhancer();
			ex.setClassLoader(classLoader);
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
