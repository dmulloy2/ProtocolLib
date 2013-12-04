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
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
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
	private static BukkitUnwrapper DEFAULT;

	public static final ReportType REPORT_ILLEGAL_ARGUMENT = new ReportType("Illegal argument.");
	public static final ReportType REPORT_SECURITY_LIMITATION = new ReportType("Security limitation.");
	public static final ReportType REPORT_CANNOT_FIND_UNWRAP_METHOD = new ReportType("Cannot find method.");
	
	public static final ReportType REPORT_CANNOT_READ_FIELD_HANDLE = new ReportType("Cannot read field 'handle'.");
	
	private static Map<Class<?>, Unwrapper> unwrapperCache = new ConcurrentHashMap<Class<?>, Unwrapper>();
	
	// The current error reporter
	private final ErrorReporter reporter;
	
	/**
	 * Retrieve the default instance of the Bukkit unwrapper.
	 * @return The default instance.
	 */
	public static BukkitUnwrapper getInstance() {
		ErrorReporter currentReporter = ProtocolLibrary.getErrorReporter();
		
		// Also recreate the unwrapper if the error reporter has changed
		if (DEFAULT == null || DEFAULT.reporter != currentReporter) {
			DEFAULT = new BukkitUnwrapper(currentReporter);
		}
		return DEFAULT;
	}
	
	/**
	 * Construct a new Bukkit unwrapper with ProtocolLib's default error reporter.
	 */
	public BukkitUnwrapper() {
		this(ProtocolLibrary.getErrorReporter());
	}
	
	/**
	 * Construct a new Bukkit unwrapper with the given error reporter.
	 * @param reporter - the error reporter to use.
	 */
	public BukkitUnwrapper(ErrorReporter reporter) {
		 this.reporter = reporter;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object unwrapItem(Object wrappedObject) {
		// Special case
		if (wrappedObject == null) 
			return null;
		Class<?> currentClass = PacketConstructor.getClass(wrappedObject);
		
		// No need to unwrap primitives
		if (currentClass.isPrimitive() || currentClass.equals(String.class))
			return null;
		
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
	private Unwrapper getSpecificUnwrapper(final Class<?> type) {
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
						if (wrappedObject instanceof Class)
							return checkClass((Class<?>) wrappedObject, type, find.getReturnType());
						return find.invoke(wrappedObject);
						
					} catch (IllegalArgumentException e) {
						reporter.reportDetailed(this, 
								Report.newBuilder(REPORT_ILLEGAL_ARGUMENT).error(e).callerParam(wrappedObject, find)
						);
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
			reporter.reportDetailed(this, 
					Report.newBuilder(REPORT_SECURITY_LIMITATION).error(e).callerParam(type)
			);
		} catch (NoSuchMethodException e) {
			// Try getting the field unwrapper too
			Unwrapper fieldUnwrapper = getFieldUnwrapper(type);
			
			if (fieldUnwrapper != null)
				return fieldUnwrapper;
			else
				reporter.reportDetailed(this, 
						Report.newBuilder(REPORT_CANNOT_FIND_UNWRAP_METHOD).error(e).callerParam(type));
		}
		
		// Default method
		return null;
	}
	
	/**
	 * Retrieve a cached unwrapper using the handle field.
	 * @param type - a cached field unwrapper.
	 * @return The cached field unwrapper.
	 */
	private Unwrapper getFieldUnwrapper(final Class<?> type) {
		final Field find = FieldUtils.getField(type, "handle", true);
		
		// See if we succeeded
		if (find != null) {
			Unwrapper fieldUnwrapper = new Unwrapper() {
				@Override
				public Object unwrapItem(Object wrappedObject) {
					try {
						if (wrappedObject instanceof Class)
							return checkClass((Class<?>) wrappedObject, type, find.getType());
						return FieldUtils.readField(find, wrappedObject, true);
					} catch (IllegalAccessException e) {
						reporter.reportDetailed(this, 
								Report.newBuilder(REPORT_CANNOT_READ_FIELD_HANDLE).error(e).callerParam(wrappedObject, find)
						);
						return null;
					}
				}
			};
			
			unwrapperCache.put(type, fieldUnwrapper);
			return fieldUnwrapper;
			
		} else {
			// Inform about this too
			reporter.reportDetailed(this, 
					Report.newBuilder(REPORT_CANNOT_READ_FIELD_HANDLE).callerParam(find)
			);
			return null;
		}
	}

	private static Class<?> checkClass(Class<?> input, Class<?> expected, Class<?> result) {
		if (expected.isAssignableFrom(input)) {
			return result;
		}
		return null;
	}
}
