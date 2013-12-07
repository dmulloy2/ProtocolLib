package com.comphenix.protocol.wrappers.nbt;

import java.util.Map;

import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Maps;

public abstract class NameProperty {
	private static final Map<Class<?>, StructureModifier<String>> MODIFIERS = Maps.newConcurrentMap();
	
	/**
	 * Retrieve the name.
	 * @return The name.
	 */
	public abstract String getName();
	
	/**
	 * Set the name.
	 * @param name - the new value of the name.
	 */
	public abstract void setName(String name);
	
	/**
	 * Retrieve the string modifier for a particular class.
	 * @param baseClass - the base class.
	 * @return The string modifier, with no target.
	 */
	private static StructureModifier<String> getModifier(Class<?> baseClass) {
		StructureModifier<String> modifier = MODIFIERS.get(baseClass);
		
		// Share modifier
		if (modifier == null) {
			modifier = new StructureModifier<Object>(baseClass, Object.class, false).withType(String.class);
			MODIFIERS.put(baseClass, modifier);
		}
		return modifier;
	}
	
	/**
	 * Determine if a string of the given index exists in the base class.
	 * @param baseClass - the base class.
	 * @param index - the index to check.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public static boolean hasStringIndex(Class<?> baseClass, int index) {
		if (index < 0)
			return false;
		return index < getModifier(baseClass).size();
	}
	
	/**
	 * Retrieve a name property that delegates all read and write operations to a field of the given target.
	 * @param baseClass - the base class.
	 * @param target - the target 
	 * @param index - the index of the field.
	 * @return The name property.
	 */
	public static NameProperty fromStringIndex(Class<?> baseClass, Object target, final int index) {
		final StructureModifier<String> modifier = getModifier(baseClass).withTarget(target);
		
		return new NameProperty() {
			@Override
			public String getName() {
				return modifier.read(index);
			}
			
			@Override
			public void setName(String name) {
				modifier.write(index, name);
			}
		};
	}
	
	/**
	 * Retrieve a new name property around a simple field, forming a Java bean.
	 * @return The name property.
	 */
	public static NameProperty fromBean() {
		return new NameProperty() {
			private String name;
			
			@Override
			public void setName(String name) {
				this.name = name;
			}
			
			@Override
			public String getName() {
				return name;
			}
		};
	}
}
