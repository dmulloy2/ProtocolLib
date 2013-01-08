package com.comphenix.protocol.wrappers.nbt;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of NBT tags of the same type without names.
 * <p>
 * Use {@link NbtFactory} to load or create an instance.
 * 
 * @author Kristian
 *
 * @param <TType> - the value type of each NBT tag.
 */
public interface NbtList<TType> extends NbtBase<List<NbtBase<TType>>>, Iterable<TType> {
	/**
	 * The name of every NBT tag in a list.
	 */
	public static String EMPTY_NAME = "";
	
	/**
	 * Get the type of each element.
	 * @return Element type.
	 */
	public abstract NbtType getElementType();

	/**
	 * Add a NBT list or NBT compound to the list.
	 * @param element
	 */
	public abstract void add(NbtBase<TType> element);

	/**
	 * Add a new string element to the list. 
	 * @param value - the string element to add.
	 * @throws IllegalArgumentException If this is not a list of strings.
	 */
	public abstract void add(String value);

	/**
	 * Add a new byte element to the list. 
	 * @param value - the byte element to add.
	 * @throws IllegalArgumentException If this is not a list of bytes.
	 */
	public abstract void add(byte value);

	/**
	 * Add a new short element to the list. 
	 * @param value - the short element to add.
	 * @throws IllegalArgumentException If this is not a list of shorts.
	 */
	public abstract void add(short value);

	/**
	 * Add a new integer element to the list. 
	 * @param value - the string element to add.
	 * @throws IllegalArgumentException If this is not a list of integers.
	 */
	public abstract void add(int value);

	/**
	 * Add a new long element to the list. 
	 * @param value - the string element to add.
	 * @throws IllegalArgumentException If this is not a list of longs.
	 */
	public abstract void add(long value);

	/**
	 * Add a new double element to the list. 
	 * @param value - the double element to add.
	 * @throws IllegalArgumentException If this is not a list of doubles.
	 */
	public abstract void add(double value);

	/**
	 * Add a new byte array element to the list. 
	 * @param value - the byte array element to add.
	 * @throws IllegalArgumentException If this is not a list of byte arrays.
	 */
	public abstract void add(byte[] value);

	/**
	 * Add a new int array element to the list. 
	 * @param value - the int array element to add.
	 * @throws IllegalArgumentException If this is not a list of int arrays.
	 */
	public abstract void add(int[] value);

	/**
	 * Remove a given object from the list.
	 * @param remove - the object to remove.
	 */
	public abstract void remove(Object remove);

	/**
	 * Retrieve an element by index.
	 * @param index - index of the element to retrieve.
	 * @return The element to retrieve.
	 * @throws IndexOutOfBoundsException If the index is out of range (index < 0 || index >= size())
	 */
	public abstract TType getValue(int index);
	
	/**
	 * Retrieve the number of elements in this list.
	 * @return The number of elements in this list.
	 */
	public abstract int size();

	/**
	 * Retrieve each NBT tag in this list.
	 * @return A view of NBT tag in this list.
	 */
	public abstract Collection<NbtBase<TType>> asCollection();

	/**
	 * Iterate over all the elements in this list.
	 */
	public abstract Iterator<TType> iterator();
}