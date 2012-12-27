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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.injector.PacketConstructor.Unwrapper;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.google.common.primitives.Primitives;

/**
 * Represents an object capable of converting wrapped Bukkit objects into NMS objects.
 * <p>
 * Typical conversions include:
 * <ul>
 * <li>org.bukkit.entity.Player -> net.minecraft.server.EntityPlayer</li>
 * <li>org.bukkit.World -> net.minecraft.server.WorldServer</li>
 * </ul>
 * 
 * @author Kristian
 */
public class BukkitUnwrapper implements Unwrapper {
	private static Map<Class<?>, Unwrapper> unwrapperCache = new ConcurrentHashMap<Class<?>, Unwrapper>();
	
	@SuppressWarnings("unchecked")
	@Override
	public Object unwrapItem(Object wrappedObject) {
		// Special case
		if (wrappedObject == null) 
			return null;
		Class<?> currentClass = wrappedObject.getClass();
		
		// Next, check for types that doesn't have a getHandle()
		if (wrappedObject instanceof Collection) {
			return handleCollection((Collection<Object>) wrappedObject);
		} else if (Primitives.isWrapperType(currentClass) || wrappedObject instanceof String) {
			return null;
		}
		
		Unwrapper specificUnwrapper = getSpecificUnwrapper(currentClass);
		
		// Retrieve the handle
		if (specificUnwrapper != null)
			return specificUnwrapper.unwrapItem(wrappedObject);
		else
			return null;
	}
	
	// Handle a collection of items
	private Object handleCollection(Collection<Object> wrappedObject) {
		
		@SuppressWarnings("unchecked")
		Collection<Object> copy = DefaultInstances.DEFAULT.getDefault(wrappedObject.getClass());
		
		if (copy != null) {
			// Unwrap every element
			for (Object element : wrappedObject) {
				copy.add(unwrapItem(element));
			}
			return copy;
			
		} else {
			// Impossible
			return null;
		}
	}
	
	/**
	 * Retrieve a cached class unwrapper for the given class.
	 * @param type - the type of the class.
	 * @return An unwrapper for the given class.
	 */
	private Unwrapper getSpecificUnwrapper(Class<?> type) {
		// See if we're already determined this
		if (unwrapperCache.containsKey(type)) {
			// We will never remove from the cache, so this ought to be thread safe
			return unwrapperCache.get(type);
		}
		
		try {
			final Method find = type.getMethod("getHandle");
			
			// It's thread safe, as getMethod should return the same handle 
			Unwrapper methodUnwrapper = new Unwrapper() {
				@Override
				public Object unwrapItem(Object wrappedObject) {
					
					try {
						return find.invoke(wrappedObject);
						
					} catch (IllegalArgumentException e) {
						ProtocolLibrary.getErrorReporter().reportDetailed(
								this, "Illegal argument.", e, wrappedObject, find);
					} catch (IllegalAccessException e) {
						// Should not occur either
						return null;
					} catch (InvocationTargetException e) {
						// This is really bad
						throw new RuntimeException("Minecraft error.", e);
					}
					
					return null;
				}
			};
			
			unwrapperCache.put(type, methodUnwrapper);
			return methodUnwrapper;
			
		} catch (SecurityException e) {
			ProtocolLibrary.getErrorReporter().reportDetailed(this, "Security limitation.", e, type.getName());
		} catch (NoSuchMethodException e) {
			// Try getting the field unwrapper too
			Unwrapper fieldUnwrapper = getFieldUnwrapper(type);
			
			if (fieldUnwrapper != null)
				return fieldUnwrapper;
			else
				ProtocolLibrary.getErrorReporter().reportDetailed(this, "Cannot find method.", e, type.getName());
		}
		
		// Default method
		return null;
	}
	
	/**
	 * Retrieve a cached unwrapper using the handle field.
	 * @param type - a cached field unwrapper.
	 * @return The cached field unwrapper.
	 */
	private Unwrapper getFieldUnwrapper(Class<?> type) {
		final Field find = FieldUtils.getField(type, "handle", true);
		
		// See if we succeeded
		if (find != null) {
			Unwrapper fieldUnwrapper = new Unwrapper() {
				@Override
				public Object unwrapItem(Object wrappedObject) {
					try {
						return FieldUtils.readField(find, wrappedObject, true);
					} catch (IllegalAccessException e) {
						ProtocolLibrary.getErrorReporter().reportDetailed(
								this, "Cannot read field 'handle'.", e, wrappedObject, find.getName());
						return null;
					}
				}
			};
			
			unwrapperCache.put(type, fieldUnwrapper);
			return fieldUnwrapper;
			
		} else {
			// Inform about this too
			ProtocolLibrary.getErrorReporter().reportDetailed(
					this, "Could not find field 'handle'.", 
					new Exception("Unable to find 'handle'"), type.getName());
			return null;
		}
	}
}
