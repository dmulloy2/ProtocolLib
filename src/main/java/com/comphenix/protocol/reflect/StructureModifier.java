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

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.instances.BannedGenerator;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.InstanceProvider;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Provides list-oriented access to the fields of a Minecraft packet.
 * <p>
 * Implemented by using reflection. Use a CompiledStructureModifier, if speed is essential.
 *
 * @param <T> Type of the fields to retrieve.
 * @author Kristian
 */
public class StructureModifier<T> {

    // Instance generator we will use
    private static final DefaultInstances DEFAULT_GENERATOR = getDefaultGenerator();
    // a structure modifier which does nothing
    private static final StructureModifier<Object> NO_OP_MODIFIER = new StructureModifier<Object>() {
        @Override
        public Object read(int fieldIndex) throws FieldAccessException {
            return null;
        }

        @Override
        public StructureModifier<Object> write(int fieldIndex, Object value) throws FieldAccessException {
            return this;
        }

        @Override
        protected FieldAccessor findFieldAccessor(int fieldIndex) {
            return null;
        }
    };

    // Object and its type
    protected Object target;
    protected Class<?> targetType;

    // The fields to read in order
    protected Class<?> fieldType;
    protected List<FieldAccessor> accessors = new ArrayList<>();

    // Converter. May be NULL.
    protected EquivalentConverter<T> converter;

    // Improved default values
    protected Map<FieldAccessor, Integer> defaultFields;
    // Cache of previous types
    protected Map<Class<?>, StructureModifier<?>> subtypeCache;

    // Whether or subclasses should handle conversion
    protected boolean customConvertHandling;

    /**
     * Creates a structure modifier.
     *
     * @param targetType - the structure to modify.
     */
    public StructureModifier(Class<?> targetType) {
        this(targetType, Object.class, true);
    }

    /**
     * Creates a structure modifier.
     *
     * @param targetType        - the structure to modify.
     * @param superclassExclude - a superclass to exclude.
     * @param requireDefault    - whether we will be using writeDefaults()
     */
    public StructureModifier(
            Class<?> targetType,
            Class<?> superclassExclude,
            boolean requireDefault
    ) {
        List<FieldAccessor> fields = getFields(targetType, superclassExclude);
        Map<FieldAccessor, Integer> defaults = requireDefault ? generateDefaultFields(fields) : new HashMap<>();

        this.initialize(targetType, Object.class, fields, defaults, null, new HashMap<>());
    }

    /**
     * Consumers of this method should call "initialize".
     */
    protected StructureModifier() {

    }

    private static DefaultInstances getDefaultGenerator() {
        List<InstanceProvider> providers = new ArrayList<>();

        // Prevent certain classes from being generated
        providers.add(new BannedGenerator(MinecraftReflection.getItemStackClass(), MinecraftReflection.getBlockClass()));
        providers.addAll(DefaultInstances.DEFAULT.getRegistered());
        return DefaultInstances.fromCollection(providers);
    }

    // Used to generate plausible default values
    private static Map<FieldAccessor, Integer> generateDefaultFields(Collection<FieldAccessor> fields) {
        int currentFieldIndex = 0;
        Map<FieldAccessor, Integer> requireDefaults = new HashMap<>();

        for (FieldAccessor accessor : fields) {
            Field field = accessor.getField();
            if (!field.getType().isPrimitive() && !Modifier.isFinal(field.getModifiers())) {
                if (DEFAULT_GENERATOR.hasDefault(field.getType())) {
                    requireDefaults.put(accessor, currentFieldIndex);
                }
            }

            // increment the index of the processed fields
            currentFieldIndex++;
        }

        return requireDefaults;
    }

    // Used to filter out irrelevant fields
    private static final ThreadLocal<Table<Class<?>, Class<?>, Reference<List<FieldAccessor>>>>
        fieldCacheLocal = ThreadLocal.withInitial(HashBasedTable::create);
    private static final Class<?> NULL_CACHE_CLASS_REPLACEMENT = Void.class;

