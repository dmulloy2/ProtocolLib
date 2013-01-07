package com.comphenix.protocol.wrappers.nbt;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

abstract class ConvertedMap<Key, VInner, VOuter> extends AbstractConverted<VInner, VOuter> implements Map<Key, VOuter> {
	// Inner map
	private Map<Key, VInner> inner;

	public ConvertedMap(Map<Key, VInner> inner) {
		if (inner == null)
			throw new IllegalArgumentException("Inner map cannot be NULL.");
		this.inner = inner;
	}

	@Override
	public void clear() {
		inner.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return inner.containsKey(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean containsValue(Object value) {
		return inner.containsValue(toInner((VOuter) value));
	}

	@Override
	public Set<Entry<Key, VOuter>> entrySet() {
		return new ConvertedSet<Entry<Key,VInner>, Entry<Key,VOuter>>(inner.entrySet()) {
			@Override
			protected Entry<Key, VInner> toInner(final Entry<Key, VOuter> outer) {
				return new Entry<Key, VInner>() {
					@Override
					public Key getKey() {
						return outer.getKey();
					}

					@Override
					public VInner getValue() {
						return ConvertedMap.this.toInner(outer.getValue());
					}

					@Override
					public VInner setValue(VInner value) {
						return ConvertedMap.this.toInner(outer.setValue(ConvertedMap.this.toOuter(value))); 
					}
					
					@Override
					public String toString() {
						return String.format("\"%s\": %s", getKey(), getValue());
					}
				};
			}
			
			@Override
			protected Entry<Key, VOuter> toOuter(final Entry<Key, VInner> inner) {
				return new Entry<Key, VOuter>() {
					@Override
					public Key getKey() {
						return inner.getKey();
					}

					@Override
					public VOuter getValue() {
						return ConvertedMap.this.toOuter(inner.getValue());
					}

					@Override
					public VOuter setValue(VOuter value) {
						return ConvertedMap.this.toOuter(inner.setValue(ConvertedMap.this.toInner(value))); 
					}
					
					@Override
					public String toString() {
						return String.format("\"%s\": %s", getKey(), getValue());
					}
				};
			}
		};
	}

	@Override
	public VOuter get(Object key) {
		return toOuter(inner.get(key));
	}

	@Override
	public boolean isEmpty() {
		return inner.isEmpty();
	}

	@Override
	public Set<Key> keySet() {
		return inner.keySet();
	}

	@Override
	public VOuter put(Key key, VOuter value) {
		return toOuter(inner.put(key, toInner(value)));
	}

	@Override
	public void putAll(Map<? extends Key, ? extends VOuter> m) {
		for (Entry<? extends Key, ? extends VOuter> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public VOuter remove(Object key) {
		return toOuter(inner.remove(key));
	}

	@Override
	public int size() {
		return inner.size();
	}

	@Override
	public Collection<VOuter> values() {
		return new ConvertedCollection<VInner, VOuter>(inner.values()) {
			@Override
			protected VOuter toOuter(VInner inner) {
				return ConvertedMap.this.toOuter(inner);
			}

			@Override
			protected VInner toInner(VOuter outer) {
				return ConvertedMap.this.toInner(outer);
			}
		};
	}
}
