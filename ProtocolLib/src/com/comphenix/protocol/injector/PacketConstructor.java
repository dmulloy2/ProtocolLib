package com.comphenix.protocol.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.Packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * A packet constructor that uses an internal Minecraft.
 * @author Kristian
 *
 */
public class PacketConstructor {

	/**
	 * A packet constructor that automatically converts Bukkit types to their NMS conterpart. 
	 * <p>
	 * Remember to call withPacket().
	 */
	public static PacketConstructor DEFAULT = new PacketConstructor(null);
	
	// The constructor method that's actually responsible for creating the packet
	private Constructor<?> constructorMethod;
	
	// The packet ID
	private int packetID;
	
	// Used to unwrap Bukkit objects
	private List<Unwrapper> unwrappers;
	
	private PacketConstructor(Constructor<?> constructorMethod) {
		this.constructorMethod = constructorMethod;
		this.unwrappers = Lists.newArrayList((Unwrapper) new BukkitUnwrapper());
	}
	
	private PacketConstructor(int packetID, Constructor<?> constructorMethod, List<Unwrapper> unwrappers) {
		this.packetID = packetID;
		this.constructorMethod = constructorMethod;
		this.unwrappers = unwrappers;
	}
	
	public ImmutableList<Unwrapper> getUnwrappers() {
		return ImmutableList.copyOf(unwrappers);
	}
	
	/**
	 * Retrieve the id of the packets this constructor creates.
	 * @return The ID of the packets this constructor will create.
	 */
	public int getPacketID() {
		return packetID;
	}
	
	/**
	 * Return a copy of the current constructor with a different list of unwrappers.
	 * @param unwrappers - list of unwrappers that convert Bukkit wrappers into the equivalent NMS classes.
	 * @return A constructor with a different set of unwrappers.
	 */
	public PacketConstructor withUnwrappers(List<Unwrapper> unwrappers) {
		return new PacketConstructor(packetID, constructorMethod, unwrappers);
	}

	/**
	 * Create a packet constructor that creates packets using the given types.
	 * @param id - packet ID.
	 * @param types - types to create.
	 * @return A packet constructor with these types.
	 * @throws IllegalArgumentException If no packet constructor could be created with these types.
	 */
	public PacketConstructor withPacket(int id, Object[] values) {
		
		Class<?>[] types = new Class<?>[values.length];
		
		for (int i = 0; i < types.length; i++) {
			// Default type
			if (values[i] != null) {
				types[i] = values[i].getClass();
				
				for (Unwrapper unwrapper : unwrappers) {
					Object result = unwrapper.unwrapItem(values[i]);
					
					// Update type we're searching for
					if (result != null) {
						types[i] = result.getClass();
						break;
					}
				}
			
			} else {
				// Try it
				types[i] = Object.class;
			}
		}
		
		Class<?> packetType = MinecraftRegistry.getPacketClassFromID(id);
		
		if (packetType == null)
			throw new IllegalArgumentException("Could not find a packet by the id " + id);
		
		// Find the correct constructor
		for (Constructor<?> constructor : packetType.getConstructors()) {
			Class<?>[] params = constructor.getParameterTypes();

			if (isCompatible(types, params)) {
				// Right, we've found our type
				return new PacketConstructor(id, constructor, unwrappers);
			}
		}
		
		throw new IllegalArgumentException("No suitable constructor could be found.");
	}
	
	/**
	 * Construct a packet using the special builtin Minecraft constructors.
	 * @param values - values containing Bukkit wrapped items to pass to Minecraft.
	 * @return The created packet.
	 * @throws FieldAccessException Failure due to a security limitation.
	 * @throws IllegalArgumentException Arguments doesn't match the constructor.
	 * @throws RuntimeException Minecraft threw an exception.
	 */
	public PacketContainer createPacket(Object... values) throws FieldAccessException {
		
		try {
			// Convert types
			for (int i = 0; i < values.length; i++) {
				for (Unwrapper unwrapper : unwrappers) {
					Object converted = unwrapper.unwrapItem(values[i]);
					
					if (converted != null) {
						values[i] = converted;
						break;
					}
				}
			}
			
			Packet nmsPacket = (Packet) constructorMethod.newInstance(values);
			return new PacketContainer(packetID, nmsPacket);
			
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (InstantiationException e) {
			throw new FieldAccessException("Cannot construct an abstract packet.", e);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot construct packet due to a security limitation.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Minecraft error.", e);
		}
	}
	
	// Determine if a method with the types 'params' can be called with 'types'
	private static boolean isCompatible(Class<?>[] types, Class<?>[] params) {
		
		// Determine if the types are similar
		if (params.length == types.length) {
			for (int i = 0; i < params.length; i++) {
				if (!params[i].isAssignableFrom(types[i])) {
					return false;
				}
			}
			
			return true;
		}
		
		// Parameter count must match
		return false;
	}

	public static class BukkitUnwrapper implements Unwrapper {
	
		private static Map<Class<?>, Method> cache = new ConcurrentHashMap<Class<?>, Method>();
	
		@Override
		public Object unwrapItem(Object wrappedObject) {

			Class<?> currentClass = wrappedObject.getClass();
			Method cachedMethod = initializeCache(currentClass);
			
			try {
				// Retrieve the handle
				if (cachedMethod != null)
					return cachedMethod.invoke(wrappedObject);
				else
					return null;
				
			} catch (IllegalArgumentException e) {
				// Impossible
				return null;
			} catch (IllegalAccessException e) {
				return null;
			} catch (InvocationTargetException e) {
				// This is REALLY bad
				throw new RuntimeException("Minecraft error.", e);
			}
		}
		
		private Method initializeCache(Class<?> type) {
			
			// See if we're already determined this
			if (cache.containsKey(type)) {
				// We will never remove from the cache, so this ought to be thread safe
				return cache.get(type);
			}
			
			try {
				Method find = type.getMethod("getHandle");
				
				// It's thread safe, as getMethod should return the same handle 
				cache.put(type, find);
				return find;
				
			} catch (SecurityException e) {
				return null;
			} catch (NoSuchMethodException e) {
				return null;
			}
		}
	}
	
	public static interface Unwrapper {
		public Object unwrapItem(Object wrappedObject);
	}
}
