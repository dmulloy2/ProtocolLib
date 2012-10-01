package com.comphenix.protocol.reflect.compiler;

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
public abstract class CompiledStructureModifier<TField> extends StructureModifier<TField> {
	// Used to compile instances of structure modifiers
	protected StructureCompiler compiler;
	
	public CompiledStructureModifier() {
		super();
		customConvertHandling = true;
	}
	
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
	
	@SuppressWarnings("unchecked")
	@Override
	public final TField read(int fieldIndex) throws FieldAccessException {
		Object result = readGenerated(fieldIndex);
		
		if (converter != null)
			return converter.getSpecific(result);
		else
			return (TField) result;
	}
	
	protected abstract Object readGenerated(int fieldIndex) throws FieldAccessException;

	@SuppressWarnings("unchecked")
	@Override
	public StructureModifier<TField> write(int index, Object value) throws FieldAccessException {
		if (converter != null)
			value = converter.getGeneric((TField) value);
		return writeGenerated(index, value);
	}
	
	protected abstract StructureModifier<TField> writeGenerated(int index, Object value) throws FieldAccessException;
	
	@Override
	public StructureModifier<TField> withTarget(Object target) {
		if (compiler != null)
			return compiler.compile(super.withTarget(target));
		else
			return super.withTarget(target);
	}
}
