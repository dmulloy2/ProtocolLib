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

import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.entity.Player;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.server.AbstractInputStreamLookup;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;

/**
 * Injects every NetLoginHandler created by the server.
 * 
 * @author Kristian
 */
class NetLoginInjector {
	private ConcurrentMap<Object, PlayerInjector> injectedLogins = Maps.newConcurrentMap();
	
	private static Method getSocketMethod;
	
	// Handles every hook
	private ProxyPlayerInjectionHandler injectionHandler;
	
	// Associate input streams and injectors
	private AbstractInputStreamLookup inputStreamLookup;
	
	// The current error rerporter
	private ErrorReporter reporter;
	
	public NetLoginInjector(ErrorReporter reporter, ProxyPlayerInjectionHandler injectionHandler, AbstractInputStreamLookup inputStreamLookup) {
		this.reporter = reporter;
		this.injectionHandler = injectionHandler;
		this.inputStreamLookup = inputStreamLookup;
	}

	/**
	 * Invoked when a NetLoginHandler has been created.
	 * @param inserting - the new NetLoginHandler.
	 * @return An injected NetLoginHandler, or the original object.
	 */
	public Object onNetLoginCreated(Object inserting) {
		try {
			// Make sure we actually need to inject during this phase
			if (!injectionHandler.isInjectionNecessary(GamePhase.LOGIN))
				return inserting;
			
			if (getSocketMethod == null) {
				getSocketMethod = FuzzyReflection.fromObject(inserting).
					getMethodByParameters("getSocket", Socket.class, new Class<?>[0]);
			}

			// Get the underlying socket
			Socket socket = (Socket) getSocketMethod.invoke(inserting);
			SocketInjector socketInjector = inputStreamLookup.getSocketInjector(socket);
			
			// This is the case if we're dealing with a connection initiated by the injected server socket
			if (socketInjector != null) {
				PlayerInjector injector = injectionHandler.injectPlayer(socketInjector.getPlayer(), inserting, GamePhase.LOGIN);
				injector.updateOnLogin = true;
	
				// Save the login
				injectedLogins.putIfAbsent(inserting, injector);
			}
			
			// NetServerInjector can never work (currently), so we don't need to replace the NetLoginHandler
			return inserting;	
			
		} catch (Throwable e) {
			// Minecraft can't handle this, so we'll deal with it here
			reporter.reportDetailed(this, "Unable to hook " + 
						MinecraftReflection.getNetLoginHandlerName() + ".", e, inserting);
			return inserting;
		}
	}
	
	/**
	 * Invoked when a NetLoginHandler should be reverted.
	 * @param inserting - the original NetLoginHandler.
	 * @return An injected NetLoginHandler, or the original object.
	 */
	public synchronized void cleanup(Object removing) {
		PlayerInjector injected = injectedLogins.get(removing);
		
		if (injected != null) {
			try {
				PlayerInjector newInjector = null;
				Player player = injected.getPlayer();
				
				// Clean up list
				injectedLogins.remove(removing);
				
				// No need to clean up twice
				if (injected.isClean())
					return;
				
				// Hack to clean up other references
				newInjector = injectionHandler.getInjectorByNetworkHandler(injected.getNetworkManager());
				injectionHandler.uninjectPlayer(player);
				
				// Update NetworkManager
				if (newInjector != null) {
					if (injected instanceof NetworkObjectInjector) {
						newInjector.setNetworkManager(injected.getNetworkManager(), true);
					}
				}
				
			} catch (Throwable e) {
				// Don't leak this to Minecraft
				reporter.reportDetailed(this, "Cannot cleanup " + 
						MinecraftReflection.getNetLoginHandlerName() + ".", e, removing);
			}
		}
	}
	
	/**
	 * Remove all injected hooks.
	 */
	public void cleanupAll() {
		for (PlayerInjector injector : injectedLogins.values()) {
			injector.cleanupAll();
		}
		
		injectedLogins.clear();
	}
}
