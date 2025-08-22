/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2015 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.injector.netty.channel;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerManager;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftReflection;

import com.google.common.collect.MapMaker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Represents an injector factory.
 * <p>
 * Note that the factory will return {@link EmptyInjector} when the factory is closed.
 *
 * @author Kristian
 */
public class InjectionFactory {

    private final ConcurrentMap<String, Injector> nameLookup = new MapMaker().weakValues().makeMap();
    private final ConcurrentMap<Player, Injector> playerLookup = new MapMaker().weakKeys().weakValues().makeMap();

    // bukkit stuff
    private final Plugin plugin;

    // protocol lib stuff
    private final ErrorReporter errorReporter;
    private final ListenerManager listenerManager;

    // state of the factory
    private boolean closed;

    public InjectionFactory(Plugin plugin, ErrorReporter errorReporter, ListenerManager listenerManager) {
        this.plugin = plugin;
        this.errorReporter = errorReporter;
        this.listenerManager = listenerManager;
    }

    /**
     * Retrieve the main plugin associated with this injection factory.
     *
     * @return The main plugin.
     */
    public Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * Construct or retrieve a channel injector from an existing Bukkit player.
     *
     * @param player   - the existing Bukkit player.
     * @return A new injector, an existing injector associated with this player, or a closed injector.
     */
    @Nonnull
    public Injector fromPlayer(Player player) {
        if (this.closed) {
            return new EmptyInjector(player);
        }

        // try to get the injector using the player reference first
        Injector injector = this.playerLookup.get(player);
        if (injector != null && !injector.isClosed()) {
            ProtocolLogger.log(player.getName() + " has a cached injector. Player valid: " + injector.hasValidPlayer());
            return injector;
        }

        // check if a network manager is present, if not maybe we cached the player temporarily
        Object networkManager = MinecraftFields.getNetworkManager(player);
        if (networkManager == null) {
            return this.fromName(player.getName(), player);
        }

        // get the channel of the player and check if we already hooked into it
        Channel channel = FuzzyReflection.getFieldValue(networkManager, Channel.class, true);
        injector = NettyChannelInjector.findInjector(channel);

        if (injector != null) {
            // check if the new player is not the old one, this saves us a bit when many calls to the method are made
            if (injector.getPlayer() != player || !this.playerLookup.containsKey(player)) {
                this.playerLookup.remove(injector.getPlayer());
                this.cacheInjector(player, injector);
                // re-set the player of the injection
                ProtocolLogger.log("Reusing existing injector and setting player to " + player.getName());
                injector.setPlayer(player);
            } else {
                ProtocolLogger.log(player.getName() + "'s channel already has an injector. Player valid: " + injector.hasValidPlayer());
            }
        } else {
            // construct a new injector as it seems like we have none yet
            injector = new NettyChannelInjector(
                    player,
                    networkManager,
                    channel,
                    this.listenerManager,
                    this,
                    this.errorReporter);
            this.cacheInjector(player, injector);
        }

        // definitely not null
        return injector;
    }

    /**
     * Retrieve a cached injector from a name.
     * <p>
     * The injector may be NULL if the plugin has been reloaded during a player login.
     *
     * @param name   - the name.
     * @param player - the player.
     * @return The cached injector, or a closed injector if it could not be found.
     */
    public Injector fromName(String name, Player player) {
        if (this.closed) {
            return new EmptyInjector(player);
        }

        // check if we have a player with that name cached
        Injector injector = this.nameLookup.get(name);
        if (injector != null) {
            injector.setPlayer(player);
            return injector;
        }

        return new EmptyInjector(player);
    }

    /**
     * Construct a new channel injector for the given channel.
     *
     * @param channel       - the channel.
     * @return The channel injector, or a closed injector.
     */
    @Nonnull
    public Injector fromChannel(Channel channel) {
        if (this.closed) {
            return EmptyInjector.WITHOUT_PLAYER;
        }

        Object networkManager = this.findNetworkManager(channel);
        Player temporaryPlayer = TemporaryPlayerFactory.createTemporaryPlayer();

        NettyChannelInjector injector = new NettyChannelInjector(
                temporaryPlayer,
                networkManager,
                channel,
                this.listenerManager,
                this,
                this.errorReporter);

        // Initialize temporary player
        TemporaryPlayerFactory.setInjectorForPlayer(temporaryPlayer, injector);
        return injector;
    }

    /**
     * Invalidate a cached injector.
     *
     * @param player - the associated player.
     * @return The cached injector, or NULL if nothing was cached.
     */
    public Injector invalidate(Player player, String name) {
        Injector injector = null;

        // try the name first, more unsafe but works 99% of the time
        if (name != null) {
            injector = this.nameLookup.remove(name);
        }

        // if we have a player then use that as the safe removal way
        if (player != null) {
            injector = this.playerLookup.remove(player);
        }

        return injector;
    }

    /**
     * Cache an injector by player.
     *
     * @param player   - the player.
     * @param injector - the injector to cache.
     * @return The previously cached injector.
     */
    public Injector cacheInjector(Player player, Injector injector) {
        this.nameLookup.put(player.getName(), injector);
        return this.playerLookup.put(player, injector);
    }

    /**
     * Cache an injector by name alone.
     *
     * @param name     - the name to lookup.
     * @param injector - the injector.
     * @return The cached injector.
     */
    public Injector cacheInjector(String name, Injector injector) {
        return this.nameLookup.put(name, injector);
    }

    /**
     * Retrieve the associated channel injector.
     *
     * @param player - the temporary player, or normal Bukkit player.
     * @return The associated injector, or NULL if this is a Bukkit player.
     */
    private NettyChannelInjector getTemporaryInjector(Player player) {
        Injector injector = TemporaryPlayerFactory.getInjectorFromPlayer(player);
        if (injector instanceof NettyChannelInjector) {
            return (NettyChannelInjector) injector;
        }

        return null;
    }

    /**
     * Find the network manager in a channel's pipeline.
     *
     * @param channel - the channel.
     * @return The network manager.
     */
    private Object findNetworkManager(Channel channel) {
        Class<?> networkManagerClass = MinecraftReflection.getNetworkManagerClass();

        for (Entry<String, ChannelHandler> entry : channel.pipeline()) {
            if (networkManagerClass.isAssignableFrom(entry.getValue().getClass())) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException("Unable to find NetworkManager in " + channel);
    }

    /**
     * Determine if the factory is closed.
     * <p>
     * If it is, all new injectors will be closed by default.
     *
     * @return TRUE if it is closed, FALSE otherwise.
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * Close all injectors created by this factory, and cease the creation of new injections.
     */
    public void close() {
        if (!this.closed) {
            this.closed = true;

            // Close everything
            for (Injector injector : this.playerLookup.values()) {
                injector.close();
            }

            for (Injector injector : this.nameLookup.values()) {
                injector.close();
            }
        }
    }
}
