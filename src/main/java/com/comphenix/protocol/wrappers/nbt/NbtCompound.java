package com.comphenix.protocol.wrappers.nbt;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Represents a mapping of arbitrary NBT elements and their unique names.
 * <p>
 * Use {@link NbtFactory} to load or create an instance.
 * <p>
 * The {@link NbtBase#getValue()} method returns a {@link java.util.Map} that will return the full content
 * of this NBT compound, but may throw an {@link UnsupportedOperationException} for any of the write operations.
 * 
 * @author Kristian
 */
public interface NbtCompound extends NbtBase<Map<String, NbtBase<?>>>, Iterable<NbtBase<?>> {
	@Override
	@Deprecated
    Map<String, NbtBase<?>> getValue();
	
	/**
	 * Determine if an entry with the given key exists or not.
	 * @param key - the key to lookup.
	 * @return TRUE if an entry with the given key exists, FALSE otherwise.
	 */
    boolean containsKey(String key);

	/**
	 * Retrieve a Set view of the keys of each entry in this compound.
	 * @return The keys of each entry.
	 */
    Set<String> getKeys();

	/**
	 * Retrieve the value of a given entry.
	 * @param <T> Type
	 * @param key - key of the entry to retrieve.
	 * @return The value of this entry, or NULL if not found.
	 */
    <T> NbtBase<T> getValue(String key);

	/**
	 * Retrieve a value by its key, or assign and return a new NBT element if it doesn't exist.
	 * @param key - the key of the entry to find or create.
	 * @param type - the NBT element we will create if not found.
	 * @return The value that was retrieved or just created.
	 */
    NbtBase<?> getValueOrDefault(String key, NbtType type);

	/**
	 * Set a entry based on its name.
	 * @param <T> Type
	 * @param entry - entry with a name and value.
	 * @return This compound, for chaining.
	 * @throws IllegalArgumentException If entry is NULL.
	 */
    <T> NbtCompound put(@Nonnull NbtBase<T> entry);

	/**
	 * Retrieve the string value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The string value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    String getString(String key);

	/**
	 * Retrieve the string value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
    String getStringOrDefault(String key);

	/**
	 * Associate a NBT string value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, String value);
	
	/**
	 * Inserts an entry after cloning it and renaming it to "key".
	 * @param key - the name of the entry.
	 * @param entry - the entry to insert.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, NbtBase<?> entry);

	/**
	 * Retrieve the byte value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The byte value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    byte getByte(String key);

	/**
	 * Retrieve the byte value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
    byte getByteOrDefault(String key);

	/**
	 * Associate a NBT byte value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, byte value);

	/**
	 * Retrieve the short value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The short value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    Short getShort(String key);

	/**
	 * Retrieve the short value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
    short getShortOrDefault(String key);

	/**
	 * Associate a NBT short value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, short value);

	/**
	 * Retrieve the integer value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The integer value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    int getInteger(String key);

	/**
	 * Retrieve the integer value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
    int getIntegerOrDefault(String key);

	/**
	 * Associate a NBT integer value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, int value);

	/**
	 * Retrieve the long value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The long value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    long getLong(String key);

	/**
	 * Retrieve the long value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
    long getLongOrDefault(String key);

	/**
	 * Associate a NBT long value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, long value);

	/**
	 * Retrieve the float value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The float value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    float getFloat(String key);

	/**
	 * Retrieve the float value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
    float getFloatOrDefault(String key);

	/**
	 * Associate a NBT float value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, float value);

	/**
	 * Retrieve the double value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The double value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    double getDouble(String key);

	/**
	 * Retrieve the double value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
    double getDoubleOrDefault(String key);

	/**
	 * Associate a NBT double value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, double value);

	/**
	 * Retrieve the byte array value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The byte array value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    byte[] getByteArray(String key);

	/**
	 * Associate a NBT byte array value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, byte[] value);

	/**
	 * Retrieve the integer array value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The integer array value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    int[] getIntegerArray(String key);

	/**
	 * Associate a NBT integer array value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(String key, int[] value);
	
	/**
	 * Associates a given Java primitive value, list, map or NbtBase with a certain key.
	 * <p>
	 * If the value is NULL, the corresponding key is removed. Any Map or List will be converted
	 * to a corresponding NbtCompound or NbtList.
	 * 
	 * @param key - the name of the new entry,
	 * @param value - the value of the new entry, or NULL to remove the current value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound putObject(String key, Object value);
	
	/**
	 * Retrieve the primitive object, NbtList or NbtCompound associated with the given key.
	 * @param key - the key of the object to find.
	 * @return The object with this key, or NULL if we couldn't find anything.
	 */
    Object getObject(String key);
	
	/**
	 * Retrieve the compound (map) value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The compound value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    NbtCompound getCompound(String key);

	/**
	 * Retrieve a compound (map) value by its key, or create a new compound if it doesn't exist.
	 * @param key - the key of the entry to find or create.
	 * @return The compound value that was retrieved or just created.
	 */
    NbtCompound getCompoundOrDefault(String key);

	/**
	 * Associate a NBT compound with its name as key.
	 * @param compound - the compound value.
	 * @return This current compound, for chaining.
	 */
    NbtCompound put(NbtCompound compound);

	/**
	 * Retrieve the NBT list value of an entry identified by a given key.
	 * @param <T> Type
	 * @param key - the key of the entry.
	 * @return The NBT list value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
    <T> NbtList<T> getList(String key);

	/**
	 * Retrieve a NBT list value by its key, or create a new list if it doesn't exist.
	 * @param <T> Type
	 * @param key - the key of the entry to find or create.
	 * @return The compound value that was retrieved or just created.
	 */
    <T> NbtList<T> getListOrDefault(String key);

	/**
	 * Associate a NBT list with the given key.
	 * @param <T> Type
	 * @param list - the list value.
	 * @return This current compound, for chaining.
	 */
    <T> NbtCompound put(NbtList<T> list);

	/**
	 * Associate a new NBT list with the given key.
	 * @param <T> Type
	 * @param key - the key and name of the new NBT list.
	 * @param list - the list of NBT elements.
	 * @return This current compound, for chaining.
	 */
    <T> NbtCompound put(String key, Collection<? extends NbtBase<T>> list);

	/**
	 * Remove the NBT element that is associated with the given key.
	 * @param <T> Type
	 * @param key - the key of the element to remove.
	 * @return The removed element, or NULL if no such element was found.
	 */
    <T> NbtBase<?> remove(String key);
	
	/**
	 * Retrieve an iterator view of the NBT tags stored in this compound.
	 * @return The tags stored in this compound.
	 */
	@Override
    Iterator<NbtBase<?>> iterator();
}
