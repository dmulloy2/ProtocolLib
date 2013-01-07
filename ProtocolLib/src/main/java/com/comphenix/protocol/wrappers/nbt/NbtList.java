package com.comphenix.protocol.wrappers.nbt;

import java.io.DataOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * Represents a list of NBT tags of the same type without names.
 * <p>
 * Use {@link NbtFactory} to load or create an instance.
 * 
 * @author Kristian
 *
 * @param <TType> - the value type of each NBT tag.
 */
public class NbtList<TType> implements NbtWrapper<List<NbtBase<TType>>>, Iterable<TType> {
	/**
	 * The name of every NBT tag in a list.
	 */
	public static String EMPTY_NAME = "";
	
	// A list container
	private NbtElement<List<Object>> container;
	
	// Saved wrapper list
	private ConvertedList<Object, NbtBase<TType>> savedList;
	
	/**
	 * Construct a new empty NBT list.
	 * @param name - name of this list.
	 * @return The new empty NBT list.
	 */
	public static <T> NbtList<T> fromName(String name) {
		return (NbtList<T>) NbtFactory.<List<NbtBase<T>>>ofType(NbtType.TAG_LIST, name);
	}
	
	/**
	 * Construct a NBT list of out an array of values..
	 * @param name - name of this list.
	 * @param elements - values to add.
	 * @return The new filled NBT list.
	 */
	public static <T> NbtList<T> fromArray(String name, T... elements) {
		NbtList<T> result = fromName(name);
		
		for (T element : elements) {
			if (element == null)
				throw new IllegalArgumentException("An NBT list cannot contain a null element!");
			result.add(NbtFactory.ofType(element.getClass(), EMPTY_NAME, element));
		}
		return result;
	}
	
	/**
	 * Construct a NBT list of out a list of NBT elements.
	 * @param name - name of this list.
	 * @param elements - elements to add.
	 * @return The new filled NBT list.
	 */
	public static <T> NbtList<T> fromList(String name, Collection<? extends T> elements) {
		NbtList<T> result = fromName(name);

		for (T element : elements) {
			if (element == null)
				throw new IllegalArgumentException("An NBT list cannot contain a null element!");
			result.add(NbtFactory.ofType(element.getClass(), EMPTY_NAME, element));
		}
		return result;
	}
	
	NbtList(Object handle) {
		this.container = new NbtElement<List<Object>>(handle);
	}

	@Override
	public Object getHandle() {
		return container.getHandle();
	}
	
	@Override
	public NbtType getType() {
		return NbtType.TAG_LIST;
	}
	
	/**
	 * Get the type of each element.
	 * @return Element type.
	 */
	public NbtType getElementType() {
		return container.getSubType();
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
					if (size() > 0) {
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
					boolean empty = size() == 0;
					boolean result = false;
					
					for (NbtBase<TType> element : c) {
						add(element);
						result = true;
					}
					
					// See if we now added our first object(s)
					if (empty && result) {
						container.setSubType(get(0).getType());
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
					return NbtFactory.fromNMS(inner);
				}
				
				@Override
				public String toString() {
					return NbtList.this.toString();
				}
			};
		}
		return savedList;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public NbtBase<List<NbtBase<TType>>> deepClone() {
		return (NbtBase) container.deepClone();
	}
	
	public void add(NbtBase<TType> element) {
		getValue().add(element);
	}
	
	@SuppressWarnings("unchecked")
	public void add(String value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@SuppressWarnings("unchecked")
	public void add(byte value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@SuppressWarnings("unchecked")
	public void add(short value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@SuppressWarnings("unchecked")
	public void add(int value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@SuppressWarnings("unchecked")
	public void add(long value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}

	@SuppressWarnings("unchecked")
	public void add(double value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@SuppressWarnings("unchecked")
	public void add(byte[] value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	@SuppressWarnings("unchecked")
	public void add(int[] value) {
		add((NbtBase<TType>) NbtFactory.of(EMPTY_NAME, value));
	}
	
	public int size() {
		return getValue().size();
	}
	
	public TType getValue(int index) {
		return getValue().get(index).getValue();
	}
	
	/**
	 * Retrieve each NBT tag in this list.
	 * @return A view of NBT tag in this list.
	 */
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
		NbtFactory.toStream(container, destination);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NbtList) {
			 @SuppressWarnings("unchecked")
			NbtList<TType> other = (NbtList<TType>) obj;
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
}
