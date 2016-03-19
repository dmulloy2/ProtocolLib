/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.PluginContext;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.reflect.instances.BannedGenerator;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Provides list-oriented access to the fields of a Minecraft packet.
 * <p>
 * Implemented by using reflection. Use a CompiledStructureModifier, if speed is essential.
 * 
 * @author Kristian
 * @param <TField> Type of the fields to retrieve.
 */
@SuppressWarnings("rawtypes")
public class StructureModifier<TField> {
	
	// Object and its type
	protected Class targetType;
	protected Object target;
	
	// Converter. May be NULL.
	protected EquivalentConverter<TField> converter;
	
	// The fields to read in order
	protected Class fieldType;
	protected List<Field> data = new ArrayList<Field>();
	
	// Improved default values
	protected Map<Field, Integer> defaultFields;
	
	// Cache of previous types
	protected Map<Class, StructureModifier> subtypeCache;
	
	// Whether or subclasses should handle conversion
	protected boolean customConvertHandling;
	
	// Whether or not to automatically compile the structure modifier
	protected boolean useStructureCompiler;
	
	// Instance generator we wil use
	private static DefaultInstances DEFAULT_GENERATOR = getDefaultGenerator();

	private static DefaultInstances getDefaultGenerator() {
		List<InstanceProvider> providers = Lists.newArrayList();
		
		// Prevent certain classes from being generated
		providers.add(new BannedGenerator(MinecraftReflection.getItemStackClass(), MinecraftReflection.getBlockClass()));
		providers.addAll(DefaultInstances.DEFAULT.getRegistered());
		return DefaultInstances.fromCollection(providers);
	}
			
	/**
	 * Creates a structure modifier.
	 * @param targetType - the structure to modify.
	 */
	public StructureModifier(Class targetType) {
		this(targetType, null, true);
	}
	
	/**
	 * Creates a structure modifier.
	 * @param targetType - the structure to modify.
	 * @param useStructureCompiler - whether or not to use a structure compiler.
	 */
	public StructureModifier(Class targetType, boolean useStructureCompiler) {
		this(targetType, null, true, useStructureCompiler);
	}
	
	/**
	 * Creates a structure modifier.
	 * @param targetType - the structure to modify.
	 * @param superclassExclude - a superclass to exclude.
	 * @param requireDefault - whether or not we will be using writeDefaults().
	 */
	public StructureModifier(Class targetType, Class superclassExclude, boolean requireDefault) {
		this(targetType, superclassExclude, requireDefault, true);
	}
	
	/**
	 * Creates a structure modifier.
	 * @param targetType - the structure to modify.
	 * @param superclassExclude - a superclass to exclude.
	 * @param requireDefault - whether or not we will be using writeDefaults().
	 * @param useStructureCompiler - whether or not to automatically compile this structure modifier.
	 */
	public StructureModifier(Class targetType, Class superclassExclude, boolean requireDefault, boolean useStructureCompiler) {
		List<Field> fields = getFields(targetType, superclassExclude);
		Map<Field, Integer> defaults = requireDefault ? generateDefaultFields(fields) : new HashMap<Field, Integer>();
		
		initialize(targetType, Object.class, fields, defaults, null,
				   new ConcurrentHashMap<Class, StructureModifier>(), useStructureCompiler);
	}
	
	/**
	 * Consumers of this method should call "initialize".
	 */
	protected StructureModifier() {
		
	}
	
	/**
	 * Initialize using the same field types.
	 * @param other - information to set.
	 */
	protected void initialize(StructureModifier<TField> other) {
		initialize(other.targetType, other.fieldType, other.data,
				   other.defaultFields, other.converter, other.subtypeCache,
				   other.useStructureCompiler);
	}
	
	/**
	 * Initialize every field of this class.
	 * @param targetType - type of the object we're reading and writing from.
	 * @param fieldType - the common type of the fields we're modifying.
	 * @param data - list of fields to modify.
	 * @param defaultFields - list of fields that will be automatically initialized.
	 * @param converter - converts between the common field type and the actual type the consumer expects.
	 * @param subTypeCache - a structure modifier cache.
	 */
	protected void initialize(Class targetType, Class fieldType,
			  List<Field> data, Map<Field, Integer> defaultFields,
			  EquivalentConverter<TField> converter, Map<Class, StructureModifier> subTypeCache) {
		initialize(targetType, fieldType, data, defaultFields, converter, subTypeCache, true);
	}
	
