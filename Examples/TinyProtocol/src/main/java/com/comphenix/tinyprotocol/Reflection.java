package com.comphenix.tinyprotocol;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

/**
 * An utility class that simplifies reflection in Bukkit plugins.
 * @author Kristian
 */
public final class Reflection {
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
	
	// Deduce the net.minecraft.server.v* package
	private static String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
	private static String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");
	private static String VERSION = OBC_PREFIX.replace("org.bukkit.craftbukkit", "").replace(".", "");
	
	// Variable replacement
	private static Pattern MATCH_VARIABLE = Pattern.compile("\\{([^\\}]+)\\}");
	
	private Reflection() {
		// Seal class
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
	 * @param className - lookup name of the class, see {@link #getClass(String)}.
	 * @param name - the name of the field, or NULL to ignore.
	 * @param fieldType - a compatible field type.
	 * @return The field accessor.
	 */
	public static <T> FieldAccessor<T> getField(String className, String name, Class<T> fieldType) {
		return getField(getClass(className), name, fieldType, 0);
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
	 * @param className - lookup name of the class, see {@link #getClass(String)}.
	 * @param fieldType - a compatible field type.
	 * @param index - the number of compatible fields to skip.
	 * @return The field accessor.
	 */
	public static <T> FieldAccessor<T> getField(String className, Class<T> fieldType, int index) {
		return getField(getClass(className), fieldType, index);
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
     * @param className - lookup name of the class, see {@link #getClass(String)}.
     * @param methodName - the method name, or NULL to skip.
     * @param params - the expected parameters.
     * @return An object that invokes this specific method.
     * @throws IllegalStateException If we cannot find this method.
     */
	public static MethodInvoker getMethod(String className, String methodName, Class<?>... params) {
		return getMethod(getClass(className), methodName, params);
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
     * Retrieve a class from its full name.
     * <p>
     * Strings enclosed with curly brackets - such as {TEXT} - will be replaced according 
     * to the following table:
     * <p>
	 * <table border="1">
	 *   <tr>
	 *     <th>Variable</th>
	 *     <th>Content</th>
	 *   </tr>
	 *   <tr>
	 *     <td>{nms}</td>
	 *     <td>Actual package name of net.minecraft.server.VERSION</td>
	 *   </tr>
	 *   <tr>
	 *     <td>{obc}</td>
	 *     <td>Actual pacakge name of org.bukkit.craftbukkit.VERSION</td>
	 *   </tr>
	 *   <tr>
	 *     <td>{version}</td>
	 *     <td>The current Minecraft package VERSION, if any.</td>
	 *   </tr>
	 * </table>
     * @param lookupName - the class name with variables.
     * @return The looked up class.
     * @throws IllegalArgumentException If a variable or class could not be found.
     */
    public static Class<?> getClass(String lookupName) {
    	return getCanonicalClass(expandVariables(lookupName));
    }
    
	/**
	 * Retrieve a class in the net.minecraft.server.VERSION.* package.
	 * @param name - the name of the class, excluding the package.
	 * @throws IllegalArgumentException If the class doesn't exist.
	 */
	public static Class<?> getMinecraftClass(String name) {
		return getCanonicalClass(NMS_PREFIX + "." + name);
	}
	
	/**
	 * Retrieve a class in the org.bukkit.craftbukkit.VERSION.* package.
	 * @param name - the name of the class, excluding the package.
	 * @throws IllegalArgumentException If the class doesn't exist.
	 */
	public static Class<?> getCraftBukkitClass(String name) {
		return getCanonicalClass(OBC_PREFIX + "." + name);
	}
	
	/**
	 * Retrieve a class by its canonical name.
	 * @param canonicalName - the canonical name.
	 * @return The class.
	 */
	private static Class<?> getCanonicalClass(String canonicalName) {
    	try {
			return Class.forName(canonicalName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Cannot find " + canonicalName, e);
		}
	}
	
	/**
	 * Expand variables such as "{nms}" and "{obc}" to their corresponding packages.
	 * @param name - the full name of the class.
	 * @return The expanded string.
	 */
	private static String expandVariables(String name) {
	    StringBuffer output = new StringBuffer();
	    Matcher matcher = MATCH_VARIABLE.matcher(name);
	    
	    while (matcher.find()) {
	    	String variable = matcher.group(1);
	    	String replacement = "";
	    	
	    	// Expand all detected variables
	    	if ("nms".equalsIgnoreCase(variable))
	    		replacement = NMS_PREFIX;
	    	else if ("obc".equalsIgnoreCase(variable))
	    		replacement = OBC_PREFIX;
	    	else if ("version".equalsIgnoreCase(variable))
	    		replacement = VERSION;
	    	else
	    		throw new IllegalArgumentException("Unknown variable: " + variable);
	    	
	    	// Assume the expanded variables are all packages, and append a dot
	    	if (replacement.length() > 0 && matcher.end() < name.length() && name.charAt(matcher.end()) != '.')
	    		replacement += ".";
	        matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
	    }
	    matcher.appendTail(output);
	    return output.toString();
	}
}
