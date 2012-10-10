package com.comphenix.protocol.injector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.comphenix.protocol.injector.PacketConstructor.Unwrapper;
import com.comphenix.protocol.reflect.instances.DefaultInstances;

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
	
	private static Map<Class<?>, Method> cache = new ConcurrentHashMap<Class<?>, Method>();

	@SuppressWarnings("unchecked")
	@Override
	public Object unwrapItem(Object wrappedObject) {

		// Special case
		if (wrappedObject instanceof Collection) {
			return handleCollection((Collection<Object>) wrappedObject);
		}
		
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