	/**
	 * Initialize every field of this class.
	 * @param targetType - type of the object we're reading and writing from.
	 * @param fieldType - the common type of the fields we're modifying.
	 * @param data - list of fields to modify.
	 * @param defaultFields - list of fields that will be automatically initialized.
	 * @param converter - converts between the common field type and the actual type the consumer expects.
	 * @param subTypeCache - a structure modifier cache.
	 * @param useStructureCompiler - whether or not to automatically compile this structure modifier.
	 */
	protected void initialize(Class targetType, Class fieldType,
			  List<Field> data, Map<Field, Integer> defaultFields,
			  EquivalentConverter<TField> converter, Map<Class, StructureModifier> subTypeCache,
			  boolean useStructureCompiler) {
		
		this.targetType = targetType;
		this.fieldType = fieldType;
		this.data = data;
		this.defaultFields = defaultFields;
		this.converter = converter;
		this.subtypeCache = subTypeCache;
		this.useStructureCompiler = useStructureCompiler;
	}

	/**
	 * Reads the value of a field given its index.
	 * <p>
	 * Note: This method is prone to exceptions (there are currently 5 total throw statements). It is recommended that you
	 * use {@link #readSafely(int)}, which returns {@code null} if the field doesn't exist, instead of throwing an exception.
	 * 
	 * @param fieldIndex - index of the field.
	 * @return Value of the field.
	 * @throws FieldAccessException if the field doesn't exist, or it cannot be accessed under the current security contraints.
	 */
	public TField read(int fieldIndex) throws FieldAccessException {
		try {
			return readInternal(fieldIndex);
		} catch (FieldAccessException ex) {
			String plugin = PluginContext.getPluginCaller(ex);
			if (ProtocolLibrary.INCOMPATIBLE.contains(plugin)) {
				ProtocolLibrary.log(Level.WARNING, "Encountered an exception caused by incompatible plugin {0}.", plugin);
				ProtocolLibrary.log(Level.WARNING, "It is advised that you remove it.");
			}

			throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	private TField readInternal(int fieldIndex) throws FieldAccessException {
		if (target == null)
			throw new IllegalStateException("Cannot read from a null target!");

		if (fieldIndex < 0)
			throw new FieldAccessException(String.format("Field index (%s) cannot be negative.", fieldIndex));

		if (data.size() == 0)
			throw new FieldAccessException(String.format("No field with type %s exists in class %s.", fieldType.getName(),
					target.getClass().getSimpleName()));

		if (fieldIndex >= data.size())
			throw new FieldAccessException(String.format("Field index out of bounds. (Index: %s, Size: %s)", fieldIndex, data.size()));

		try {
			Object result = FieldUtils.readField(data.get(fieldIndex), target, true);

			// Use the converter, if we have it
			if (needConversion()) {
				return converter.getSpecific(result);
			} else {
				return (TField) result;
			}
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot read field due to a security limitation.", e);
		}
	}
	
	/**
	 * Reads the value of a field only if it exists. If the field does not exist, {@code null} is returned.
	 * <p>
	 * As its name implies, this method is a much safer alternative to {@link #read(int)}.
	 * In addition to throwing less exceptions and thereby causing less console spam, this
	 * method makes providing backwards compatiblity signficiantly easier, as shown below:
	 * 
	 * <pre><code>
	 * BlockPosition position = packet.getBlockPositionModifier().readSafely(0);
	 * if (position != null) {
	 *     // Handle 1.8+
	 * } else {
	 *     // Handle 1.7-
	 * }
	 * </code></pre>
	 * 
	 * @param fieldIndex - index of the field.
	 * @return Value of the field, or NULL if it doesn't exist.
	 * @throws FieldAccessException if the field cannot be accessed under the current security constraints.
	 */
	public TField readSafely(int fieldIndex) throws FieldAccessException {
		if (fieldIndex >= 0 && fieldIndex < data.size()) {
			return read(fieldIndex);
		} else {
			return null;
		}
	}
	
	/**
	 * Determine whether or not a field is read-only (final).
	 * @param fieldIndex - index of the field.
	 * @return TRUE if the field by the given index is read-only, FALSE otherwise.
	 */
	public boolean isReadOnly(int fieldIndex) {
		return Modifier.isFinal(getField(fieldIndex).getModifiers());
	}
	
	/**
	 * Determine if a given field is public or not.
	 * @param fieldIndex - field index.
	 * @return TRUE if the field is public, FALSE otherwise.
	 */
	public boolean isPublic(int fieldIndex) {
		return Modifier.isPublic(getField(fieldIndex).getModifiers());
	}
	
	/**
	 * Set whether or not a field should be treated as read only.
	 * <p>
	 * Note that changing the read-only state to TRUE will only work if the current
	 * field was recently read-only or the current structure modifier hasn't been compiled yet.
	 * 
	 * @param fieldIndex - index of the field.
	 * @param value - TRUE if this field should be read only, FALSE otherwise.
	 * @throws FieldAccessException If we cannot modify the read-only status.
	 */
	public void setReadOnly(int fieldIndex, boolean value) throws FieldAccessException {
		if (fieldIndex < 0 || fieldIndex >= data.size())
			throw new IllegalArgumentException("Index parameter is not within [0 - " + data.size() + ")");

		try {
			StructureModifier.setFinalState(data.get(fieldIndex), value);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot write read only status due to a security limitation.", e);
		}
	}
	
	/**
	 * Alter the final status of a field.
	 * @param field - the field to change.
	 * @param isReadOnly - TRUE if the field should be read only, FALSE otherwise.
	 * @throws IllegalAccessException If an error occured.
	 */
	protected static void setFinalState(Field field, boolean isReadOnly) throws IllegalAccessException {
	    if (isReadOnly)
	    	FieldUtils.writeField((Object) field, "modifiers", field.getModifiers() | Modifier.FINAL, true);
	    else
	    	FieldUtils.writeField((Object) field, "modifiers", field.getModifiers() & ~Modifier.FINAL, true);
	}
	
	/**
	 * Writes the value of a field given its index.
	 * @param fieldIndex - index of the field.
	 * @param value - new value of the field.
	 * @return This structure modifier - for chaining.
	 * @throws FieldAccessException The field doesn't exist, or it cannot be accessed under the current security contraints.
	 */
	public StructureModifier<TField> write(int fieldIndex, TField value) throws FieldAccessException {
		try {
			return writeInternal(fieldIndex, value);
		} catch (FieldAccessException ex) {
			String plugin = PluginContext.getPluginCaller(ex);
			if (ProtocolLibrary.INCOMPATIBLE.contains(plugin)) {
				ProtocolLibrary.log(Level.WARNING, "Encountered an exception caused by incompatible plugin {0}.", plugin);
				ProtocolLibrary.log(Level.WARNING, "It is advised that you remove it.");
			}

			throw ex;
		}
	}

	private StructureModifier<TField> writeInternal(int fieldIndex, TField value) throws FieldAccessException {
		if (target == null)
			throw new IllegalStateException("Cannot read from a null target!");

		if (fieldIndex < 0)
			throw new FieldAccessException(String.format("Field index (%s) cannot be negative.", fieldIndex));

		if (data.size() == 0)
			throw new FieldAccessException(String.format("No field with type %s exists in class %s.", fieldType.getName(),
					target.getClass().getSimpleName()));

		if (fieldIndex >= data.size())
			throw new FieldAccessException(String.format("Field index out of bounds. (Index: %s, Size: %s)", fieldIndex, data.size()));

		// Use the converter, if it exists
		Object obj = needConversion() ? converter.getGeneric(getFieldType(fieldIndex), value) : value;

		try {
			FieldUtils.writeField(data.get(fieldIndex), target, obj, true);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot read field due to a security limitation.", e);
		}

		// Make this method chainable
		return this;
	}
	
	/**
	 * Retrieve the type of a specified field.
	 * @param index - the index.
	 * @return The type of the given field.
	 */
	protected Class<?> getFieldType(int index) {
		return data.get(index).getType();
	}
	
	/**
	 * Whether or not we should use the converter instance.
	 * @return TRUE if we should, FALSE otherwise.
	 */
	private final boolean needConversion() {
		return converter != null && !customConvertHandling;
	}
	
	/**
	 * Writes the value of a given field IF and ONLY if it exists.
	 * @param fieldIndex - index of the potential field.
	 * @param value - new value of the field.
	 * @return This structure modifer - for chaining.
	 * @throws FieldAccessException The field cannot be accessed under the current security contraints.
	 */
	public StructureModifier<TField> writeSafely(int fieldIndex, TField value) throws FieldAccessException {
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
	 * @throws FieldAccessException The field cannot be accessed under the current security contraints.
	 */
	public StructureModifier<TField> modify(int fieldIndex, Function<TField, TField> select) throws FieldAccessException {
		TField value = read(fieldIndex);
		return write(fieldIndex, select.apply(value));
	}
	
	/**
	 * Retrieves a structure modifier that only reads and writes fields of a given type.
	 * @param <T> Type
	 * @param fieldType - the type, or supertype, of every field to modify.
	 * @return A structure modifier for fields of this type.
	 */
	public <T> StructureModifier<T> withType(Class fieldType) {
		return withType(fieldType, null);
	}
	
	/**
	 * Sets all non-primitive fields to a more fitting default value. See {@link DefaultInstances#getDefault(Class)}.
	 * @return The current structure modifier - for chaining.
	 * @throws FieldAccessException If we're unable to write to the fields due to a security limitation.
	 */
	public StructureModifier<TField> writeDefaults() throws FieldAccessException {
		DefaultInstances generator = DefaultInstances.DEFAULT;

		// Write a default instance to every field
		for (Field field : defaultFields.keySet()) {
			try {
				// Special case for Spigot's custom chat components
				// They must be null or messages will be blank
				if (field.getType().getCanonicalName().equals("net.md_5.bungee.api.chat.BaseComponent[]")) {
					FieldUtils.writeField(field, target, null, true);
					continue;
				}

				FieldUtils.writeField(field, target, generator.getDefault(field.getType()), true);
			} catch (IllegalAccessException e) {
				throw new FieldAccessException("Cannot write to field due to a security limitation.", e);
			}
		}

		return this;
	}
	
	/**
	 * Retrieves a structure modifier that only reads and writes fields of a given type.
	 * @param <T> Type
	 * @param fieldType - the type, or supertype, of every field to modify.
	 * @param converter - converts objects into the given type.
	 * @return A structure modifier for fields of this type.
	 */
	@SuppressWarnings("unchecked")
	public <T> StructureModifier<T> withType(Class fieldType, EquivalentConverter<T> converter) {
		StructureModifier<T> result = subtypeCache.get(fieldType);
		
		// Do we need to update the cache?
		if (result == null) {
			List<Field> filtered = new ArrayList<Field>();
			Map<Field, Integer> defaults = new HashMap<Field, Integer>();
			int index = 0;
			
			for (Field field : data) {
				if (fieldType != null && fieldType.isAssignableFrom(field.getType())) {
					filtered.add(field);
					
					// Don't use the original index
					if (defaultFields.containsKey(field))
						defaults.put(field, index);
				}
				
				// Keep track of the field index
				index++;
			}
			
			// Cache structure modifiers
			result = withFieldType(fieldType, filtered, defaults);
			
			if (fieldType != null) {
				subtypeCache.put(fieldType, result);
				
				// Automatically compile the structure modifier
				if (useStructureCompiler && BackgroundCompiler.getInstance() != null)
					BackgroundCompiler.getInstance().scheduleCompilation(subtypeCache, fieldType);
			}
		}
		
		// Add the target too
		result = result.withTarget(target);
		
		// And the converter, if it's needed
		if (!Objects.equal(result.converter, converter)) {
			result = result.withConverter(converter);
		}
		return result;
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
	 * Create a new structure modifier for the new field type.
	 * @param <T> Type
	 * @param fieldType - common type of each field.
	 * @param filtered - list of fields after filtering the original modifier.
	 * @param defaults - list of default values after filtering the original.
	 * @return A new structure modifier.
	 */
	protected <T> StructureModifier<T> withFieldType(
			Class fieldType, List<Field> filtered, Map<Field, Integer> defaults) {
		return withFieldType(fieldType, filtered, defaults, null);
	}
	
	/**
	 * Create a new structure modifier for the new field type.
	 * @param <T> Type
	 * @param fieldType - common type of each field.
	 * @param filtered - list of fields after filtering the original modifier.
	 * @param defaults - list of default values after filtering the original.
	 * @param converter - the new converter, or NULL.
	 * @return A new structure modifier.
	 */
	protected <T> StructureModifier<T> withFieldType(
			Class fieldType, List<Field> filtered,
			Map<Field, Integer> defaults, EquivalentConverter<T> converter) {
		
		StructureModifier<T> result = new StructureModifier<T>();
		result.initialize(targetType, fieldType, filtered, defaults,
						  converter, new ConcurrentHashMap<Class, StructureModifier>(),
						  useStructureCompiler);
		return result;
	}
	
	/**
	 * Retrieves a structure modifier of the same type for a different object target.
	 * @param target - different target of the same type.
	 * @return Structure modifier with the new target.
	 */
	public StructureModifier<TField> withTarget(Object target) {
		StructureModifier<TField> copy = new StructureModifier<TField>();
		
		// Create a new instance
		copy.initialize(this);
		copy.target = target;
		return copy;
	}
	
	/**
	 * Retrieves a structure modifier with the same type and target, but using a new object converter.
	 * @param converter - the object converter to use.
	 * @return Structure modifier with the new converter.
	 */
	@SuppressWarnings("unchecked")
	private <T> StructureModifier<T> withConverter(EquivalentConverter<T> converter) {
		StructureModifier copy = withTarget(target);
		
		copy.setConverter(converter);
		return copy;
	}
	
	/**
	 * Set the current object converter. Should only be called during construction.
	 * @param converter - current object converter.
	 */
	protected void setConverter(EquivalentConverter<TField> converter) {
		this.converter = converter;
	}
	
	/**
	 * Retrieves a list of the fields matching the constraints of this structure modifier.
	 * @return List of fields.
	 */
	public List<Field> getFields() {
		return ImmutableList.copyOf(data);
	}

	/**
	 * Retrieve a field by index.
	 * @param fieldIndex - index of the field to retrieve.
	 * @return The field represented with the given index.
	 * @throws IllegalArgumentException If no field with the given index can be found.
	 */
	public Field getField(int fieldIndex) {
		if (fieldIndex < 0 || fieldIndex >= data.size())
			throw new IllegalArgumentException("Index parameter is not within [0 - " + data.size() + ")");
		
		return data.get(fieldIndex);
	}
	
	/**
	 * Retrieve every value stored in the fields of the current type.
	 * @return Every field value.
	 * @throws FieldAccessException Unable to access one or all of the fields
	 */
	public List<TField> getValues() throws FieldAccessException {
		List<TField> values = new ArrayList<TField>();
		
		for (int i = 0; i < size(); i++) {
			values.add(read(i));
		}
		
		return values;
	}
	
	// Used to generate plausible default values
	private static Map<Field, Integer> generateDefaultFields(List<Field> fields) {
		
		Map<Field, Integer> requireDefaults = new HashMap<Field, Integer>();
		DefaultInstances generator = DEFAULT_GENERATOR;
		int index = 0;
		
		for (Field field : fields) {
			Class<?> type = field.getType();
			int modifier = field.getModifiers();
			
			// First, ignore primitive fields and final fields
			if (!type.isPrimitive() && !Modifier.isFinal(modifier)) {
				// Next, see if we actually can generate a default value
				if (generator.getDefault(type) != null) {
					// If so, require it
					requireDefaults.put(field, index);
				}
			}
			
			// Increment field index
			index++;
		}
		
		return requireDefaults;
	}
	
	// Used to filter out irrelevant fields
	private static List<Field> getFields(Class type, Class superclassExclude) {
		List<Field> result = new ArrayList<Field>();
		
		// Retrieve every private and public field
		for (Field field : FuzzyReflection.fromClass(type, true).getDeclaredFields(superclassExclude)) {
			int mod = field.getModifiers();
			
			// Ignore static and "abstract packet" fields
			if (!Modifier.isStatic(mod) &&
					(superclassExclude == null || !field.getDeclaringClass().equals(superclassExclude)
				)) {
				
				result.add(field);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "StructureModifier[fieldType=" + fieldType + ", data=" + data + "]";
	}
}