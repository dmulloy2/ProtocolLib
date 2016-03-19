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
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler.ConflictStrategy;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;

/**
 * Injects every NetLoginHandler created by the server.
 * 
 * @author Kristian
 */
class NetLoginInjector {
	public static final ReportType REPORT_CANNOT_HOOK_LOGIN_HANDLER = new ReportType("Unable to hook %s.");
	public static final ReportType REPORT_CANNOT_CLEANUP_LOGIN_HANDLER = new ReportType("Cannot cleanup %s.");
	
	private ConcurrentMap<Object, PlayerInjector> injectedLogins = Maps.newConcurrentMap();

	// Handles every hook
	private ProxyPlayerInjectionHandler injectionHandler;

	// Create temporary players
	private TemporaryPlayerFactory playerFactory = new TemporaryPlayerFactory();
	
	// The current error reporter
	private ErrorReporter reporter;
	private Server server;
	
	public NetLoginInjector(ErrorReporter reporter, Server server, ProxyPlayerInjectionHandler injectionHandler) {
		this.reporter = reporter;
		this.server = server;
		this.injectionHandler = injectionHandler;
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
			
			Player temporary = playerFactory.createTemporaryPlayer(server);
			// Note that we bail out if there's an existing player injector
			PlayerInjector injector = injectionHandler.injectPlayer(
					temporary, inserting, ConflictStrategy.BAIL_OUT, GamePhase.LOGIN);
			
			if (injector != null) {
				// Update injector as well
				TemporaryPlayerFactory.setInjectorInPlayer(temporary, injector);
				injector.updateOnLogin = true;
	
				// Save the login
				injectedLogins.putIfAbsent(inserting, injector);
			}
			
			// NetServerInjector can never work (currently), so we don't need to replace the NetLoginHandler
			return inserting;	
			
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			// Minecraft can't handle this, so we'll deal with it here
			reporter.reportDetailed(this, 
					Report.newBuilder(REPORT_CANNOT_HOOK_LOGIN_HANDLER).
						messageParam(MinecraftReflection.getNetLoginHandlerName()).
						callerParam(inserting, injectionHandler).
						error(e)
			);
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
				
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				// Don't leak this to Minecraft
				reporter.reportDetailed(this, 
						Report.newBuilder(REPORT_CANNOT_CLEANUP_LOGIN_HANDLER).
							messageParam(MinecraftReflection.getNetLoginHandlerName()).
							callerParam(removing).
							error(e)
				);
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
