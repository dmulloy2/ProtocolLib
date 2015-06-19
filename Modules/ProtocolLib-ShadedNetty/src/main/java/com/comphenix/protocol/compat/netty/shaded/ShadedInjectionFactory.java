/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
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
package com.comphenix.protocol.compat.netty.shaded;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import net.minecraft.util.io.netty.channel.Channel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.compat.netty.shaded.ShadedChannelInjector.ChannelSocketInjector;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.netty.ClosedInjector;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.MapMaker;

/**
 * Represents an injector factory.
 * <p>
 * Note that the factory will return {@link ClosedInjector} when the factory is closed.
 * @author Kristian
 */
public class ShadedInjectionFactory {
	// This should work as long as the injectors are, uh, injected
	private final ConcurrentMap<Player, Injector> playerLookup = new MapMaker().weakKeys().weakValues().makeMap();
	private final ConcurrentMap<String, Injector> nameLookup = new MapMaker().weakValues().makeMap();
	
	// Whether or not the factory is closed
	private volatile boolean closed;
	
	// The current plugin
	private final Plugin plugin;
	
	public ShadedInjectionFactory(Plugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Retrieve the main plugin associated with this injection factory.
	 * @return The main plugin.
	 */
	public Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Construct or retrieve a channel injector from an existing Bukkit player.
	 * @param player - the existing Bukkit player.
	 * @param listener - the listener.
	 * @return A new injector, an existing injector associated with this player, or a closed injector.
	 */
	@Nonnull
	public Injector fromPlayer(Player player, ChannelListener listener) {
		if (closed)
			return new ClosedInjector(player);
		Injector injector = playerLookup.get(player);
		
		// Find a temporary injector as well
		if (injector == null)
			injector = getTemporaryInjector(player);
		if (injector != null && !injector.isClosed())
			return injector;
		
		Object networkManager = MinecraftFields.getNetworkManager(player);
		
		// Must be a temporary Bukkit player
		if (networkManager == null) {
			return fromName(player.getName(), player);
		}
		Channel channel = FuzzyReflection.getFieldValue(networkManager, Channel.class, true);
		
		// See if a channel has already been created
		injector = (ShadedChannelInjector) ShadedChannelInjector.findChannelHandler(channel, ShadedChannelInjector.class);
		
		if (injector != null) {
			// Update the player instance
			playerLookup.remove(injector.getPlayer());
			injector.setPlayer(player);
		} else {
			injector = new ShadedChannelInjector(player, networkManager, channel, listener, this);
		}
		
		// Cache injector and return
		cacheInjector(player, injector);
		return injector;
	}
		
	/**
	 * Retrieve a cached injector from a name.
	 * <p>
	 * The injector may be NULL if the plugin has been reloaded during a player login.
	 * @param name - the name.
	 * @param player - the player.
	 * @return The cached injector, or a closed injector if it could not be found.
	 */
	public Injector fromName(String name, Player player) {
		if (!closed) {
			Injector injector = nameLookup.get(name);
			
			// We can only retrieve cached injectors
			if (injector != null) {
				// Update instance
				injector.setUpdatedPlayer(player);
				return injector;
			}
		}
		return new ClosedInjector(player);
	}
	
	/**
	 * Construct a new channel injector for the given channel.
	 * @param channel - the channel.
	 * @param listener - the listener.
	 * @param playerFactory - a temporary player creator.
	 * @return The channel injector, or a closed injector.
	 */
	@Nonnull
	public Injector fromChannel(Channel channel, ChannelListener listener, TemporaryPlayerFactory playerFactory) {
		if (closed)
			return new ClosedInjector(null);
		
		Object networkManager = findNetworkManager(channel);
		Player temporaryPlayer = playerFactory.createTemporaryPlayer(Bukkit.getServer());
		ShadedChannelInjector injector = new ShadedChannelInjector(temporaryPlayer, networkManager, channel, listener, this);
		
		// Initialize temporary player
		TemporaryPlayerFactory.setInjectorInPlayer(temporaryPlayer, new ChannelSocketInjector(injector));
		return injector;
	}
	
	/**
	 * Invalidate a cached injector.
	 * @param player - the associated player.
	 * @return The cached injector, or NULL if nothing was cached.
	 */
	public Injector invalidate(Player player) {
		Injector injector = playerLookup.remove(player);
		
		nameLookup.remove(player.getName());
		return injector;
	}
	
	/**
	 * Cache an injector by player.
	 * @param player - the player.
	 * @param injector - the injector to cache.
	 * @return The previously cached injector.
	 */
	public Injector cacheInjector(Player player, Injector injector) {
		nameLookup.put(player.getName(), injector);
		return playerLookup.put(player, injector);
	}
	
	/**
	 * Cache an injector by name alone.
	 * @param name - the name to lookup.
	 * @param injector - the injector.
	 * @return The cached injector.
	 */
	public Injector cacheInjector(String name, Injector injector) {
		return nameLookup.put(name, injector);
	}
	
	/**
	 * Retrieve the associated channel injector.
	 * @param player - the temporary player, or normal Bukkit player.
	 * @return The associated injector, or NULL if this is a Bukkit player.
	 */
	private ShadedChannelInjector getTemporaryInjector(Player player) {
		SocketInjector injector = TemporaryPlayerFactory.getInjectorFromPlayer(player);
		
		if (injector != null) {
			return ((ChannelSocketInjector) injector).getChannelInjector();
		}
		return null;
	}
	
	/**
	 * Find the network manager in a channel's pipeline.
	 * @param channel - the channel.
	 * @return The network manager.
	 */
	private Object findNetworkManager(Channel channel) {
		// Find the network manager
		Object networkManager = ShadedChannelInjector.findChannelHandler(channel, MinecraftReflection.getNetworkManagerClass());
		
		if (networkManager != null)
			return networkManager;
		throw new IllegalArgumentException("Unable to find NetworkManager in " + channel);
	}
	
	/**
	 * Determine if the factory is closed.
	 * <p>
	 * If it is, all new injectors will be closed by default.
	 * @return TRUE if it is closed, FALSE otherwise.
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * Close all injectors created by this factory, and cease the creation of new injections.
	 */
	public synchronized void close() {
		if (!closed) {
			closed = true;
			
			// Close everything
			for (Injector injector : playerLookup.values())
				injector.close();
			for (Injector injector : nameLookup.values())
				injector.close();
		}
	}
}
