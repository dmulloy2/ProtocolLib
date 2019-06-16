package com.comphenix.protocol.reflect;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities for working with fields by reflection. Adapted and refactored from
 * the dormant [reflect] Commons sandbox component.
 * <p>
 * The ability is provided to break the scoping restrictions coded by the
 * programmer. This can allow fields to be changed that shouldn't be. This
 * facility should be used with care.
 * 
 * @author Apache Software Foundation
 * @author Matt Benson
 * @since 2.5
 * @version $Id: FieldUtils.java 1057009 2011-01-09 19:48:06Z niallp $
 */
@SuppressWarnings("rawtypes")
public class FieldUtils {

	/**
	 * FieldUtils instances should NOT be constructed in standard programming.
	 * <p>
	 * This constructor is public to permit tools that require a JavaBean
	 * instance to operate.
	 */
	public FieldUtils() {
		super();
	}

	/**
	 * Gets an accessible <code>Field</code> by name respecting scope.
	 * Superclasses/interfaces will be considered.
	 * 
	 * @param cls the class to reflect, must not be null
	 * @param fieldName the field name to obtain
	 * @return the Field object
	 * @throws IllegalArgumentException if the class or field name is null
	 */
	public static Field getField(Class cls, String fieldName) {
		Field field = getField(cls, fieldName, false);
		MemberUtils.setAccessibleWorkaround(field);
		return field;
	}

	/**
	 * Gets an accessible <code>Field</code> by name breaking scope if
	 * requested. Superclasses/interfaces will be considered.
	 * 
	 * @param cls the class to reflect, must not be null
	 * @param fieldName the field name to obtain
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method. <code>False</code> will
	 *            only match public fields.
	 * @return the Field object
	 * @throws IllegalArgumentException if the class or field name is null
	 */
	public static Field getField(final Class cls, String fieldName, boolean forceAccess) {
		if (cls == null) {
			throw new IllegalArgumentException("The class must not be null");
		}
		if (fieldName == null) {
			throw new IllegalArgumentException("The field name must not be null");
		}
		// Sun Java 1.3 has a bugged implementation of getField hence we write
		// the
		// code ourselves

		// getField() will return the Field object with the declaring class
		// set correctly to the class that declares the field. Thus requesting
		// the
		// field on a subclass will return the field from the superclass.
		//
		// priority order for lookup:
		// searchclass private/protected/package/public
		// superclass protected/package/public
		// private/different package blocks access to further superclasses
		// implementedinterface public

		// check up the superclass hierarchy
		for (Class acls = cls; acls != null; acls = acls.getSuperclass()) {
			try {
				Field field = acls.getDeclaredField(fieldName);
				// getDeclaredField checks for non-public scopes as well
				// and it returns accurate results
				if (!Modifier.isPublic(field.getModifiers())) {
					if (forceAccess) {
						field.setAccessible(true);
					} else {
						continue;
					}
				}
				return field;
			} catch (NoSuchFieldException ex) {
				// ignore
			}
		}
		// check the public interface case. This must be manually searched for
		// in case there is a public supersuperclass field hidden by a
		// private/package
		// superclass field.
		Field match = null;
		for (Iterator intf = getAllInterfaces(cls).iterator(); intf.hasNext();) {
			try {
				Field test = ((Class) intf.next()).getField(fieldName);
				if (match != null) {
					throw new IllegalArgumentException("Reference to field " + fieldName
							+ " is ambiguous relative to " + cls
							+ "; a matching field exists on two or more implemented interfaces.");
				}
				match = test;
			} catch (NoSuchFieldException ex) {
				// ignore
			}
		}
		return match;
	}
	
