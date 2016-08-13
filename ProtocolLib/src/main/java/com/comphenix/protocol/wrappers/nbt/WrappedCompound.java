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

package com.comphenix.protocol.wrappers.nbt;

import java.io.DataOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.comphenix.protocol.wrappers.collection.ConvertedMap;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;

/**
 * A concrete implementation of an NbtCompound that wraps an underlying NMS Compound.
 * 
 * @author Kristian
 */
class WrappedCompound implements NbtWrapper<Map<String, NbtBase<?>>>, NbtCompound {
	// A list container
	private WrappedElement<Map<String, Object>> container;
	
	// Saved wrapper map
	private ConvertedMap<String, Object, NbtBase<?>> savedMap;
	
	/**
	 * Construct a new NBT compound wrapper.
	 * @param name - the name of the wrapper.
	 * @return The wrapped NBT compound.
	 */
	public static WrappedCompound fromName(String name) {
		// Simplify things for the caller
		return (WrappedCompound) NbtFactory.<Map<String, NbtBase<?>>>ofWrapper(NbtType.TAG_COMPOUND, name);
	}
	
	/**
	 * Construct a new NBT compound wrapper initialized with a given list of NBT values.
	 * @param name - the name of the compound wrapper. 
	 * @param list - the list of elements to add.
	 * @return The new wrapped NBT compound.
	 */
	public static NbtCompound fromList(String name, Collection<? extends NbtBase<?>> list) {
		WrappedCompound copy = fromName(name);
		
		for (NbtBase<?> base : list)
			copy.getValue().put(base.getName(), base);
		return copy;
	}
	
	/**
	 * Construct a wrapped compound from a given NMS handle.
	 * @param handle - the NMS handle.
	 */
	public WrappedCompound(Object handle) {
		this.container = new WrappedElement<Map<String,Object>>(handle);
	}

	/**
	 * Construct a wrapped compound from a given NMS handle.
	 * @param handle - the NMS handle.
	 * @param name - the name of the current compound.
	 */
	public WrappedCompound(Object handle, String name) {
		this.container = new WrappedElement<Map<String,Object>>(handle, name);
	}
	
	@Override
	public boolean accept(NbtVisitor visitor) {
		// Enter this node?
		if (visitor.visitEnter(this)) {
			for (NbtBase<?> node : this) {
				if (!node.accept(visitor))
					break;
			}
		}
		
		return visitor.visitLeave(this);
	}

	@Override
	public Object getHandle() {
		return container.getHandle();
	}
	
	@Override
	public NbtType getType() {
		return NbtType.TAG_COMPOUND;
	}

	@Override
	public String getName() {
		return container.getName();
	}

	@Override
	public void setName(String name) {
		container.setName(name);
	}
	
	/**
	 * Determine if an entry with the given key exists or not.
	 * @param key - the key to lookup. 
	 */
	@Override
	public boolean containsKey(String key) {
		return getValue().containsKey(key);
	}
	
	/**
	 * Retrieve a Set view of the keys of each entry in this compound. 
	 * @return The keys of each entry.
	 */
	@Override
	public Set<String> getKeys() {
		return getValue().keySet();
	}
	
	@Override
	public Map<String, NbtBase<?>> getValue() {
		// Return a wrapper map
		if (savedMap == null) {
			savedMap = new ConvertedMap<String, Object, NbtBase<?>>(container.getValue()) {
				@Override
				protected Object toInner(NbtBase<?> outer) {
					if (outer == null) 
						return null;
					return NbtFactory.fromBase(outer).getHandle();
				}
				
				@SuppressWarnings("deprecation")
				protected NbtBase<?> toOuter(Object inner) {
					if (inner == null)
						return null;
					return NbtFactory.fromNMS(inner);
				};
				
				@Override
				protected NbtBase<?> toOuter(String key, Object inner) {
					if (inner == null)
						return null;
					return NbtFactory.fromNMS(inner, key);
				}
				
				@Override
				public String toString() {
					return WrappedCompound.this.toString();
				}
			};
		}
		return savedMap;
	}

