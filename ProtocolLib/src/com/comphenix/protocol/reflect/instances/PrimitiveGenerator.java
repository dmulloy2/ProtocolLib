package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Array;

import javax.annotation.Nullable;

import com.comphenix.protocol.reflect.PrimitiveUtils;
import com.google.common.base.Defaults;

/**
 * Provides constructors for primtive types, wrappers, arrays and strings.
 * @author Kristian
 */
public class PrimitiveGenerator implements InstanceProvider {
	
	/**
	 * Default value for Strings.
	 */
	public static final String STRING_DEFAULT = "";
	
	/**
	 * Shared instance of this generator.
	 */
	public static PrimitiveGenerator INSTANCE = new PrimitiveGenerator(STRING_DEFAULT);

	// Our default string value
	private String stringDefault;
	
	public PrimitiveGenerator(String stringDefault) {
		this.stringDefault = stringDefault;
	}
	
	/**
	 * Retrieve the string default.
	 * @return Default instance of a string.
	 */
	public String getStringDefault() {
		return stringDefault;
	}

	@Override
	public Object create(@Nullable Class<?> type) {
		
		if (PrimitiveUtils.isPrimitive(type)) {
			return Defaults.defaultValue(type);
		} else if (PrimitiveUtils.isWrapperType(type)) {
			return Defaults.defaultValue(PrimitiveUtils.unwrap(type));
		} else if (type.isArray()) {
			Class<?> arrayType = type.getComponentType();
			return Array.newInstance(arrayType, 0);
		} else if (type.isEnum()) {
			Object[] values = type.getEnumConstants();
			if (values != null && values.length > 0)
				return values[0];
		} else if (type.equals(String.class)) {
			return stringDefault;
		} 
		
		// Cannot handle this type
		return null;
	}	
}