package com.comphenix.tinyprotocol;

import java.lang.reflect.Field;
import javax.annotation.Nullable;

import net.minecraft.server.v1_7_R1.NetworkManager;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPromise;

import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Function;

public abstract class TinyProtocol implements Listener {
	private static Function<Object, Channel> CHANNEL_ACCESSOR = getFieldAccessor(NetworkManager.class, Channel.class, 0);
	
	private boolean closed;
	private Plugin plugin;
	
	public TinyProtocol(Plugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		// Prepare existing players
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			injectPlayer(player);
		}
	}
	
	@EventHandler
	public final void onPlayerJoin(PlayerJoinEvent e) {
		if (closed) 
			return;
		injectPlayer(e.getPlayer());
	}

	private void injectPlayer(final Player player) {
		// Inject our packet interceptor
		getChannel(player).pipeline().addBefore("packet_handler", getHandlerName(), new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				Object result = onPacketInAsync(player, msg);
				
				if (result != null) {
					super.channelRead(ctx, result);
				}
			}
			
			@Override
			public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
				Object result = onPacketOutAsync(player, msg);
				
				if (result != null) {
					super.write(ctx, result, promise);
				}
			}
		});
	}

	private String getHandlerName() {
		return "tiny-" + plugin.getName();
	}

	@EventHandler
	public final void onPluginDisable(PluginDisableEvent e) {
		if (e.getPlugin().equals(plugin)) {
			close();
		}
	}
	
	private Channel getChannel(Player player) {
		NetworkManager manager = ((CraftPlayer) player.getPlayer()).getHandle().playerConnection.networkManager;
		return CHANNEL_ACCESSOR.apply(manager);
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
	 * Retrieve a field accessor for a specific field type and index.
	 * @param target - the target type.
	 * @param fieldType - the field type.
	 * @param index - the index.
	 * @return The field accessor.
	 */
	public static <T> Function<Object, T> getFieldAccessor(Class<?> target, Class<T> fieldType, int index) {
		for (Field field : target.getDeclaredFields()) {
			if (fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
				final Field targetField = field;
				field.setAccessible(true);
				
				// A function for retrieving a specific field value
				return new Function<Object, T>() {
					@SuppressWarnings("unchecked")
					@Override
					public T apply(@Nullable Object instance) {
						try {
							return (T) targetField.get(instance);
						} catch (IllegalAccessException e) {
							throw new RuntimeException("Cannot access reflection.", e);
						}
					}
				};
			}
		}
		
		// Search in parent classes
		if (target.getSuperclass() != null)
			return getFieldAccessor(target.getSuperclass(), fieldType, index);
		throw new IllegalArgumentException("Cannot find field with type " + fieldType);
	}
	
	public final void close() {
		if (!closed) {
			closed = true;
	
			// Remove our handlers
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				getChannel(player).pipeline().remove(getHandlerName());
			}
		}
	}
}
