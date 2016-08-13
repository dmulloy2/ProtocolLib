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
import java.util.List;

import javax.annotation.Nullable;

import com.comphenix.protocol.wrappers.collection.ConvertedList;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * Represents a concrete implementation of an NBT list that wraps an underlying NMS list.
 * @author Kristian
 *
 * @param <TType> - the type of the value in each NBT sub element.
 */
class WrappedList<TType> implements NbtWrapper<List<NbtBase<TType>>>, NbtList<TType> {	
	// A list container
	private WrappedElement<List<Object>> container;
	
	// Saved wrapper list
	private ConvertedList<Object, NbtBase<TType>> savedList;
	
	// Element type
	private NbtType elementType = NbtType.TAG_END;
	
	/**
	 * Construct a new empty NBT list.
	 * @param name - name of this list.
	 * @return The new empty NBT list.
	 */
	@SuppressWarnings("unchecked")
	public static <T> NbtList<T> fromName(String name) {
		return (NbtList<T>) NbtFactory.<List<NbtBase<T>>>ofWrapper(NbtType.TAG_LIST, name);
	}
	
	/**
	 * Construct a NBT list of out an array of values..
	 * @param name - name of this list.
	 * @param elements - values to add.
	 * @return The new filled NBT list.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> NbtList<T> fromArray(String name, T... elements) {
		NbtList<T> result = fromName(name);
		
		for (T element : elements) {
			if (element == null)
				throw new IllegalArgumentException("An NBT list cannot contain a null element!");
			
			if (element instanceof NbtBase) 
				result.add((NbtBase) element);
			else
				result.add(NbtFactory.ofWrapper(element.getClass(), EMPTY_NAME, element));
		}
		return result;
	}
	
	/**
	 * Construct a NBT list of out a list of NBT elements.
	 * @param name - name of this list.
	 * @param elements - elements to add.
	 * @return The new filled NBT list.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> NbtList<T> fromList(String name, Collection<? extends T> elements) {
		NbtList<T> result = fromName(name);

		for (T element : elements) {
			if (element == null)
				throw new IllegalArgumentException("An NBT list cannot contain a null element!");
			
			if (element instanceof NbtBase) 
				result.add((NbtBase) element);
			else
				result.add(NbtFactory.ofWrapper(element.getClass(), EMPTY_NAME, element));
		}
		return result;
	}
	
	/**
	 * Construct a list from an NMS instance.
	 * @param handle - NMS instance.
	 */
	public WrappedList(Object handle) {
		this.container = new WrappedElement<List<Object>>(handle);
		this.elementType = container.getSubType();
	}
	
	/**
	 * Construct a list from an NMS instance.
	 * @param handle - NMS instance.
	 * @param name - name of the current list.
	 */
	public WrappedList(Object handle, String name) {
		this.container = new WrappedElement<List<Object>>(handle, name);
		this.elementType = container.getSubType();
	}

