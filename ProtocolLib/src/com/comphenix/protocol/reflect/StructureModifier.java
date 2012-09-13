package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.gson.internal.Primitives;

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
	
	// Improved default values
	private Set<Field> defaultFields;
	
	// Cache of previous types
	private Map<Class, StructureModifier> subtypeCache;
	
	public StructureModifier(Class targetType, Class superclassExclude) {
		List<Field> fields = getFields(targetType, superclassExclude);
		
		initialize(targetType, Object.class, 
				fields, generateDefaultFields(fields), null, 
				new HashMap<Class, StructureModifier>());
	}
	
	private StructureModifier(StructureModifier<TField> other, Object target) {
		initialize(other.targetType, other.fieldType, other.data, other.defaultFields, other.converter, other.subtypeCache);
		this.target = target;
	}
	
	private StructureModifier() {
		// Consumers of this method should call "initialize"
	}
	
	private void initialize(Class targetType, Class fieldType, 
			  List<Field> data, Set<Field> defaultFields,
			  EquivalentConverter<TField> converter, Map<Class, StructureModifier> subTypeCache) {
		this.targetType = targetType;
		this.fieldType = fieldType;
		this.data = data;
		this.defaultFields = defaultFields;
		this.converter = converter;
		this.subtypeCache = subTypeCache;
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
	 * Reads the value of a field if and ONLY IF it exists.
	 * @param fieldIndex - index of the field.
	 * @return Value of the field, or NULL if it doesn't exist.
	 * @throws IllegalAccessException If we're unable to read the field due to a security limitation.
	 */
	public TField readSafely(int fieldIndex) throws IllegalAccessException {
		if (fieldIndex >= 0 && fieldIndex < data.size()) {
			return read(fieldIndex);
		} else {
			return null;
		}
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
	 * Writes the value of a given field IF and ONLY if it exists.
	 * @param fieldIndex - index of the potential field.
	 * @param value - new value of the field.
	 * @return This structure modifer - for chaining.
	 * @throws IllegalAccessException If we're unable to write to the field due to a security limitation.
	 */
	public StructureModifier<TField> writeSafely(int fieldIndex, TField value) throws IllegalAccessException {
		if (fieldIndex >= 0 && fieldIndex < data.size()) {
			write(fieldIndex, value);
		}
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
	 * Sets all non-primitive fields to a more fitting default value. See {@link DefaultInstance}.
	 * @return The current structure modifier - for chaining.
	 * @throws IllegalAccessException If we're unable to write to the fields due to a security limitation.
	 */
	public StructureModifier<TField> writeDefaults() throws IllegalAccessException {
		
		// Write a default instance to every field
		for (Field field : defaultFields) {
			FieldUtils.writeField(field, target, 
					DefaultInstances.getDefault(field.getType()), true);
		}
		
		return this;
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
			Set<Field> defaults = new HashSet<Field>();
			
			for (Field field : data) {
				if (fieldType.isAssignableFrom(field.getType())) {
					filtered.add(field);
					
					if (defaultFields.contains(field))
						defaults.add(field);
				}
			}
			
			// Cache structure modifiers
			result = new StructureModifier<T>();
			result.initialize(targetType, fieldType, filtered, defaults, 
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
	
	/**
	 * Retrieves a list of the fields matching the constraints of this structure modifier.
	 * @return List of fields.
	 */
	public List<Field> getFields() {
		return ImmutableList.copyOf(data);
	}
	
	// Used to generate plausible default values
	private static Set<Field> generateDefaultFields(List<Field> fields) {
		
		Set<Field> requireDefaults = new HashSet<Field>();

		for (Field field : fields) {
			Class<?> type = field.getType();
			
			// First, ignore primitive fields
			if (!Primitives.isPrimitive(type)) {
				// Next, see if we actually can generate a default value
				if (DefaultInstances.getDefault(type) != null) {
					// If so, require it
					requireDefaults.add(field);
				}
			}
		}
		
		return requireDefaults;
	}
	
	// Used to filter out irrelevant fields
	private static List<Field> getFields(Class type, Class superclassExclude) {
		List<Field> result = new ArrayList<Field>();
		
		// Retrieve every private and public field
		for (Field field : FuzzyReflection.fromClass(type, true).getFields()) {
			int mod = field.getModifiers();
			
			// Ignore static, final and "abstract packet" fields
			if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod) && (
					superclassExclude == null || !field.getDeclaringClass().equals(Packet.class)
				)) {
				
				result.add(field);
			}
		}
		
		return result;
	}
}
