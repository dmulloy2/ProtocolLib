package com.comphenix.protocol.injector.player;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.player.TemporaryPlayerFactory.InjectContainer;
import com.google.common.collect.Maps;

/**
 * Injects every NetLoginHandler created by the server.
 * 
 * @author Kristian
 */
class NetLoginInjector {

	private ConcurrentMap<Object, PlayerInjector> injectedLogins = Maps.newConcurrentMap();
	
	// Handles every hook
	private PlayerInjectionHandler injectionHandler;
	private Server server;
	
	// The current logger
	private Logger logger;
	
	private ReadWriteLock injectionLock = new ReentrantReadWriteLock();
	
	// Used to create fake players
	private TemporaryPlayerFactory tempPlayerFactory = new TemporaryPlayerFactory();
	
	public NetLoginInjector(Logger logger, PlayerInjectionHandler injectionHandler, Server server) {
		this.logger = logger;
		this.injectionHandler = injectionHandler;
		this.server = server;
	}

	/**
	 * Invoked when a NetLoginHandler has been created.
	 * @param inserting - the new NetLoginHandler.
	 * @return An injected NetLoginHandler, or the original object.
	 */
	public Object onNetLoginCreated(Object inserting) {
		
		injectionLock.writeLock().lock();
		
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
			logger.log(Level.WARNING, "Unable to hook NetLoginHandler.", e);
			return inserting;
			
		} finally {
			injectionLock.writeLock().unlock();
		}
	}
	
	/**
	 * Retrieve the lock used for reading.
	 * @return Reading lock.
	 */
	public Lock getReadLock() {
		return injectionLock.readLock();
	}
	
	/**
	 * Invoked when a NetLoginHandler should be reverted.
	 * @param inserting - the original NetLoginHandler.
	 * @return An injected NetLoginHandler, or the original object.
	 */
	public synchronized void cleanup(Object removing) {
		PlayerInjector injected = injectedLogins.get(removing);
		
		if (injected != null) {
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
			
			//logger.warning("Using alternative cleanup method.");
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
