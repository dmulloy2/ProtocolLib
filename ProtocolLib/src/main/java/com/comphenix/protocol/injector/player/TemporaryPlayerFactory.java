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
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.FieldAccessException;

/**
 * Create fake player instances that represents pre-authenticated clients.
 */
class TemporaryPlayerFactory {
	
	/**
	 * Able to store a PlayerInjector.
	 * <p>
	 * A necessary hack.
	 * @author Kristian
	 */
	public static class InjectContainer {
		private PlayerInjector injector;

		public PlayerInjector getInjector() {
			return injector;
		}

		public void setInjector(PlayerInjector injector) {
			this.injector = injector;
		}
	}
	
	// Helpful constructors
	private final PacketConstructor chatPacket;
	
	// Prevent too many class creations
	private static CallbackFilter callbackFilter;
	
	public TemporaryPlayerFactory() {
		chatPacket =  PacketConstructor.DEFAULT.withPacket(3, new Object[] { "DEMO" });
	}
	
	/**
	 * Construct a temporary player that supports a subset of every player command.
	 * <p>
	 * Supported methods include:
	 * <ul>
	 *   <li>getPlayer()</li>
	 *   <li>getAddress()</li>
	 *   <li>getServer()</li>
	 *   <li>chat(String)</li>
	 *   <li>sendMessage(String)</li>
	 *   <li>sendMessage(String[])</li>
	 *   <li>kickPlayer(String)</li>
	 * </ul>
	 * <p>
	 * Note that the player a player has not been assigned a name yet, and thus cannot be
	 * uniquely identified. Use the address instead.
	 * @param injector - the player injector used.
	 * @param server - the current server.
	 * @return A temporary player instance.
	 */
	public Player createTemporaryPlayer(final Server server) {
	
		// Default implementation
		Callback implementation = new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

				String methodName = method.getName();
				PlayerInjector injector = ((InjectContainer) obj).getInjector();
				
				if (injector == null)
					throw new IllegalStateException("Unable to find injector.");
				
				// Use the socket to get the address
				if (methodName.equalsIgnoreCase("getName"))
					return "UNKNOWN[" + injector.getSocket().getRemoteSocketAddress() + "]";
				if (methodName.equalsIgnoreCase("getPlayer"))
					return injector.getUpdatedPlayer();
				if (methodName.equalsIgnoreCase("getAddress")) 
					return injector.getAddress();
				if (methodName.equalsIgnoreCase("getServer"))
					return server;
				
				try {
					// Handle send message methods
					if (methodName.equalsIgnoreCase("chat") || methodName.equalsIgnoreCase("sendMessage")) {
						Object argument = args[0];
						
						// Dynamic overloading
						if (argument instanceof String) {
							return sendMessage(injector, (String) argument);
						} else if (argument instanceof String[]) {
							for (String message : (String[]) argument) {
								sendMessage(injector, message);
							}
							return null;
						}
					}
				} catch (InvocationTargetException e) {
					throw e.getCause();
				}
				
				// Also, handle kicking
				if (methodName.equalsIgnoreCase("kickPlayer")) {
					injector.disconnect((String) args[0]);
					return null;
				}
					
				// Ignore all other methods
				throw new UnsupportedOperationException(
						"The method " + method.getName() + " is not supported for temporary players.");
			}
    	};
		
		// Shared callback filter
		if (callbackFilter == null) {
			callbackFilter = new CallbackFilter() {
				@Override
				public int accept(Method method) {
					// Do not override the object method or the superclass methods
					if (method.getDeclaringClass().equals(Object.class) ||
						method.getDeclaringClass().equals(InjectContainer.class))
						return 0;
					else 
						return 1;
				}
			};
		}
    	
		// CGLib is amazing
    	Enhancer ex = new Enhancer();
    	ex.setSuperclass(InjectContainer.class);
    	ex.setInterfaces(new Class[] { Player.class });
		ex.setCallbacks(new Callback[] { NoOp.INSTANCE, implementation });
		ex.setCallbackFilter(callbackFilter);
    	
    	return (Player) ex.create();
	}
	
	/**
	 * Send a message to the given client.
	 * @param injector - the injector representing the client.
	 * @param message - a message.
	 * @return Always NULL.
	 * @throws InvocationTargetException If the message couldn't be sent.
	 * @throws FieldAccessException If we were unable to construct the message packet.
	 */
	private Object sendMessage(PlayerInjector injector, String message) throws InvocationTargetException, FieldAccessException {
		injector.sendServerPacket(chatPacket.createPacket(message).getHandle(), false);
		return null;
	}
}
