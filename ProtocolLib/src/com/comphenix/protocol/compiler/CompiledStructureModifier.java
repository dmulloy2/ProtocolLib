package com.comphenix.protocol.compiler;

import java.lang.reflect.Field;
import java.util.Map;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;

/**
 * Represents a compiled structure modifier.
 * 
 * @author Kristian
 * @param <TField> Field type.
 */
public class CompiledStructureModifier<TField> extends StructureModifier<TField> {
	// Used to compile instances of structure modifiers
	protected StructureCompiler compiler;
	
	// Speed up the default writer
	@SuppressWarnings("unchecked")
	@Override
	public StructureModifier<TField> writeDefaults() throws FieldAccessException {
		
		DefaultInstances generator = DefaultInstances.DEFAULT;
		
		// Write a default instance to every field
		for (Map.Entry<Field, Integer> entry  : defaultFields.entrySet()) {
			Integer index = entry.getValue();
			Field field = entry.getKey();
			
			write(index, (TField) generator.getDefault(field.getType()));
		}
		
		return this;
	}
	
	@Override
	public StructureModifier<TField> withTarget(Object target) {
		if (compiler != null)
			return compiler.compile(super.withTarget(target));
		else
			return super.withTarget(target);
	}
}