    // Used to filter out irrelevant fields
    private static List<FieldAccessor> getFields(Class<?> type, Class<?> superclassExclude) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be NULL.");
        }
        Table<Class<?>, Class<?>, Reference<List<FieldAccessor>>> fieldCache = fieldCacheLocal.get();
        Class<?> superclassKey = superclassExclude == null ? NULL_CACHE_CLASS_REPLACEMENT : superclassExclude;
        Reference<List<FieldAccessor>> cacheEntryReference = fieldCache.get(type, superclassKey);
        if (cacheEntryReference != null) {
            List<FieldAccessor> cacheEntry = cacheEntryReference.get();
            if (cacheEntry != null) {
                return cacheEntry;
            }
        }
        List<FieldAccessor> accessors = FuzzyReflection.fromClass(type, true)
            .getDeclaredFields(superclassExclude)
            .stream()
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .map(Accessors::getFieldAccessor)
            .collect(Collectors.toList());
        fieldCache.put(type, superclassKey, new SoftReference<>(accessors));
        fieldCache.cellSet().removeIf(entry -> entry.getValue().get() == null);
        return accessors;
    }

    /**
     * Initialize using the same field types.
     *
     * @param other - information to set.
     */
    protected void initialize(StructureModifier<T> other) {
        this.initialize(
                other.targetType,
                other.fieldType,
                other.accessors,
                other.defaultFields,
                other.converter,
                other.subtypeCache);
    }

    /**
     * Initialize every field of this class.
     *
     * @param targetType    - type of the object we're reading and writing from.
     * @param fieldType     - the common type of the fields we're modifying.
     * @param data          - list of fields to modify.
     * @param defaultFields - list of fields that will be automatically initialized.
     * @param converter     - converts between the common field type and the actual type the consumer expects.
     * @param subTypeCache  - a structure modifier cache.
     */
    protected void initialize(
            Class<?> targetType,
            Class<?> fieldType,
            List<FieldAccessor> data,
            Map<FieldAccessor, Integer> defaultFields,
            EquivalentConverter<T> converter,
            Map<Class<?>, StructureModifier<?>> subTypeCache
    ) {
        this.targetType = targetType;
        this.fieldType = fieldType;
        this.accessors = data;
        this.defaultFields = defaultFields;
        this.converter = converter;
        this.subtypeCache = subTypeCache;
    }

    /**
     * Reads the value of a field given its index.
     * <p>
     * Note: This method is prone to exceptions (there are currently 5 total throw statements). It is recommended that you
     * use {@link #readSafely(int)}, which returns {@code null} if the field doesn't exist, instead of throwing an
     * exception.
     *
     * @param fieldIndex - index of the field.
     * @return Value of the field.
     * @throws FieldAccessException  if the given field index is out of bounds.
     * @throws IllegalStateException if this modifier has no target set.
     */
    public T read(int fieldIndex) throws FieldAccessException {
        FieldAccessor accessor = this.findFieldAccessor(fieldIndex);
        if (accessor == null) {
            throw FieldAccessException.fromFormat(
                    "Field index %d is out of bounds for length %s",
                    fieldIndex,
                    this.accessors.size());
        }

        return this.readInternal(accessor);
    }

    /**
     * Reads the value of a field only if it exists. If the field does not exist, {@code null} is returned.
     * <p>
     * As its name implies, this method is a much safer alternative to {@link #read(int)}. In addition to throwing less
     * exceptions and thereby causing less console spam, this method makes providing backwards compatiblity signficiantly
     * easier, as shown below:
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
     * @throws IllegalStateException if this modifier has no target set.
     */
    public T readSafely(int fieldIndex) throws FieldAccessException {
        return this.readInternal(this.findFieldAccessor(fieldIndex));
    }

    /**
     * Reads the value of a field only if it exists. If the field does not exist, an empty {@link Optional} is returned.
     * <p>
     * This method has the same functionality as {@link #readSafely(int)}, but enforces null checks by way of an Optional.
     * It will eventually become the preferred method of reading fields.
     *
     * @param fieldIndex index of the field
     * @return An optional that may contain the value of the field
     * @see #readSafely(int)
     */
    public Optional<T> optionRead(int fieldIndex) {
        return Optional.ofNullable(this.readSafely(fieldIndex));
    }

    @SuppressWarnings("unchecked")
    private T readInternal(FieldAccessor accessor) {
        // just return null if the accessor is null
        if (accessor == null) {
            return null;
        }

        // get the field value and convert it if needed
        Object fieldValue = accessor.get(this.target);
        return this.needConversion() ? this.converter.getSpecific(fieldValue) : (T) fieldValue;
    }

    /**
     * Writes the value of a field given its index.
     *
     * @param fieldIndex - index of the field.
     * @param value      - new value of the field.
     * @return This structure modifier - for chaining.
     * @throws FieldAccessException The field doesn't exist, or it cannot be accessed under the current security
     *                              contraints.
     */
    public StructureModifier<T> write(int fieldIndex, T value) throws FieldAccessException {
        FieldAccessor accessor = this.findFieldAccessor(fieldIndex);
        if (accessor == null) {
            throw FieldAccessException.fromFormat(
                    "Field index %d is out of bounds for length %s",
                    fieldIndex,
                    this.accessors.size());
        }

        return this.writeInternal(accessor, value);
    }

    /**
     * Writes the value of a given field IF and ONLY if it exists.
     *
     * @param fieldIndex - index of the potential field.
     * @param value      - new value of the field.
     * @return This structure modifer - for chaining.
     * @throws FieldAccessException The field cannot be accessed under the current security contraints.
     */
    public StructureModifier<T> writeSafely(int fieldIndex, T value) throws FieldAccessException {
        FieldAccessor accessor = this.findFieldAccessor(fieldIndex);
        return this.writeInternal(accessor, value);
    }

    /**
     * Correctly modifies the value of a field.
     *
     * @param fieldIndex - index of the field to modify.
     * @param select     - the function that modifies the field value.
     * @return This structure modifier - for chaining.
     * @throws FieldAccessException The field cannot be accessed under the current security contraints.
     */
    public StructureModifier<T> modify(int fieldIndex, UnaryOperator<T> select) throws FieldAccessException {
        T value = this.read(fieldIndex);
        return this.write(fieldIndex, select.apply(value));
    }

    private StructureModifier<T> writeInternal(FieldAccessor accessor, T value) throws FieldAccessException {
        // just ignore if the accessor is not present
        if (accessor == null) {
            return this;
        }

        // convert and write
        Object fieldValue = this.needConversion() ? this.converter.getGeneric(value) : value;
        accessor.set(this.target, fieldValue);

        return this;
    }

    protected FieldAccessor findFieldAccessor(int fieldIndex) {
        if (this.target == null) {
            throw new IllegalStateException("Cannot read from modifier which has no target!");
        }

        // check if the field is out of bounds
        if (fieldIndex < 0 || fieldIndex >= this.accessors.size()) {
            return null;
        }

        return this.accessors.get(fieldIndex);
    }

    /**
     * Whether we should use the converter instance.
     *
     * @return TRUE if we should, FALSE otherwise.
     */
    private boolean needConversion() {
        return this.converter != null && !this.customConvertHandling;
    }

    /**
     * Sets all non-primitive fields to a more fitting default value. See {@link DefaultInstances#getDefault(Class)}.
     *
     * @return The current structure modifier - for chaining.
     * @throws FieldAccessException If we're unable to write to the fields due to a security limitation.
     */
    public StructureModifier<T> writeDefaults() throws FieldAccessException {
        // Write a default instance to every field
        for (FieldAccessor accessor : this.defaultFields.keySet()) {
            // Special case for Spigot's custom chat components
            // They must be null or messages will be blank
            Field field = accessor.getField();
            if (field.getType().getCanonicalName().equals("net.md_5.bungee.api.chat.BaseComponent[]")) {
                accessor.set(this.target, null);
                continue;
            }

            // get the default value and write the field
            Object defaultValue = DEFAULT_GENERATOR.getDefault(field.getType());
            accessor.set(this.target, defaultValue);
        }

        return this;
    }

    /**
     * Retrieves the common type of each field.
     *
     * @return Common type of each field.
     */
    public Class<?> getFieldType() {
        return this.fieldType;
    }

    /**
     * Retrieves the type of the object we're modifying.
     *
     * @return Type of the object.
     */
    public Class<?> getTargetType() {
        return this.targetType;
    }

    /**
     * Retrieves the object we're currently modifying.
     *
     * @return Object we're modifying.
     */
    public Object getTarget() {
        return this.target;
    }

    /**
     * Retrieve the number of readable types.
     *
     * @return Readable types.
     */
    public int size() {
        return this.accessors.size();
    }

    /**
     * Retrieves a list of the fields matching the constraints of this structure modifier.
     *
     * @return List of fields.
     */
    public List<FieldAccessor> getFields() {
        return Collections.unmodifiableList(this.accessors);
    }

    /**
     * Retrieve a field by index.
     *
     * @param fieldIndex - index of the field to retrieve.
     * @return The field represented with the given index.
     * @throws IllegalArgumentException If no field with the given index can be found.
     */
    public Field getField(int fieldIndex) {
        FieldAccessor accessor = this.findFieldAccessor(fieldIndex);
        if (accessor == null) {
            throw new IllegalArgumentException(String.format(
                    "Field index %d is out of bounds for length %s",
                    fieldIndex,
                    this.accessors.size()));
        }

        return accessor.getField();
    }

    /**
     * Retrieve every value stored in the fields of the current type.
     *
     * @return Every field value.
     * @throws FieldAccessException Unable to access one or all of the fields
     */
    public List<T> getValues() throws FieldAccessException {
        List<T> values = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            values.add(this.readSafely(i));
        }

        return values;
    }

    /**
     * Retrieves a structure modifier that only reads and writes fields of a given type.
     *
     * @param <R>       Type
     * @param fieldType - the type, or supertype, of every field to modify.
     * @return A structure modifier for fields of this type.
     */
    public <R> StructureModifier<R> withType(Class<?> fieldType) {
        return this.withType(fieldType, null);
    }

    /**
     * Retrieves a structure modifier that only reads and writes fields of a given type.
     *
     * @param <R>       Type
     * @param fieldType - the type, or supertype, of every field to modify.
     * @param converter - converts objects into the given type.
     * @return A structure modifier for fields of this type.
     */
    public <R> StructureModifier<R> withType(Class<?> fieldType, EquivalentConverter<R> converter) {
        return this.withParamType(fieldType, converter);
    }

    /**
     * Retrieves a structure modifier that only reads and writes fields of a given type.
     *
     * @param <R>        Type
     * @param fieldType  - the type, or supertype, of every field to modify.
     * @param converter  - converts objects into the given type.
     * @param paramTypes - field type parameters
     * @return A structure modifier for fields of this type.
     */
    @SuppressWarnings("unchecked")
    public <R> StructureModifier<R> withParamType(
            Class<?> fieldType,
            EquivalentConverter<R> converter,
            Class<?>... paramTypes
    ) {
        if (fieldType == null) {
            // It's not supported in this version, so return an empty modifier
            return (StructureModifier<R>) NO_OP_MODIFIER;
        }

        // Do we need to update the cache?
        StructureModifier<R> result = (StructureModifier<R>) this.subtypeCache.get(fieldType);
        if (result == null) {
            List<FieldAccessor> fields = new ArrayList<>();
            Map<FieldAccessor, Integer> defaults = new HashMap<>();

            // filter out all fields we don't need
            for (int i = 0; i < this.accessors.size(); i++) {
                FieldAccessor accessor = this.accessors.get(i);
                Field field = accessor.getField();

                // check if the field type matches
                if (!fieldType.isAssignableFrom(field.getType())) {
                    continue;
                }

                // check if we need to check for parameters
                if (paramTypes.length > 0) {
                    // check if the field is parameterized
                    Type generic = field.getGenericType();
                    if (!(generic instanceof ParameterizedType)) {
                        continue;
                    }

                    // check if the type arguments of the field are matching
                    ParameterizedType parameterized = (ParameterizedType) generic;
                    if (!Arrays.equals(parameterized.getActualTypeArguments(), paramTypes)) {
                        continue;
                    }
                }

                // this field should be included
                fields.add(accessor);
                if (this.defaultFields.containsKey(accessor)) {
                    defaults.put(accessor, i);
                }
            }

            // Cache structure modifiers
            result = this.withFieldType(fieldType, fields, defaults);
            this.subtypeCache.put(fieldType, result);
        }

        // Add the target too
        result = result.withTarget(this.target);
        result.converter = converter;

        return result;
    }

    /**
     * Create a new structure modifier for the new field type.
     *
     * @param <V>       Type
     * @param fieldType - common type of each field.
     * @param filtered  - list of fields after filtering the original modifier.
     * @param defaults  - list of default values after filtering the original.
     * @return A new structure modifier.
     */
    protected <V> StructureModifier<V> withFieldType(
            Class<?> fieldType,
            List<FieldAccessor> filtered,
            Map<FieldAccessor, Integer> defaults
    ) {
        return this.withFieldType(fieldType, filtered, defaults, null);
    }

    /**
     * Create a new structure modifier for the new field type.
     *
     * @param <V>       Type
     * @param fieldType - common type of each field.
     * @param filtered  - list of fields after filtering the original modifier.
     * @param defaults  - list of default values after filtering the original.
     * @param converter - the new converter, or NULL.
     * @return A new structure modifier.
     */
    @SuppressWarnings("SameParameterValue") // api method, maybe someone needs it
    protected <V> StructureModifier<V> withFieldType(
            Class<?> fieldType,
            List<FieldAccessor> filtered,
            Map<FieldAccessor, Integer> defaults,
            EquivalentConverter<V> converter
    ) {
        StructureModifier<V> result = new StructureModifier<>();
        result.initialize(
                this.targetType,
                fieldType,
                filtered,
                defaults,
                converter,
                new HashMap<>());
        return result;
    }

    /**
     * Retrieves a structure modifier of the same type for a different object target.
     *
     * @param target - different target of the same type.
     * @return Structure modifier with the new target.
     */
    public StructureModifier<T> withTarget(Object target) {
        StructureModifier<T> copy = new StructureModifier<>();

        // Create a new instance
        copy.initialize(this);
        copy.target = target;
        return copy;
    }

    /**
     * Retrieves a structure modifier with the same type and target, but using a new object converter.
     *
     * @param converter - the object converter to use.
     * @return Structure modifier with the new converter.
     */
    @SuppressWarnings("unchecked")
    private <V> StructureModifier<V> withConverter(EquivalentConverter<V> converter) {
        StructureModifier<V> copy = (StructureModifier<V>) this.withTarget(this.target);
        copy.setConverter(converter);
        return copy;
    }

    /**
     * Set the current object converter. Should only be called during construction.
     *
     * @param converter - current object converter.
     */
    protected void setConverter(EquivalentConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    public String toString() {
        return "StructureModifier[fieldType=" + this.fieldType + ", data=" + this.accessors + "]";
    }
}
