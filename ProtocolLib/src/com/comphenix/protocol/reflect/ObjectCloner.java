package com.comphenix.protocol.reflect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Can copy an object field by field.
 * 
 * @author Kristian
 */
public class ObjectCloner {

	// Cache structure modifiers
	@SuppressWarnings("rawtypes")
	private static ConcurrentMap<Class, StructureModifier<Object>> cache =
			new ConcurrentHashMap<Class, StructureModifier<Object>>();
	
	/**
	 * Copy every field in object A to object B.
	 * <p>
	 * The two objects must have the same number of fields of the same type.
	 * @param source - fields to copy.
	 * @param destination - fields to copy to.
	 * @param commonType - type containing each field to copy.
	 */
	public static void copyTo(Object source, Object destination, Class<?> commonType) {
		
		if (source == null)
			throw new IllegalArgumentException("Source cannot be NULL");
		if (destination == null)
			throw new IllegalArgumentException("Destination cannot be NULL");
		
		StructureModifier<Object> modifier = cache.get(commonType);

		// Create the structure modifier if we haven't already
		if (modifier == null) {
			StructureModifier<Object> value = new StructureModifier<Object>(commonType, null, false);
			modifier = cache.putIfAbsent(commonType, value);
			
			if (modifier == null)
				modifier = value;
		}
		
		// Add target
		StructureModifier<Object> modifierSource = modifier.withTarget(source);
		StructureModifier<Object> modifierDest = modifier.withTarget(destination);
		
		// Copy every field
		try {
			for (int i = 0; i < modifierSource.size(); i++) {
				Object value = modifierSource.read(i);
				modifierDest.write(i, value);
				
				// System.out.println(String.format("Writing value %s to %s", 
				//		value, modifier.getFields().get(i).getName()));
			}
		} catch (FieldAccessException e) {
			throw new RuntimeException("Unable to copy fields from " + commonType.getName(), e);
		}
	}
}
