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

package com.comphenix.protocol.injector.server;

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

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.ChatExtensions;

/**
 * Create fake player instances that represents pre-authenticated clients.
 */
public class TemporaryPlayerFactory {
	// Prevent too many class creations
	private static CallbackFilter callbackFilter;
	
	/**
	 * Retrieve the injector from a given player if it contains one.
	 * @param player - the player that may contain a reference to a player injector.
	 * @return The referenced player injector, or NULL if none can be found.
	 */
	public static SocketInjector getInjectorFromPlayer(Player player) {
		if (player instanceof InjectorContainer) {
			return ((InjectorContainer) player).getInjector();
		} 
		return null;
	}
	
	/**
	 * Set the player injector, if possible.
	 * @param player - the player to update.
	 * @param injector - the injector to store.
	 */
	public static void setInjectorInPlayer(Player player, SocketInjector injector) {
		((InjectorContainer) player).setInjector(injector);
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
	 * Note that a temporary player has not yet been assigned a name, and thus cannot be
	 * uniquely identified. Use the address instead.
	 * @param server - the current server.
	 * @return A temporary player instance.
	 */
	public Player createTemporaryPlayer(final Server server) {
	
		// Default implementation
		Callback implementation = new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				String methodName = method.getName();
				SocketInjector injector = ((InjectorContainer) obj).getInjector();
				
				if (injector == null)
					throw new IllegalStateException("Unable to find injector.");

				// Use the socket to get the address
				else if (methodName.equals("getPlayer"))
					return injector.getUpdatedPlayer();
				else if (methodName.equals("getAddress")) 
					return injector.getAddress();
				else if (methodName.equals("getServer"))
					return server;

				// Handle send message methods
				if (methodName.equals("chat") || methodName.equals("sendMessage")) {
					try {
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
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				}
				
				// Also, handle kicking
				if (methodName.equals("kickPlayer")) {
					injector.disconnect((String) args[0]);
					return null;
				}
				
				// The fallback instance
				Player updated = injector.getUpdatedPlayer();
				
				if (updated != obj && updated != null) {
					return proxy.invoke(updated, args);
				}
				
				// Methods that are supported in the fallback instance
				if (methodName.equals("isOnline"))
					return injector.getSocket() != null && injector.getSocket().isConnected();
				else if (methodName.equals("getName"))
					return "UNKNOWN[" + injector.getSocket().getRemoteSocketAddress() + "]";
				
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
						method.getDeclaringClass().equals(InjectorContainer.class))
						return 0;
					else 
						return 1;
				}
			};
		}
    	
		// CGLib is amazing
    	Enhancer ex = new Enhancer();
    	ex.setSuperclass(InjectorContainer.class);
    	ex.setInterfaces(new Class[] { Player.class });
		ex.setCallbacks(new Callback[] { NoOp.INSTANCE, implementation });
		ex.setCallbackFilter(callbackFilter);
    	
    	return (Player) ex.create();
	}
	
	/**
	 * Construct a temporary player with the given associated socket injector.
	 * @param server - the parent server.
	 * @param injector - the referenced socket injector.
	 * @return The temporary player.
	 */
	public Player createTemporaryPlayer(Server server, SocketInjector injector) {
		Player temporary = createTemporaryPlayer(server);
		
		((InjectorContainer) temporary).setInjector(injector);
		return temporary;
	}
	
	/**
	 * Send a message to the given client.
	 * @param injector - the injector representing the client.
	 * @param message - a message.
	 * @return Always NULL.
	 * @throws InvocationTargetException If the message couldn't be sent.
	 * @throws FieldAccessException If we were unable to construct the message packet.
	 */
	private Object sendMessage(SocketInjector injector, String message) throws InvocationTargetException, FieldAccessException {
		for (PacketContainer packet : ChatExtensions.createChatPackets(message)) {
			injector.sendServerPacket(packet.getHandle(), null, false);
		}
		return null;
	}
}
