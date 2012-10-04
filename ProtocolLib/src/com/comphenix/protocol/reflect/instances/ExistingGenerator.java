package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;

/**
 * Provides instance constructors using a list of existing values.
 * <p>
 * Only one instance per individual class.
 * @author Kristian
 */
public class ExistingGenerator implements InstanceProvider {

	@SuppressWarnings("rawtypes")
	private Map<Class, Object> existingValues = new HashMap<Class, Object>();
	
	private ExistingGenerator() {
		// Only accessible to the constructors
	}
	
	/**
	 * Automatically create an instance provider from a objects public and private fields.
	 * <p>
	 * If two or more fields share the same type, the last declared non-null field will take
	 * precedent.
	 * @param object - object to create an instance generator from.
	 * @return The instance generator.
	 */
	public static ExistingGenerator fromObjectFields(Object object) {
		if (object == null)
			throw new IllegalArgumentException("Object cannot be NULL.");
		
		return fromObjectFields(object, object.getClass());
	}
	
	/**
	 * Automatically create an instance provider from a objects public and private fields.
	 * <p>
	 * If two or more fields share the same type, the last declared non-null field will take
	 * precedent.
	 * @param object - object to create an instance generator from.
	 * @param type - the type to cast the object.
	 * @return The instance generator.
	 */
	public static ExistingGenerator fromObjectFields(Object object, Class<?> type) {
		ExistingGenerator generator = new ExistingGenerator();
		
		// Possible errors
		if (object == null)
			throw new IllegalArgumentException("Object cannot be NULL.");
		if (type == null)
			throw new IllegalArgumentException("Type cannot be NULL.");
		if (!type.isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException("Type must be a superclass or be the same type.");
		
		// Read instances from every field.
		for (Field field : FuzzyReflection.fromClass(type, true).getFields()) {
			try {
				Object value = FieldUtils.readField(field, object, true);

				// Use the type of the field, not the object itself
				if (value != null)
					generator.addObject(field.getType(), value);
				
			} catch (Exception e) {
				// Yes, swallow it. No, really.
			}
		}
		
		return generator;
	}
	
	/**
	 * Create an instance generator from a pre-defined array of values.
	 * @param values - values to provide.
	 * @return An instance provider that uses these values.
	 */
	public static ExistingGenerator fromObjectArray(Object[] values) {
		ExistingGenerator generator = new ExistingGenerator();
		
		for (Object value : values)
			 generator.addObject(value);
		
		return generator;
	}
	
	private void addObject(Object value) {
		if (value == null)
			throw new IllegalArgumentException("Value cannot be NULL.");
		
		existingValues.put(value.getClass(), value);
	}

	private void addObject(Class<?> type, Object value) {
		existingValues.put(type, value);
	}

	
	@Override
	public Object create(@Nullable Class<?> type) {
		
		Object value = existingValues.get(type);
		
		// NULL values indicate that the generator failed
		return value;
	}
}
