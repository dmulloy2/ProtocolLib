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

package com.comphenix.protocol.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.error.RethrowErrorReporter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

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
	private PacketType type;
	
	// Used to unwrap Bukkit objects
	private List<Unwrapper> unwrappers;
	
	// Parameters that need to be unwrapped
	private Unwrapper[] paramUnwrapper;
	
	private PacketConstructor(Constructor<?> constructorMethod) {
		this.constructorMethod = constructorMethod;
		this.unwrappers = Lists.newArrayList((Unwrapper) new BukkitUnwrapper(new RethrowErrorReporter() ));
		this.unwrappers.addAll(BukkitConverters.getUnwrappers()); 
	}
	
	private PacketConstructor(PacketType type, Constructor<?> constructorMethod, List<Unwrapper> unwrappers, Unwrapper[] paramUnwrapper) {
		this.type = type;
		this.constructorMethod = constructorMethod;
		this.unwrappers = unwrappers;
		this.paramUnwrapper = paramUnwrapper;
	}
	
	public ImmutableList<Unwrapper> getUnwrappers() {
		return ImmutableList.copyOf(unwrappers);
	}
	
	/**
	 * Retrieve the id of the packets this constructor creates.
	 * <p>
	 * Deprecated: Use {@link #getType()} instead.
	 * @return The ID of the packets this constructor will create.
	 */
	@Deprecated
	public int getPacketID() {
		return type.getLegacyId();
	}
	
	/**
	 * Retrieve the type of the packets this constructor creates.
	 * @return The type of the created packets.
	 */
	public PacketType getType() {
		return type;
	}
	
	/**
	 * Return a copy of the current constructor with a different list of unwrappers.
	 * @param unwrappers - list of unwrappers that convert Bukkit wrappers into the equivalent NMS classes.
	 * @return A constructor with a different set of unwrappers.
	 */
	public PacketConstructor withUnwrappers(List<Unwrapper> unwrappers) {
		return new PacketConstructor(type, constructorMethod, unwrappers, paramUnwrapper);
	}

	/**
	 * Create a packet constructor that creates packets using the given ID.
	 * <p>
	 * Note that if you pass a Class<?> as a value, it will use its type directly.
	 * <p>
	 * Deprecated: Use {@link #withPacket(PacketType, Object[])} instead.
	 * @param id - legacy (1.6.4) packet ID.
	 * @param values - the values that will match each parameter in the desired constructor.
	 * @return A packet constructor with these types.
	 * @throws IllegalArgumentException If no packet constructor could be created with these types.
	 */
	@Deprecated
	public PacketConstructor withPacket(int id, Object[] values) {
		return withPacket(PacketType.findLegacy(id), values);
	}
	
	/**
	 * Create a packet constructor that creates packets using the given types.
	 * <p>
	 * Note that if you pass a Class<?> as a value, it will use its type directly.
	 * @param type - the type of the packet to create.
	 * @param values - the values that will match each parameter in the desired constructor.
	 * @return A packet constructor with these types.
	 * @throws IllegalArgumentException If no packet constructor could be created with these types.
	 */
	public PacketConstructor withPacket(PacketType type, Object[] values) {
		Class<?>[] types = new Class<?>[values.length];
		Throwable lastException = null;
		Unwrapper[] paramUnwrapper = new Unwrapper[values.length];		
		
		for (int i = 0; i < types.length; i++) {
			// Default type
			if (values[i] != null) {
				types[i] = PacketConstructor.getClass(values[i]);
				
				for (Unwrapper unwrapper : unwrappers) {
					Object result = null;
					
					try {
						result = unwrapper.unwrapItem(values[i]);
					} catch (OutOfMemoryError e) {
						throw e;
					} catch (ThreadDeath e) {
						throw e;
					} catch (Throwable e) {
						lastException = e;
					}
					
					// Update type we're searching for
					if (result != null) {
						types[i] = PacketConstructor.getClass(result);
						paramUnwrapper[i] = unwrapper;
						break;
					}
				}
			
			} else {
				// Try it
				types[i] = Object.class;
			}
		}
		Class<?> packetType = PacketRegistry.getPacketClassFromType(type, true);
		
		if (packetType == null)
			throw new IllegalArgumentException("Could not find a packet by the type " + type);
		
		// Find the correct constructor
		for (Constructor<?> constructor : packetType.getConstructors()) {
			Class<?>[] params = constructor.getParameterTypes();

			if (isCompatible(types, params)) {
				// Right, we've found our type
				return new PacketConstructor(type, constructor, unwrappers, paramUnwrapper);
			}
		}
		throw new IllegalArgumentException("No suitable constructor could be found.", lastException);
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
			// Convert types that needs to be converted
			for (int i = 0; i < values.length; i++) {
				if (paramUnwrapper[i] != null) {
					values[i] = paramUnwrapper[i].unwrapItem(values[i]);
				}
			}
			
			Object nmsPacket = constructorMethod.newInstance(values);
			return new PacketContainer(type, nmsPacket);
			
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
				Class<?> inputType = types[i];
				Class<?> paramType = params[i];
				
				// The input type is always wrapped
				if (!inputType.isPrimitive() && paramType.isPrimitive()) {
					// Wrap it
					paramType = Primitives.wrap(paramType);
				}
				
				// Compare assignability
				if (!paramType.isAssignableFrom(inputType)) {
					return false;
				}
			}
			
			return true;
		}
		
		// Parameter count must match
		return false;
	}
	
	/**
	 * Retrieve the class of an object, or just the class if it already is a class object.
	 * @param obj - the object.
	 * @return The class of an object.
	 */
	public static Class<?> getClass(Object obj) {
		if (obj instanceof Class)
			return (Class<?>) obj;
		return obj.getClass();
	}

	/**
	 * Represents a unwrapper for a constructor parameter.
	 * 
	 * @author Kristian
	 */
	public static interface Unwrapper {
		/**
		 * Convert the given wrapped object to the equivalent net.minecraft.server object.
		 * <p>
		 * Note that we may pass in a class instead of object - in that case, the unwrapper should 
		 * return the equivalent NMS class.
		 * @param wrappedObject - wrapped object or class.
		 * @return The equivalent net.minecraft.server object or class.
		 */
		public Object unwrapItem(Object wrappedObject);
	}
}