    /**
     * <p>Gets a <code>List</code> of all interfaces implemented by the given
     * class and its superclasses.</p>
     *
     * <p>The order is determined by looking through each interface in turn as
     * declared in the source file and following its hierarchy up. Then each
     * superclass is considered in the same way. Later duplicates are ignored,
     * so the order is maintained.</p>
     *
     * @param cls  the class to look up, may be <code>null</code>
     * @return the <code>List</code> of interfaces in order,
     *  <code>null</code> if null input
     */
    private static List getAllInterfaces(Class cls) {
        if (cls == null) {
            return null;
        }
        List<Class> list = new ArrayList<Class>();
        
        while (cls != null) {
            Class[] interfaces = cls.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (list.contains(interfaces[i]) == false) {
                    list.add(interfaces[i]);
                }
                List superInterfaces = getAllInterfaces(interfaces[i]);
                for (Iterator it = superInterfaces.iterator(); it.hasNext();) {
                    Class intface = (Class) it.next();
                    if (list.contains(intface) == false) {
                        list.add(intface);
                    }
                }
            }
            cls = cls.getSuperclass();
        }
        return list;
    }

	/**
	 * Read an accessible static Field.
	 * 
	 * @param field to read
	 * @return the field value
	 * @throws IllegalArgumentException if the field is null or not static
	 * @throws IllegalAccessException if the field is not accessible
	 */
	public static Object readStaticField(Field field) throws IllegalAccessException {
		return readStaticField(field, false);
	}

	/**
	 * Read a static Field.
	 * 
	 * @param field to read
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method.
	 * @return the field value
	 * @throws IllegalArgumentException if the field is null or not static
	 * @throws IllegalAccessException if the field is not made accessible
	 */
	public static Object readStaticField(Field field, boolean forceAccess)
			throws IllegalAccessException {
		if (field == null) {
			throw new IllegalArgumentException("The field must not be null");
		}
		if (!Modifier.isStatic(field.getModifiers())) {
			throw new IllegalArgumentException("The field '" + field.getName() + "' is not static");
		}
		return readField(field, (Object) null, forceAccess);
	}

	/**
	 * Read the named public static field. Superclasses will be considered.
	 * 
	 * @param cls the class to reflect, must not be null
	 * @param fieldName the field name to obtain
	 * @return the value of the field
	 * @throws IllegalArgumentException if the class or field name is null
	 * @throws IllegalAccessException if the field is not accessible
	 */
	public static Object readStaticField(Class cls, String fieldName) throws IllegalAccessException {
		return readStaticField(cls, fieldName, false);
	}

	/**
	 * Read the named static field. Superclasses will be considered.
	 * 
	 * @param cls the class to reflect, must not be null
	 * @param fieldName the field name to obtain
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method. <code>False</code> will
	 *            only match public fields.
	 * @return the Field object
	 * @throws IllegalArgumentException if the class or field name is null
	 * @throws IllegalAccessException if the field is not made accessible
	 */
	public static Object readStaticField(Class cls, String fieldName, boolean forceAccess)
			throws IllegalAccessException {
		Field field = getField(cls, fieldName, forceAccess);
		if (field == null) {
			throw new IllegalArgumentException("Cannot locate field " + fieldName + " on " + cls);
		}
		// already forced access above, don't repeat it here:
		return readStaticField(field, false);
	}

	/**
	 * Read an accessible Field.
	 * 
	 * @param field the field to use
	 * @param target the object to call on, may be null for static fields
	 * @return the field value
	 * @throws IllegalArgumentException if the field is null
	 * @throws IllegalAccessException if the field is not accessible
	 */
	public static Object readField(Field field, Object target) throws IllegalAccessException {
		return readField(field, target, false);
	}

	/**
	 * Read a Field.
	 * 
	 * @param field the field to use
	 * @param target the object to call on, may be null for static fields
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method.
	 * @return the field value
	 * @throws IllegalArgumentException if the field is null
	 * @throws IllegalAccessException if the field is not made accessible
	 */
	public static Object readField(Field field, Object target, boolean forceAccess) throws IllegalAccessException {
		if (field == null)
			throw new IllegalArgumentException("The field must not be null");
		
		if (forceAccess && !field.isAccessible()) {
			field.setAccessible(true);
		} else {
			MemberUtils.setAccessibleWorkaround(field);
		}
		return field.get(target);
	}

	/**
	 * Read the named public field. Superclasses will be considered.
	 * 
	 * @param target the object to reflect, must not be null
	 * @param fieldName the field name to obtain
	 * @return the value of the field
	 * @throws IllegalArgumentException if the class or field name is null
	 * @throws IllegalAccessException if the named field is not public
	 */
	public static Object readField(Object target, String fieldName) throws IllegalAccessException {
		return readField(target, fieldName, false);
	}

	/**
	 * Read the named field. Superclasses will be considered.
	 * 
	 * @param target the object to reflect, must not be null
	 * @param fieldName the field name to obtain
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method. <code>False</code> will
	 *            only match public fields.
	 * @return the field value
	 * @throws IllegalArgumentException if the class or field name is null
	 * @throws IllegalAccessException if the named field is not made accessible
	 */
	public static Object readField(Object target, String fieldName, boolean forceAccess)
			throws IllegalAccessException {
		if (target == null) {
			throw new IllegalArgumentException("target object must not be null");
		}
		Class cls = target.getClass();
		Field field = getField(cls, fieldName, forceAccess);
		if (field == null) {
			throw new IllegalArgumentException("Cannot locate field " + fieldName + " on " + cls);
		}
		// already forced access above, don't repeat it here:
		return readField(field, target);
	}

	/**
	 * Write a public static Field.
	 * 
	 * @param field to write
	 * @param value to set
	 * @throws IllegalArgumentException if the field is null or not static
	 * @throws IllegalAccessException if the field is not public or is final
	 */
	public static void writeStaticField(Field field, Object value) throws IllegalAccessException {
		writeStaticField(field, value, false);
	}

	/**
	 * Write a static Field.
	 * 
	 * @param field to write
	 * @param value to set
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method. <code>False</code> will
	 *            only match public fields.
	 * @throws IllegalArgumentException if the field is null or not static
	 * @throws IllegalAccessException if the field is not made accessible or is
	 *             final
	 */
	public static void writeStaticField(Field field, Object value, boolean forceAccess)
			throws IllegalAccessException {
		if (field == null) {
			throw new IllegalArgumentException("The field must not be null");
		}
		if (!Modifier.isStatic(field.getModifiers())) {
			throw new IllegalArgumentException("The field '" + field.getName() + "' is not static");
		}
		writeField(field, (Object) null, value, forceAccess);
	}

	/**
	 * Write a named public static Field. Superclasses will be considered.
	 * 
	 * @param cls Class on which the Field is to be found
	 * @param fieldName to write
	 * @param value to set
	 * @throws IllegalArgumentException if the field cannot be located or is not
	 *             static
	 * @throws IllegalAccessException if the field is not public or is final
	 */
	public static void writeStaticField(Class cls, String fieldName, Object value)
			throws IllegalAccessException {
		writeStaticField(cls, fieldName, value, false);
	}

	/**
	 * Write a named static Field. Superclasses will be considered.
	 * 
	 * @param cls Class on which the Field is to be found
	 * @param fieldName to write
	 * @param value to set
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method. <code>False</code> will
	 *            only match public fields.
	 * @throws IllegalArgumentException if the field cannot be located or is not
	 *             static
	 * @throws IllegalAccessException if the field is not made accessible or is
	 *             final
	 */
	public static void writeStaticField(Class cls, String fieldName, Object value,
			boolean forceAccess) throws IllegalAccessException {
		Field field = getField(cls, fieldName, forceAccess);
		if (field == null) {
			throw new IllegalArgumentException("Cannot locate field " + fieldName + " on " + cls);
		}
		// already forced access above, don't repeat it here:
		writeStaticField(field, value);
	}

	public static void writeStaticFinalField(Class<?> clazz, String fieldName, Object value, boolean forceAccess) throws Exception {
		Field field = getField(clazz, fieldName, forceAccess);
		if (field == null) {
			throw new IllegalArgumentException("Cannot locate field " + fieldName + " in " + clazz);
		}

		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.setAccessible(true);
		field.set(null, value);
	}

	/**
	 * Write an accessible field.
	 * 
	 * @param field to write
	 * @param target the object to call on, may be null for static fields
	 * @param value to set
	 * @throws IllegalArgumentException if the field is null
	 * @throws IllegalAccessException if the field is not accessible or is final
	 */
	public static void writeField(Field field, Object target, Object value)
			throws IllegalAccessException {
		writeField(field, target, value, false);
	}

	/**
	 * Write a field.
	 * 
	 * @param field to write
	 * @param target the object to call on, may be null for static fields
	 * @param value to set
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method. <code>False</code> will
	 *            only match public fields.
	 * @throws IllegalArgumentException if the field is null
	 * @throws IllegalAccessException if the field is not made accessible or is
	 *             final
	 */
	public static void writeField(Field field, Object target, Object value, boolean forceAccess)
			throws IllegalAccessException {
		if (field == null) {
			throw new IllegalArgumentException("The field must not be null");
		}
		if (forceAccess && !field.isAccessible()) {
			field.setAccessible(true);
		} else {
			MemberUtils.setAccessibleWorkaround(field);
		}
		field.set(target, value);
	}

	/**
	 * Write a public field. Superclasses will be considered.
	 * 
	 * @param target the object to reflect, must not be null
	 * @param fieldName the field name to obtain
	 * @param value to set
	 * @throws IllegalArgumentException if <code>target</code> or
	 *             <code>fieldName</code> is null
	 * @throws IllegalAccessException if the field is not accessible
	 */
	public static void writeField(Object target, String fieldName, Object value)
			throws IllegalAccessException {
		writeField(target, fieldName, value, false);
	}

	/**
	 * Write a field. Superclasses will be considered.
	 * 
	 * @param target the object to reflect, must not be null
	 * @param fieldName the field name to obtain
	 * @param value to set
	 * @param forceAccess whether to break scope restrictions using the
	 *            <code>setAccessible</code> method. <code>False</code> will
	 *            only match public fields.
	 * @throws IllegalArgumentException if <code>target</code> or
	 *             <code>fieldName</code> is null
	 * @throws IllegalAccessException if the field is not made accessible
	 */
	public static void writeField(Object target, String fieldName, Object value, boolean forceAccess)
			throws IllegalAccessException {
		if (target == null) {
			throw new IllegalArgumentException("target object must not be null");
		}
		Class cls = target.getClass();
		Field field = getField(cls, fieldName, forceAccess);
		if (field == null) {
			throw new IllegalArgumentException("Cannot locate declared field " + cls.getName()
					+ "." + fieldName);
		}
		// already forced access above, don't repeat it here:
		writeField(field, target, value);
	}

	// Useful member methods
	private static class MemberUtils {

		private static final int ACCESS_TEST = Modifier.PUBLIC | Modifier.PROTECTED
				| Modifier.PRIVATE;

		public static void setAccessibleWorkaround(AccessibleObject o) {
			if (o == null || o.isAccessible()) {
				return;
			}
			Member m = (Member) o;
			if (Modifier.isPublic(m.getModifiers())
					&& isPackageAccess(m.getDeclaringClass().getModifiers())) {
				try {
					o.setAccessible(true);
				} catch (SecurityException e) { // NOPMD
					// ignore in favor of subsequent IllegalAccessException
				}
			}
		}

		/**
		 * Returns whether a given set of modifiers implies package access.
		 * 
		 * @param modifiers to test
		 * @return true unless package/protected/private modifier detected
		 */
		public static boolean isPackageAccess(int modifiers) {
			return (modifiers & ACCESS_TEST) == 0;
		}
	}
}
