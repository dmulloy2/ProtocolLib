package com.comphenix.protocol.wrappers.nbt;

import java.io.DataOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a mapping of arbitrary NBT elements and their unique names.
 * <p>
 * Use {@link NbtFactory} to load or create an instance.
 * 
 * @author Kristian
 */
public class NbtCompound implements NbtWrapper<Map<String, NbtBase<?>>>, Iterable<NbtBase<?>> {
	// A list container
	private NbtElement<Map<String, Object>> container;
	
	// Saved wrapper map
	private ConvertedMap<String, Object, NbtBase<?>> savedMap;
	
	/**
	 * Construct a new NBT compound wrapper.
	 * @param name - the name of the wrapper.
	 * @return The wrapped NBT compound.
	 */
	public static NbtCompound fromName(String name) {
		// Simplify things for the caller
		return (NbtCompound) NbtFactory.<Map<String, NbtBase<?>>>ofType(NbtType.TAG_COMPOUND, name);
	}
	
	/**
	 * Construct a new NBT compound wrapper initialized with a given list of NBT values.
	 * @param name - the name of the compound wrapper. 
	 * @param list - the list of elements to add.
	 * @return The new wrapped NBT compound.
	 */
	public static <T> NbtCompound fromList(String name, Collection<? extends NbtBase<T>> list) {
		NbtCompound copy = new NbtCompound(name);
		
		for (NbtBase<T> base : list)
			copy.getValue().put(base.getName(), base);
		return copy;
	}
	
	/**
	 * Construct a wrapped compound from a given NMS handle.
	 * @param handle - the NMS handle.
	 */
	NbtCompound(Object handle) {
		this.container = new NbtElement<Map<String,Object>>(handle);
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
	 * Retrieve a Set view of the keys of each entry in this compound. 
	 * @return The keys of each entry.
	 */
	public Set<String> getKeys() {
		return getValue().keySet();
	}
	
	/**
	 * Retrieve a Collection view of the entries in this compound. 
	 * @return A view of each NBT tag in this compound.
	 */
	public Collection<NbtBase<?>> asCollection(){
		return getValue().values();
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
				
				protected NbtBase<?> toOuter(Object inner) {
					if (inner == null)
						return null;
					return NbtFactory.fromNMS(inner);
				};
				
				@Override
				public String toString() {
					return NbtCompound.this.toString();
				}
			};
		}
		return savedMap;
	}

	@Override
	public void setValue(Map<String, NbtBase<?>> newValue) {
		// Write all the entries
		for (Map.Entry<String, NbtBase<?>> entry : newValue.entrySet()) {
			put(entry.getValue());
		}
	}
	
	/**
	 * Retrieve the value of a given entry.
	 * @param key - key of the entry to retrieve.
	 * @return The value of this entry.
	 */
	@SuppressWarnings("unchecked")
	public <T> NbtBase<T> getValue(String key) {
		return (NbtBase<T>) getValue().get(key);
	}
	
	/**
	 * Retrieve a value, or throw an exception.
	 * @param key - the key to retrieve.
	 * @return The value of the entry.
	 */
	private <T> NbtBase<T> getValueExact(String key) {
		NbtBase<T> value = getValue(key);
		
		// Only return a legal key
		if (value != null)
			return value;
		else
			throw new IllegalArgumentException("Cannot find key " + key);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public NbtBase<Map<String, NbtBase<?>>> clone() {
		return (NbtBase) container.clone();
	}
	
	/**
	 * Set a entry based on its name.
	 * @param entry - entry with a name and value.
	 * @return This compound, for chaining.
	 */
	public <T> NbtCompound put(NbtBase<T> entry) {
		getValue().put(entry.getName(), entry);
		return this;
	}
	
	/**
	 * Retrieve the string value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The string value of the entry.
	 */
	public String getString(String key) {
		return (String) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT string value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, String value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the byte value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The byte value of the entry.
	 */
	public Byte getByte(String key) {
		return (Byte) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT byte value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, byte value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the short value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The short value of the entry.
	 */
	public Short getShort(String key) {
		return (Short) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT short value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, short value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the integer value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The integer value of the entry.
	 */
	public Integer getInteger(String key) {
		return (Integer) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT integer value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, int value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the long value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The long value of the entry.
	 */
	public Long getLong(String key) {
		return (Long) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT long value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, long value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the float value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The float value of the entry.
	 */
	public Float getFloat(String key) {
		return (Float) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT float value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, float value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the double value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The double value of the entry.
	 */
	public Double getDouble(String key) {
		return (Double) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT double value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, double value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the byte array value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The byte array value of the entry.
	 */
	public byte[] getByteArray(String key) {
		return (byte[]) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT byte array value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, byte[] value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the integer array value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The integer array value of the entry.
	 */
	public int[] getIntegerArray(String key) {
		return (int[]) getValueExact(key).getValue();
	}
	
	/**
	 * Associate a NBT integer array value with the given key.
	 * @param key - the key and NBT name.
	 * @param value - the value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(String key, int[] value) {
		getValue().put(key, NbtFactory.of(key, value));
		return this;
	}
	
	/**
	 * Retrieve the compound (map) value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The compound value of the entry.
	 */
	@SuppressWarnings("rawtypes")
	public NbtCompound getCompound(String key) {
		return (NbtCompound) ((NbtBase) getValueExact(key));
	}
	
	/**
	 * Associate a NBT compound with its name as key.
	 * @param compound - the compound value.
	 * @return This current compound, for chaining.
	 */
	public NbtCompound put(NbtCompound compound) {
		getValue().put(compound.getName(), compound);
		return this;
	}
	
	/**
	 * Retrieve the NBT list value of an entry identified by a given key.
	 * @param key - the key of the entry.
	 * @return The NBT list value of the entry.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> NbtList<T> getList(String key) {
		return (NbtList) getValueExact(key);
	}
	
	/**
	 * Associate a NBT list with the given key.
	 * @param list - the list value.
	 * @return This current compound, for chaining.
	 */
	public <T> NbtCompound put(NbtList<T> list) {
		getValue().put(list.getName(), list);
		return this;
	}
	
	/**
	 * Associate a new NBT list with the given key.
	 * @param key - the key and name of the new NBT list.
	 * @param list - the list of NBT elements.
	 * @return This current compound, for chaining.
	 */
	public <T> NbtCompound put(String key, Collection<? extends NbtBase<T>> list) {
		return put(NbtList.fromList(key, list));
	}
	
	@Override
	public void write(DataOutput destination) {
		NbtFactory.toStream(container, destination);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NbtCompound) {
			NbtCompound other = (NbtCompound) obj;
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
