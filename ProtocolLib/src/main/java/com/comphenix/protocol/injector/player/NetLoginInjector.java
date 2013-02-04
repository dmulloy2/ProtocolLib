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

import java.util.concurrent.ConcurrentMap;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.player.TemporaryPlayerFactory.InjectContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;

/**
 * Injects every NetLoginHandler created by the server.
 * 
 * @author Kristian
 */
class NetLoginInjector {

	private ConcurrentMap<Object, PlayerInjector> injectedLogins = Maps.newConcurrentMap();
	
	// Handles every hook
	private ProxyPlayerInjectionHandler injectionHandler;
	private Server server;
	
	// The current error rerporter
	private ErrorReporter reporter;
	
	// Used to create fake players
	private TemporaryPlayerFactory tempPlayerFactory = new TemporaryPlayerFactory();
	
	public NetLoginInjector(ErrorReporter reporter, ProxyPlayerInjectionHandler injectionHandler, Server server) {
		this.reporter = reporter;
		this.injectionHandler = injectionHandler;
		this.server = server;
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
			
			Player fakePlayer = tempPlayerFactory.createTemporaryPlayer(server);
			PlayerInjector injector = injectionHandler.injectPlayer(fakePlayer, inserting, GamePhase.LOGIN);
			injector.updateOnLogin = true;
			
			// Associate the injector too
			InjectContainer container = (InjectContainer) fakePlayer;
			container.setInjector(injector);
			
			// Save the login
			injectedLogins.putIfAbsent(inserting, injector);
			
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
				
				// Update NetworkManager
				if (newInjector == null) {
					injectionHandler.uninjectPlayer(player);
				} else {
					injectionHandler.uninjectPlayer(player, false);
					
					if (injected instanceof NetworkObjectInjector)
						newInjector.setNetworkManager(injected.getNetworkManager(), true);
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
