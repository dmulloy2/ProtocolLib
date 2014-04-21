package com.comphenix.protocol.wrappers.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/**
 * Represents a multimap that wraps another multimap by transforming the entries that are going in and out.
 * @author Kristian
 *
 * @param <Key> - the key.
 * @param <VInner> - the inner value type.
 * @param <VOuter> - the outer value type.
 */
public abstract class ConvertedMultimap<Key, VInner, VOuter> extends AbstractConverted<VInner, VOuter> implements Multimap<Key, VOuter> {
	// Inner multimap
	private Multimap<Key, VInner> inner;

	public ConvertedMultimap(Multimap<Key, VInner> inner) {
		this.inner = Preconditions.checkNotNull(inner, "inner map cannot be NULL.");
	}
	
	/**
	 * Wrap a given collection.
	 * @param inner - the inner collection.
	 * @return The outer collection.
	 */
	protected Collection<VOuter> toOuterCollection(Collection<VInner> inner) {
		return new ConvertedCollection<VInner, VOuter>(inner) {
			@Override
			protected VInner toInner(VOuter outer) {
				return ConvertedMultimap.this.toInner(outer);
			}
			
			@Override
			protected VOuter toOuter(VInner inner) {
				return ConvertedMultimap.this.toOuter(inner);
			}
			
			@Override
			public String toString() {
				return "[" + Joiner.on(", ").join(this) + "]";
			}
		};
	}
	
	/**
	 * Wrap a given collection.
	 * @param outer - the outer collection.
	 * @return The inner collection.
	 */
	protected Collection<VInner> toInnerCollection(Collection<VOuter> outer) {
		return new ConvertedCollection<VOuter, VInner>(outer) {
			@Override
			protected VOuter toInner(VInner outer) {
				return ConvertedMultimap.this.toOuter(outer);
			}
			
			@Override
			protected VInner toOuter(VOuter inner) {
				return ConvertedMultimap.this.toInner(inner);
			}
			
			@Override
			public String toString() {
				return "[" + Joiner.on(", ").join(this) + "]";
			}
		};
	}

	/**
	 * Convert to an inner object if its of the correct type, otherwise leave it.
	 * @param outer - the outer object.
	 * @return The inner object, or the same object.
	 */
	@SuppressWarnings("unchecked")
	protected Object toInnerObject(Object outer) {
		return toInner((VOuter) outer);
	}
	
	@Override
	public int size() {
		return inner.size();
	}

	@Override
	public boolean isEmpty() {
		return inner.isEmpty();
	}

	@Override
	public boolean containsKey(@Nullable Object key) {
		return inner.containsKey(key);
	}

	@Override
	public boolean containsValue(@Nullable Object value) {
		return inner.containsValue(toInnerObject(value));
	}

	@Override
	public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
		return inner.containsEntry(key, toInnerObject(value));
	}

	@Override
	public boolean put(@Nullable Key key, @Nullable VOuter value) {
		return inner.put(key, toInner(value));
	}

	@Override
	public boolean remove(@Nullable Object key, @Nullable Object value) {
		return inner.remove(key, toInnerObject(value));
	}

	@Override
	public boolean putAll(@Nullable Key key, Iterable<? extends VOuter> values) {
		return inner.putAll(key, Iterables.transform(values, getInnerConverter()));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public boolean putAll(Multimap<? extends Key, ? extends VOuter> multimap) {
		return inner.putAll(new ConvertedMultimap<Key, VOuter, VInner>((Multimap) multimap) {
			@Override
			protected VOuter toInner(VInner outer) {
				return ConvertedMultimap.this.toOuter(outer);
			}
						
			@Override
			protected VInner toOuter(VOuter inner) {
				return ConvertedMultimap.this.toInner(inner);
			}
		});
	}

	@Override
	public Collection<VOuter> replaceValues(@Nullable Key key, Iterable<? extends VOuter> values) {
		return toOuterCollection(
			inner.replaceValues(key, Iterables.transform(values, getInnerConverter()))
		);
	}

	@Override
	public Collection<VOuter> removeAll(@Nullable Object key) {
		return toOuterCollection(inner.removeAll(key));
	}

	@Override
	public void clear() {
		inner.clear();
	}

	@Override
	public Collection<VOuter> get(@Nullable Key key) {
		return toOuterCollection(inner.get(key));
	}

	@Override
	public Set<Key> keySet() {
		return inner.keySet();
	}

	@Override
	public Multiset<Key> keys() {
		return inner.keys();
	}

	@Override
	public Collection<VOuter> values() {
		return toOuterCollection(inner.values());
	}

	@Override
	public Collection<Entry<Key, VOuter>> entries() {
		return ConvertedMap.convertedEntrySet(inner.entries(), 
			new BiFunction<Key, VOuter, VInner>() {
				public VInner apply(Key key, VOuter outer) {
					return toInner(outer);
				}
			},
			new BiFunction<Key, VInner, VOuter>() {
				public VOuter apply(Key key, VInner inner) {
					return toOuter(inner);
				}
			}
		);
	}

	@Override
	public Map<Key, Collection<VOuter>> asMap() {
		return new ConvertedMap<Key, Collection<VInner>, Collection<VOuter>>(inner.asMap()) {
			@Override
			protected Collection<VInner> toInner(Collection<VOuter> outer) {
				return toInnerCollection(outer);
			}
			
			@Override
			protected Collection<VOuter> toOuter(Collection<VInner> inner) {
				return toOuterCollection(inner);
			}
		};
	}
	
    /**
     * Returns a string representation of this map.  The string representation
     * consists of a list of key-value mappings in the order returned by the
     * map's <tt>entrySet</tt> view's iterator, enclosed in braces
     * (<tt>"{}"</tt>).  Adjacent mappings are separated by the characters
     * <tt>", "</tt> (comma and space).  Each key-value mapping is rendered as
     * the key followed by an equals sign (<tt>"="</tt>) followed by the
     * associated value.  Keys and values are converted to strings as by
     * {@link String#valueOf(Object)}.
     *
     * @return a string representation of this map
     */
    public String toString() {
        Iterator<Entry<Key, VOuter>> i = entries().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<Key, VOuter> e = i.next();
            Key key = e.getKey();
            VOuter value = e.getValue();
            sb.append(key   == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (! i.hasNext())
                return sb.append('}').toString();
            sb.append(", ");
        }
    }
}
