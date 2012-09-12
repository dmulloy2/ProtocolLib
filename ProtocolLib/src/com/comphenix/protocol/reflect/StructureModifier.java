package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

import net.minecraft.server.Packet;

@SuppressWarnings("rawtypes")
public class StructureModifier<TField> {

	// Object and its type
	private Class targetType;
	private Object target;
	
	// Converter. May be NULL.
	private EquivalentConverter<TField> converter;
	
	// The fields to read in order
	private Class fieldType;
	private List<Field> data = new ArrayList<Field>();
	
	// Cache of previous types
	private Map<Class, StructureModifier> subtypeCache;
	
	public StructureModifier(Class targetType) {
		this(targetType, Object.class, getFields(targetType), null, new HashMap<Class, StructureModifier>());
	}
	
	private StructureModifier(Class targetType, Class fieldType, List<Field> data, 
							  EquivalentConverter<TField> converter, Map<Class, StructureModifier> subTypeCache) {
		this.targetType = targetType;
		this.fieldType = fieldType;
		this.data = data;
		this.converter = converter;
		this.subtypeCache = subTypeCache;
	}
	
	private StructureModifier(StructureModifier<TField> other, Object target) {
		this(other.targetType, other.fieldType, other.data, other.converter, other.subtypeCache);
		this.target = target;
	}
	
	/**
	 * Reads the value of a field given its index.
	 * @param fieldIndex - index of the field.
	 * @return Value of the field.
	 * @throws IllegalAccessException If we're unable to read the field due to a security limitation.
	 */
	@SuppressWarnings("unchecked")
	public TField read(int fieldIndex) throws IllegalAccessException {
		if (fieldIndex < 0 || fieldIndex >= data.size())
			throw new IllegalArgumentException("Field index must be within 0 - count");
		if (target == null)
			throw new IllegalStateException("Cannot read from a NULL target.");
		
		Object result = FieldUtils.readField(data.get(fieldIndex), target, true);
		
		// Use the converter, if we have it
		if (converter != null)
			return converter.getSpecific(result);
		else
			return (TField) result;
	}
	
	/**
	 * Writes the value of a field given its index.
	 * @param fieldIndex - index of the field.
	 * @param value - new value of the field.
	 * @return This structure modifier - for chaining.
	 * @throws IllegalAccessException If we're unable to write to the field due to a security limitation.
	 */
	public StructureModifier<TField> write(int fieldIndex, TField value) throws IllegalAccessException {
		if (fieldIndex < 0 || fieldIndex >= data.size())
			throw new IllegalArgumentException("Field index must be within 0 - count");
		if (target == null)
			throw new IllegalStateException("Cannot write to a NULL target.");
		
		// Use the converter, if it exists
		Object obj = converter != null ? converter.getGeneric(value) : value;
		FieldUtils.writeField(data.get(fieldIndex), target, obj, true);
		
		// Make this method chainable
		return this;
	}
	
	/**
	 * Correctly modifies the value of a field.
	 * @param fieldIndex - index of the field to modify.
	 * @param select - the function that modifies the field value.
	 * @return This structure modifier - for chaining.
	 * @throws IllegalAccessException
	 */
	public StructureModifier<TField> modify(int fieldIndex, Function<TField, TField> select) throws IllegalAccessException {
		TField value = read(fieldIndex);
		return write(fieldIndex, select.apply(value));
	}
	
	/**
	 * Retrieves a structure modifier that only reads and writes fields of a given type.
	 * @param fieldType - the type, or supertype, of every field to modify.
	 * @return A structure modifier for fields of this type.
	 */
	public <T> StructureModifier<T> withType(Class fieldType) {
		return withType(fieldType, null);
	}
	
	/**
	 * Retrieves a structure modifier that only reads and writes fields of a given type.
	 * @param fieldType - the type, or supertype, of every field to modify.
	 * @param converter - converts objects into the given type.
	 * @return A structure modifier for fields of this type.
	 */
	@SuppressWarnings("unchecked")
	public <T> StructureModifier<T> withType(Class fieldType, EquivalentConverter<T> converter) {
		
		StructureModifier<T> result = subtypeCache.get(fieldType);
		
		if (fieldType.equals(this.fieldType)) {
			
			// We're dealing with the exact field type.
			return withConverter(converter);
			
		} else if (result == null) {
			List<Field> filtered = new ArrayList<Field>();
			
			for (Field field : data) {
				if (fieldType.isAssignableFrom(field.getType())) {
					filtered.add(field);
				}
			}
			
			// Cache structure modifiers
			result = new StructureModifier<T>(targetType, fieldType, filtered, 
											  converter, new HashMap<Class, StructureModifier>());
			subtypeCache.put(fieldType, result);
		}
		
		// Add the target too
		return result.withTarget(target);
	}
	
	/**
	 * Retrieves the common type of each field.
	 * @return Common type of each field.
	 */
	public Class getFieldType() {
		return fieldType;
	}
	
	/**
	 * Retrieves the type of the object we're modifying.
	 * @return Type of the object.
	 */
	public Class getTargetType() {
		return targetType;
	}
	
	/**
	 * Retrieves the object we're currently modifying.
	 * @return Object we're modifying.
	 */
	public Object getTarget() {
		return target;
	}
	
	/**
	 * Retrieve the number of readable types.
	 * @return Readable types.
	 */
	public int size() {
		return data.size();
	}
	
	/**
	 * Retrieves a structure modifier of the same type for a different object target.
	 * @param target - different target of the same type.
	 * @return Structure modifier with the new target.
	 */
	public StructureModifier<TField> withTarget(Object target) {
		return new StructureModifier<TField>(this, target);
	}
	
	/**
	 * Retrieves a structure modifier with the same type and target, but using a new object converter.
	 * @param converter- the object converter to use.
	 * @return Structure modifier with the new converter.
	 */
	@SuppressWarnings("unchecked")
	private <T> StructureModifier<T> withConverter(EquivalentConverter<T> converter) {
		StructureModifier copy = new StructureModifier(this, target);
		
		copy.converter = converter;
		return copy;
	}
	
	// Used to filter out irrelevant fields
	private static List<Field> getFields(Class type) {
		List<Field> result = new ArrayList<Field>();
		
		// Retrieve every private and public field
		for (Field field : FuzzyReflection.fromClass(type, true).getFields()) {
			int mod = field.getModifiers();
			
			// Ignore static, final and "abstract packet" fields
			if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod) && !field.getDeclaringClass().equals(Packet.class)) {
				result.add(field);
			}
		}
		
		return result;
	}
}
