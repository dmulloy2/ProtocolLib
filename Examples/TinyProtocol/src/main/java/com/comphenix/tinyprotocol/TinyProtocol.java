package com.comphenix.tinyprotocol;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;

// These are not versioned, but they require CraftBukkit
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPromise;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.MapMaker;

public abstract class TinyProtocol implements Listener {
	// Deduce the net.minecraft.server.v* package
	private static String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
	private static String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");
	
	// Used in order to lookup a channel
	private MethodInvoker getPlayerHandle = getMethod(getCraftBukkitClass("entity.CraftPlayer"), "getHandle");
	private FieldAccessor<Object> getConnection = getField(getMinecraftClass("EntityPlayer"), "playerConnection", Object.class);
	private FieldAccessor<Object> getManager = getField(getMinecraftClass("PlayerConnection"), "networkManager", Object.class);
	private FieldAccessor<Channel> getChannel = getField(getMinecraftClass("NetworkManager"), Channel.class, 0);
	
	// Speedup channel lookup
	private Map<Player, Channel> channelLookup = new MapMaker().weakKeys().makeMap();
	
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

	private String getHandlerName() {
		return "tiny-" + plugin.getName();
	}

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
	
	/**
	 * Send a packet to a particular player.
	 * @param player - the destination player.
	 * @param packet - the packet to send.
	 */
	public void sendPacket(Player player, Object packet) {
		getChannel(player).pipeline().writeAndFlush(packet);
	}
	
	/**
	 * Pretend that a given packet has been received from a player.
	 * @param player - the player that sent the packet.
	 * @param packet - the packet that will be received by the server.
	 */
	public void receivePacket(Player player, Object packet) {
		getChannel(player).pipeline().context("encoder").fireChannelRead(packet);
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
	 * Cease listening for packets. This is called automatically when your plugin is disabled.
	 */
	public final void close() {
		if (!closed) {
			closed = true;
	
			// Remove our handlers
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				getChannel(player).pipeline().remove(getHandlerName());
			}
		}
	}
	
	/**
	 * Retrieve a field accessor for a specific field type and name.
	 * @param target - the target type.
	 * @param name - the name of the field, or NULL to ignore.
	 * @param fieldType - a compatible field type.
	 * @return The field accessor.
	 */
	public static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType) {
		return getField(target, name, fieldType, 0);
	}
	
	/**
	 * Retrieve a field accessor for a specific field type and name.
	 * @param target - the target type.
	 * @param fieldType - a compatible field type.
	 * @param index - the number of compatible fields to skip.
	 * @return The field accessor.
	 */
	public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
		return getField(target, null, fieldType, index);
	}
	
	/**
	 * Retrieve a field accessor for a specific field type and name.
	 * @param nmsTargetClass - the net.minecraft.server class name.
	 * @param fieldType - a compatible field type.
	 * @param index - the number of compatible fields to skip.
	 * @return The field accessor.
	 */
	public static <T> FieldAccessor<T> getField(String nmsTargetClass, Class<T> fieldType, int index) {
		return getField(getMinecraftClass(nmsTargetClass), fieldType, index);
	}
	
	// Common method
	private static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType, int index) {
		for (final Field field : target.getDeclaredFields()) {
			if ((name == null || field.getName().equals(name)) && 
					fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
				field.setAccessible(true);
				
				// A function for retrieving a specific field value
				return new FieldAccessor<T>() {
					@SuppressWarnings("unchecked")
					@Override
					public T get(Object target) {
						try {
							return (T) field.get(target);
						} catch (IllegalAccessException e) {
							throw new RuntimeException("Cannot access reflection.", e);
						}
					}
					
					@Override
					public void set(Object target, Object value) {
						try {
							field.set(target, value);
						} catch (IllegalAccessException e) {
							throw new RuntimeException("Cannot access reflection.", e);
						}
					}
					
					@Override
					public boolean hasField(Object target) {
						// target instanceof DeclaringClass
						return field.getDeclaringClass().isAssignableFrom(target.getClass());
					}
				};
			}
		}
		
		// Search in parent classes
		if (target.getSuperclass() != null)
			return getField(target.getSuperclass(), name, fieldType, index);
		throw new IllegalArgumentException("Cannot find field with type " + fieldType);
	}
	
    /**
     * Search for the first publically and privately defined method of the given name and parameter count.
     * @param clazz - a class to start with.
     * @param methodName - the method name, or NULL to skip.
     * @param params - the expected parameters.
     * @return An object that invokes this specific method.
     * @throws IllegalStateException If we cannot find this method.
     */
    public static MethodInvoker getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        for (final Method method : clazz.getDeclaredMethods()) {
            if ((methodName == null || method.getName().equals(methodName)) &&
                 Arrays.equals(method.getParameterTypes(), params)) {
                
                method.setAccessible(true);
                return new MethodInvoker() {
                	@Override
                	public Object invoke(Object target, Object... arguments) {
                		try {
							return method.invoke(target, arguments);
						} catch (Exception e) {
							throw new RuntimeException("Cannot invoke method " + method, e);
						}
                	}
                };
            }
        }
        // Search in every superclass
        if (clazz.getSuperclass() != null)
            return getMethod(clazz.getSuperclass(), methodName, params);
        throw new IllegalStateException(String.format(
            "Unable to find method %s (%s).", methodName, Arrays.asList(params)));
    }
	
	/**
	 * Retrieve a class in the net.minecraft.server.VERSION.* package.
	 * @param name - the name of the class, excluding the package.
	 * @throws IllegalArgumentException If the class doesn't exist.
	 */
	public static Class<?> getMinecraftClass(String name) {
		try {
			return Class.forName(NMS_PREFIX + "." + name);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Cannot find nms." + name, e);
		}
	}
	
	/**
	 * Retrieve a class in the org.bukkit.craftbukkit.VERSION.* package.
	 * @param name - the name of the class, excluding the package.
	 * @throws IllegalArgumentException If the class doesn't exist.
	 */
	public static Class<?> getCraftBukkitClass(String name) {
		try {
			return Class.forName(OBC_PREFIX + "." + name);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Cannot find obc." + name, e);
		}
	}
	
	/**
	 * An interface for invoking a specific method. 
	 */
	public interface MethodInvoker {
		/**
		 * Invoke a method on a specific target object.
		 * @param target - the target object, or NULL for a static method.
		 * @param arguments - the arguments to pass to the method.
		 * @return The return value, or NULL if is void.
		 */
		public Object invoke(Object target, Object... arguments); 
	}
	
	/**
	 * An interface for retrieving the field content.
	 * @param <T> - field type.
	 */
	public interface FieldAccessor<T> {
		/**
		 * Retrieve the content of a field.
		 * @param target - the target object, or NULL for a static field.
		 * @return The value of the field.
		 */
		public T get(Object target);
		
		/**
		 * Set the content of a field.
		 * @param target - the target object, or NULL for a static field.
		 * @param value - the new value of the field.
		 */
		public void set(Object target, Object value);
		
		/**
		 * Determine if the given object has this field.
		 * @param target - the object to test.
		 * @return TRUE if it does, FALSE otherwise.
		 */
		public boolean hasField(Object target);
	}
}