	@Override
	public void setValue(Map<String, NbtBase<?>> newValue) {
		// Write all the entries
		for (Map.Entry<String, NbtBase<?>> entry : newValue.entrySet()) {
			Object value = entry.getValue();
			
			// We don't really know
			if (value instanceof NbtBase)
				put(entry.getValue());
			else
				putObject(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Retrieve the value of a given entry.
	 * @param key - key of the entry to retrieve.
	 * @return The value of this entry, or NULL if not found.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> NbtBase<T> getValue(String key) {
		return (NbtBase<T>) getValue().get(key);
	}
	
	/**
	 * Retrieve a value by its key, or assign and return a new NBT element if it doesn't exist.
	 * @param key - the key of the entry to find or create.
	 * @param type - the NBT element we will create if not found.
	 * @return The value that was retrieved or just created.
	 */
	@Override
	public NbtBase<?> getValueOrDefault(String key, NbtType type) {
		NbtBase<?> nbt = getValue(key);

		// Create or get a compound
		if (nbt == null) 
			put(nbt = NbtFactory.ofWrapper(type, key));
		else if (nbt.getType() != type) 
			throw new IllegalArgumentException("Cannot get tag " + nbt + ": Not a " + type);
		
		return nbt;
	}
	
	/**
	 * Retrieve a value, or throw an exception.
	 * @param key - the key to retrieve.
	 * @return The value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	private <T> NbtBase<T> getValueExact(String key) {
		NbtBase<T> value = getValue(key);
		
		// Only return a legal key
		if (value != null)
			return value;
		else
			throw new IllegalArgumentException("Cannot find key " + key);
	}
	
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public NbtBase<Map<String, NbtBase<?>>> deepClone() {
		return (NbtBase) container.deepClone();
	}
	
	/**
	 * Set a entry based on its name.
	 * @param entry - entry with a name and value.
	 * @return This compound, for chaining.
	 */
	@Override
	public <T> NbtCompound put(NbtBase<T> entry) {
		if (entry == null)
			throw new IllegalArgumentException("Entry cannot be NULL.");
		
		getValue().put(entry.getName(), entry);
		return this;
	}
	
	/**
	 * Retrieve the string value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The string value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public String getString(String key) {
		return (String) getValueExact(key).getValue();
	}
	
	/**
	 * Retrieve the string value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
	@Override
	public String getStringOrDefault(String key) {
		return (String) getValueOrDefault(key, NbtType.TAG_STRING).getValue();
	}
	
	/**
	 * Associate a NBT string value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, String value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	@Override
	public NbtCompound putObject(String key, Object value) {
		if (value == null) {
			remove(key);
		} else if (value instanceof NbtBase) {
			put(key, (NbtBase<?>) value);
		} else {
			NbtBase<?> base = new MemoryElement<Object>(key, value);
			put(base);
		}
		return this;
	}
	
	@Override
	public Object getObject(String key) {
		NbtBase<?> base = getValue(key);
		
		if (base != null && base.getType() != NbtType.TAG_LIST && base.getType() != NbtType.TAG_COMPOUND)
			return base.getValue();
		else
			return base;
	}
	
	/**
	 * Retrieve the byte value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The byte value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public byte getByte(String key) {
		return (Byte) getValueExact(key).getValue();
	}
	
	/**
	 * Retrieve the byte value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
	@Override
	public byte getByteOrDefault(String key) {
		return (Byte) getValueOrDefault(key, NbtType.TAG_BYTE).getValue();
	}
	
	/**
	 * Associate a NBT byte value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, byte value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the short value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The short value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public Short getShort(String key) {
		return (Short) getValueExact(key).getValue();
	}
	
	/**
	 * Retrieve the short value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
	@Override
	public short getShortOrDefault(String key) {
		return (Short) getValueOrDefault(key, NbtType.TAG_SHORT).getValue();
	}
	
	/**
	 * Associate a NBT short value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, short value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the integer value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The integer value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public int getInteger(String key) {
		return (Integer) getValueExact(key).getValue();
	}
	
	/**
	 * Retrieve the integer value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
	@Override
	public int getIntegerOrDefault(String key) {
		return (Integer) getValueOrDefault(key, NbtType.TAG_INT).getValue();
	}
	
	/**
	 * Associate a NBT integer value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, int value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the long value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The long value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public long getLong(String key) {
		return (Long) getValueExact(key).getValue();
	}
	
	/**
	 * Retrieve the long value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
	@Override
	public long getLongOrDefault(String key) {
		return (Long) getValueOrDefault(key, NbtType.TAG_LONG).getValue();
	}
	
	/**
	 * Associate a NBT long value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, long value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the float value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The float value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public float getFloat(String key) {
		return (Float) getValueExact(key).getValue();
	}
	
	/**
	 * Retrieve the float value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
	@Override
	public float getFloatOrDefault(String key) {
		return (Float) getValueOrDefault(key, NbtType.TAG_FLOAT).getValue();
	}
	
	/**
	 * Associate a NBT float value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, float value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the double value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The double value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public double getDouble(String key) {
		return (Double) getValueExact(key).getValue();
	}
	
	/**
	 * Retrieve the double value of an existing entry, or from a new default entry if it doesn't exist.
	 * @param key - the key of the entry.
	 * @return The value that was retrieved or just created.
	 */
	@Override
	public double getDoubleOrDefault(String key) {
		return (Double) getValueOrDefault(key, NbtType.TAG_DOUBLE).getValue();
	}
	
	/**
	 * Associate a NBT double value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, double value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the byte array value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The byte array value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public byte[] getByteArray(String key) {
		return (byte[]) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT byte array value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, byte[] value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the integer array value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The integer array value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	public int[] getIntegerArray(String key) {
		return (int[]) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT integer array value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(String key, int[] value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the compound (map) value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The compound value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public NbtCompound getCompound(String key) {
		return (NbtCompound) ((NbtBase) getValueExact(key));
	}
	
	/**
	 * Retrieve a compound (map) value by its key, or create a new compound if it doesn't exist.
	 * @param key - the key of the entry to find or create.
	 * @return The compound value that was retrieved or just created.
	 */
	@Override
	public NbtCompound getCompoundOrDefault(String key) {
		return (NbtCompound) getValueOrDefault(key, NbtType.TAG_COMPOUND);
	}
		
	/**
	 * Associate a NBT compound with its name as key.
	 * @param compound - the compound value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public NbtCompound put(NbtCompound compound) {
		getValue().put(compound.getName(), compound);
		return this;
	}
	
	/**
	 * Retrieve the NBT list value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The NBT list value of the entry.
	 * @throws IllegalArgumentException If the key doesn't exist.
	 */
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> NbtList<T> getList(String key) {
		return (NbtList) getValueExact(key);
	}
	
	/**
	 * Retrieve a NBT list value by its key, or create a new list if it doesn't exist.
	 * @param key - the key of the entry to find or create.
	 * @return The compound value that was retrieved or just created.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> NbtList<T> getListOrDefault(String key) {
		return (NbtList<T>) getValueOrDefault(key, NbtType.TAG_LIST);
	}
	
	/**
	 * Associate a NBT list with the given key.
	 * @param list - the list value.
	 * @return This current compound, for chaining.
	 */
	@Override
	public <T> NbtCompound put(NbtList<T> list) {
		getValue().put(list.getName(), list);
		return this;
	}
	
	@Override
	public NbtCompound put(String key, NbtBase<?> entry) {
		if (entry == null)
			throw new IllegalArgumentException("Entry cannot be NULL.");
		
		// Don't modify the original NBT
		NbtBase<?> clone = entry.deepClone();
		
		clone.setName(key);
		return put(clone);
	}
	
	/**
	 * Associate a new NBT list with the given key.
	 * @param key - the key and name of the new NBT list.
	 * @param list - the list of NBT elements.
	 * @return This current compound, for chaining.
	 */
	@Override
	public <T> NbtCompound put(String key, Collection<? extends NbtBase<T>> list) {
		return put(WrappedList.fromList(key, list));
	}
	
	@Override
	public NbtBase<?> remove(String key) {
		return getValue().remove(key);
	}
	
	@Override
	public void write(DataOutput destination) {
		NbtBinarySerializer.DEFAULT.serialize(container, destination);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WrappedCompound) {
			WrappedCompound other = (WrappedCompound) obj;
			return container.equals(other.container);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return container.hashCode();
	}

	@Override
	public Iterator<NbtBase<?>> iterator() {
		return getValue().values().iterator();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("{");
		builder.append("\"name\": \"" + getName() + "\"");
		
		for (NbtBase<?> element : this) {
			builder.append(", ");
			
			// Wrap in quotation marks
			if (element.getType() == NbtType.TAG_STRING)
				builder.append("\"" + element.getName() + "\": \"" + element.getValue() + "\"");
			else
				builder.append("\"" + element.getName() + "\": " + element.getValue());
		}
		
		builder.append("}");
		return builder.toString();
	}
}
