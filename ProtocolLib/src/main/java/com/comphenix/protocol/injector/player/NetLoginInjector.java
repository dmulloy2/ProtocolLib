package com.comphenix.protocol.injector.player;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.injector.player.PlayerInjectionHandler.GamePhase;
import com.comphenix.protocol.injector.player.TemporaryPlayerFactory.InjectContainer;
import com.google.common.collect.Maps;

public class NetLoginInjector {

	private ConcurrentMap<Object, PlayerInjector> injectedLogins = Maps.newConcurrentMap();
	
	// Handles every hook
	private PlayerInjectionHandler injectionHandler;
	private Server server;
	
	private ReadWriteLock injectionLock = new ReentrantReadWriteLock();
	
	// Used to create fake players
	private TemporaryPlayerFactory tempPlayerFactory = new TemporaryPlayerFactory();
	
	public NetLoginInjector(PlayerInjectionHandler injectionHandler, Server server) {
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
			Player fakePlayer = tempPlayerFactory.createTemporaryPlayer(server);
			PlayerInjector injector = injectionHandler.injectPlayer(fakePlayer, inserting, GamePhase.LOGIN);
			
			// Associate the injector too
			InjectContainer container = (InjectContainer) fakePlayer;
			container.setInjector(injector);
			
			// NetServerInjector can never work (currently), so we don't need to replace the NetLoginHandler
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
			injected.cleanupAll();
			injectedLogins.remove(removing);
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
