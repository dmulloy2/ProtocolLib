package com.comphenix.protocol.injector.netty;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import net.minecraft.util.io.netty.channel.Channel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.injector.netty.ChannelInjector.ChannelSocketInjector;
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
class InjectionFactory {
	// This should work as long as the injectors are, uh, injected
	private final ConcurrentMap<Player, Injector> playerLookup = new MapMaker().weakKeys().weakValues().makeMap();
	private final ConcurrentMap<String, Injector> nameLookup = new MapMaker().weakValues().makeMap();
	
	// Whether or not the factory is closed
	private volatile boolean closed;
	
	// The current plugin
	private final Plugin plugin;
	
	public InjectionFactory(Plugin plugin) {
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
	 * @param channelListener - the listener.
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
		injector = (ChannelInjector) ChannelInjector.findChannelHandler(channel, ChannelInjector.class);
		
		if (injector != null) {
			// Update the player instance
			playerLookup.remove(injector.getPlayer());
			injector.setPlayer(player);
		} else {
			injector = new ChannelInjector(player, networkManager, channel, listener, this);
		}
		
		// Cache injector and return
		cacheInjector(player, injector);
		return injector;
	}
		
	/**
	 * Retrieve a cached injector from a name.
	 * <p>
	 * The injector may be NULL if the plugin has been reloaded during a player login.
	 * @param address - the name.
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
	 * @param playerFactory - a temporary player creator.
	 * @param channelListener - the listener.
	 * @param loader - the current (plugin) class loader.
	 * @return The channel injector, or a closed injector.
	 */
	@Nonnull
	public Injector fromChannel(Channel channel, ChannelListener listener, TemporaryPlayerFactory playerFactory) {		
		if (closed)
			return new ClosedInjector(null);
		
		Object networkManager = findNetworkManager(channel);	
		Player temporaryPlayer = playerFactory.createTemporaryPlayer(Bukkit.getServer());
		ChannelInjector injector = new ChannelInjector(temporaryPlayer, networkManager, channel, listener, this);
		
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
	private ChannelInjector getTemporaryInjector(Player player) {
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
		Object networkManager = ChannelInjector.findChannelHandler(channel, MinecraftReflection.getNetworkManagerClass());
		
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
