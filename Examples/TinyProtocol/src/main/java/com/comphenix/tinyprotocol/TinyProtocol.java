package com.comphenix.tinyprotocol;

import java.util.Map;
import java.util.logging.Level;

// These are not versioned, but they require CraftBukkit
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPromise;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.tinyprotocol.Reflection.FieldAccessor;
import com.comphenix.tinyprotocol.Reflection.MethodInvoker;
import com.google.common.collect.MapMaker;

/**
 * Represents a very tiny alternative to ProtocolLib in 1.7.2.
 * <p>
 * Note that it does not support intercepting packets sent during login (such as OUT_SERVER_PING).
 * @author Kristian
 */
public abstract class TinyProtocol {	
	// Used in order to lookup a channel
	private MethodInvoker getPlayerHandle = Reflection.getMethod("{obc}.entity.CraftPlayer", "getHandle");
	private FieldAccessor<Object> getConnection = Reflection.getField("{nms}.EntityPlayer", "playerConnection", Object.class);
	private FieldAccessor<Object> getManager = Reflection.getField("{nms}.PlayerConnection", "networkManager", Object.class);
	private FieldAccessor<Channel> getChannel = Reflection.getField("{nms}.NetworkManager", Channel.class, 0);
	
	// Speedup channel lookup
	private Map<Player, Channel> channelLookup = new MapMaker().weakKeys().makeMap();
	private Listener listener;
	
	protected boolean closed;
	protected Plugin plugin;
	
	public TinyProtocol(Plugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(
				listener = createListener(), plugin);
		
		// Prepare existing players
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			injectPlayer(player);
		}
	}
		
	/**
	 * Invoked when the server is starting to send a packet to a player.
	 * <p>
	 * Note that this is not executed on the main thread.
	 * @param reciever - the receiving player.
	 * @param packet - the packet being sent.
	 * @return The packet to send instead, or NULL to cancel the transmission.
	 */
	public Object onPacketOutAsync(Player reciever, Object packet) {
		return packet;
	}

	/**
	 * Invoked when the server has received a packet from a given player.
	 * @param sender - the player that sent the packet.
	 * @param packet - the packet being sent.
	 * @return The packet to recieve instead, or NULL to cancel.
	 */
	public Object onPacketInAsync(Player sender, Object packet) {
		return packet;
	}
	
	/**
	 * Send a packet to a particular player.
	 * <p>
	 * Note that {@link #onPacketOutAsync(Player, Object)} will be invoked with this packet.
	 * @param player - the destination player.
	 * @param packet - the packet to send.
	 */
	public void sendPacket(Player player, Object packet) {
		getChannel(player).pipeline().writeAndFlush(packet);
	}
	
	/**
	 * Pretend that a given packet has been received from a player.
	 * <p>
	 * Note that {@link #onPacketInAsync(Player, Object)} will be invoked with this packet.
	 * @param player - the player that sent the packet.
	 * @param packet - the packet that will be received by the server.
	 */
	public void receivePacket(Player player, Object packet) {
		getChannel(player).pipeline().context("encoder").fireChannelRead(packet);
	}
	
	/**
	 * Retrieve the name of the channel injector, default implementation is "tiny-" + plugin name.
	 * <p>
	 * Override this if you have multiple instances of TinyProtocol, and return a unique string per instance. 
	 * @return A unique channel handler name.
	 */
	protected String getHandlerName() {
		return "tiny-" + plugin.getName();
	}

	/**
	 * Add a custom channel handler to the given player's channel pipeline, allowing us to intercept sent and received packets.
	 * @param player - the player to inject.
	 */
	private void injectPlayer(final Player player) {
		// Inject our packet interceptor
		getChannel(player).pipeline().addBefore("packet_handler", getHandlerName(), new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				try {
					msg = onPacketInAsync(player, msg);
				} catch (Exception e) {
					plugin.getLogger().log(Level.SEVERE, "Error in onPacketInAsync().", e);
				}
				
				if (msg != null) {
					super.channelRead(ctx, msg);
				}
			}
			
			@Override
			public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
				try {
					msg = onPacketOutAsync(player, msg);
				} catch (Exception e) {
					plugin.getLogger().log(Level.SEVERE, "Error in onPacketOutAsync().", e);
				}
				
				if (msg != null) {
					super.write(ctx, msg, promise);
				}
			}
		});
	}
	
	/**
	 * Retrieve the Netty channel associated with a player. This is cached.
	 * @param player - the player.
	 * @return The Netty channel.
	 */
	private Channel getChannel(Player player) {
		Channel channel = channelLookup.get(player);
		
		// Lookup channel again
		if (channel == null) {
			Object connection = getConnection.get(getPlayerHandle.invoke(player));
			Object manager = getManager.get(connection);
			
			channelLookup.put(player, channel = getChannel.get(manager));
		}
		return channel;
	}
	
	/**
	 * Create the Bukkit listener.
	 */
	private Listener createListener() {
		return new Listener() {
			@EventHandler(priority = EventPriority.LOWEST)
			public final void onPlayerJoin(PlayerJoinEvent e) {
				if (closed) 
					return;
				injectPlayer(e.getPlayer());
			}
			
			@EventHandler
			public final void onPluginDisable(PluginDisableEvent e) {
				if (e.getPlugin().equals(plugin)) {
					close();
				}
			}
		};
	}
	
	/**
	 * Cease listening for packets. This is called automatically when your plugin is disabled.
	 */
	public final void close() {
		if (!closed) {
			closed = true;
			// Compute this once
			final String handlerName = getHandlerName();
			
			// Remove our handlers
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				final Channel channel = getChannel(player);
				
				// See ChannelInjector in ProtocolLib, line 590
				channel.eventLoop().execute(new Runnable() {
					@Override
					public void run() {
						channel.pipeline().remove(handlerName);
					}
				});
			}
			
			// Clean up Bukkit
			HandlerList.unregisterAll(listener);
		}
	}
}