	@Override
	public boolean accept(NbtVisitor visitor) {
		// Enter this node?
		if (visitor.visitEnter(this)) {
			for (NbtBase<TType> node : getValue()) {
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
		return NbtType.TAG_LIST;
	}
	
	@Override
	public NbtType getElementType() {
		return elementType;
	}
	
	@Override
	public void setElementType(NbtType type) {
		this.elementType = type;
		container.setSubType(type);
	}

	@Override
	public String getName() {
		return container.getName();
	}

	@Override
	public void setName(String name) {
		container.setName(name);
	}

	@Override
	public List<NbtBase<TType>> getValue() {
		if (savedList == null) {
			savedList = new ConvertedList<Object, NbtBase<TType>>(container.getValue()) {
				// Check and see if the element is valid
				private void verifyElement(NbtBase<TType> element) {
					if (element == null)
						throw new IllegalArgumentException("Cannot store NULL elements in list.");
					if (!element.getName().equals(EMPTY_NAME))
						throw new IllegalArgumentException("Cannot add a the named NBT tag " + element + " to a list.");
					
					// Check element type
					if (getElementType() != NbtType.TAG_END) {
						if (!element.getType().equals(getElementType())) {
							throw new IllegalArgumentException(
									"Cannot add " + element + " of " + element.getType() + " to a list of type " + getElementType());
						}
					} else {
						container.setSubType(element.getType());
					}
				}
				
				@Override
				public boolean add(NbtBase<TType> e) {
					verifyElement(e);
					return super.add(e);
				}
				
				@Override
				public void add(int index, NbtBase<TType> element) {
					verifyElement(element);
					super.add(index, element);
				}
				
				@Override
				public boolean addAll(Collection<? extends NbtBase<TType>> c) {
					boolean result = false;
					
					for (NbtBase<TType> element : c) {
						add(element);
						result = true;
					}
					return result;
				}
				
				@Override
				protected Object toInner(NbtBase<TType> outer) {
					if (outer == null)
						return null;
					return NbtFactory.fromBase(outer).getHandle();
				}
				
				@Override
				protected NbtBase<TType> toOuter(Object inner) {
					if (inner == null)
						return null;
					return NbtFactory.fromNMS(inner, null);
				}
				
				@Override
				public String toString() {
					return WrappedList.this.toString();
				}
			};
		}
		return savedList;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public NbtBase<List<NbtBase<TType>>> deepClone() {
		return (NbtBase) container.deepClone();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void addClosest(Object value) {
		if (getElementType() == NbtType.TAG_END)
			throw new IllegalStateException("This list has not been typed yet.");
		
		if (value instanceof Number) {
			Number number = (Number) value;
			
			// Convert the number
			switch (getElementType()) {
				case TAG_BYTE: add(number.byteValue()); break;
				case TAG_SHORT: add(number.shortValue()); break;
				case TAG_INT: add(number.intValue()); break;
				case TAG_LONG: add(number.longValue()); break;
				case TAG_FLOAT: add(number.floatValue()); break;
				case TAG_DOUBLE: add(number.doubleValue()); break;
				case TAG_STRING: add(number.toString()); break;
				default: 
					throw new IllegalArgumentException("Cannot convert " + value + " to " + getType());
			}
			
		} else if (value instanceof NbtBase) {
			// Add the element itself
			add((NbtBase<TType>) value);
			
		} else {
			// Just add it
			add((NbtBase<TType>) NbtFactory.ofWrapper(getElementType(), EMPTY_NAME, value));
		}
	}
	
	@Override
	public void add(NbtBase<TType> element) {
		getValue().add(element);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(String value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(byte value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(short value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(int value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(long value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void add(double value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(byte[] value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(int[] value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@Override
	public int size() {
		return getValue().size();
	}
	
	@Override
	public TType getValue(int index) {
		return getValue().get(index).getValue();
	}
	
	/**
	 * Retrieve each NBT tag in this list.
	 * @return A view of NBT tag in this list.
	 */
	@Override
	public Collection<NbtBase<TType>> asCollection() {
		return getValue();
	}
	
	@Override
	public void setValue(List<NbtBase<TType>> newValue) {
		NbtBase<TType> lastElement = null;
		List<Object> list = container.getValue();
		list.clear();
		
		// Set each underlying element
		for (NbtBase<TType> type : newValue) {
			if (type != null) {
				lastElement = type;
				list.add(NbtFactory.fromBase(type).getHandle());
			} else {
				list.add(null);
			}
		}
		
		// Update the sub type as well
		if (lastElement != null) {
			container.setSubType(lastElement.getType());
		}
	}
	
	@Override
	public void write(DataOutput destination) {
		NbtBinarySerializer.DEFAULT.serialize(container, destination);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WrappedList) {
			 @SuppressWarnings("unchecked")
			WrappedList<TType> other = (WrappedList<TType>) obj;
			return container.equals(other.container);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return container.hashCode();
	}

	@Override
	public Iterator<TType> iterator() {
		return Iterables.transform(getValue(), new Function<NbtBase<TType>, TType>() {
			@Override
			public TType apply(@Nullable NbtBase<TType> param) {
				return param.getValue();
			}
		}).iterator();
	}
	
	@Override
	public String toString() {
		// Essentially JSON
		StringBuilder builder = new StringBuilder();
		
		builder.append("{\"name\": \"" + getName() + "\", \"value\": [");
		
		if (size() > 0) {
			if (getElementType() == NbtType.TAG_STRING) 
				builder.append("\"" + Joiner.on("\", \"").join(this) + "\"");
			 else 
				builder.append(Joiner.on(", ").join(this));
		}
		
		builder.append("]}");
		return builder.toString();
	}

	@Override
	public void remove(Object remove) {
		getValue().remove(remove);
	}
}
