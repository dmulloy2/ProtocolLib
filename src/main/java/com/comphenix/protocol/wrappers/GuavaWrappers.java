package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.wrappers.collection.ConvertedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents wrappers for Minecraft's own version of Guava.
 * @author Kristian
 */
class GuavaWrappers {
	private static volatile boolean USE_REFLECTION_FALLBACK = false;
	
	/**
	 * Wrap a Bukkit multimap around Minecraft's internal multimap.
	 * @param multimap - the multimap to wrap.
	 * @return The Bukkit multimap.
	 */
	public static <TKey, TValue> Multimap<TKey, TValue> getBukkitMultimap(
			final com.google.common.collect.Multimap<TKey, TValue> multimap) {
		
		if (USE_REFLECTION_FALLBACK) {
			return GuavaReflection.getBukkitMultimap(multimap);
		}
		
		Multimap<TKey, TValue> result = new Multimap<TKey, TValue>() {
			@Override
			public Map<TKey, Collection<TValue>> asMap() {
				return multimap.asMap();
			}

			@Override
			public void clear() {
				multimap.clear();
			}

			@Override
			public boolean containsEntry(Object arg0, Object arg1) {
				return multimap.containsEntry(arg0, arg1);
			}

			@Override
			public boolean containsKey(Object arg0) {
				return multimap.containsKey(arg0);
			}

			@Override
			public boolean containsValue(Object arg0) {
				return multimap.containsValue(arg0);
			}

			@Override
			public Collection<Entry<TKey, TValue>> entries() {
				return multimap.entries();
			}

			@Override
			public boolean equals(Object arg0) {
				return multimap.equals(arg0);
			}

			@Override
			public Collection<TValue> get(TKey arg0) {
				return multimap.get(arg0);
			}

			@Override
			public int hashCode() {
				return multimap.hashCode();
			}

			@Override
			public boolean isEmpty() {
				return multimap.isEmpty();
			}

			@Override
			public Set<TKey> keySet() {
				return multimap.keySet();
			}

			@Override
			public Multiset<TKey> keys() {
				return getBukkitMultiset(multimap.keys());
			}

			@Override
			public boolean put(TKey arg0, TValue arg1) {
				return multimap.put(arg0, arg1);
			}

			@Override
			public boolean putAll(com.google.common.collect.Multimap<? extends TKey, ? extends TValue> arg0) {
				boolean result = false;
				
				// Add each entry
				for (Entry<? extends TKey, ? extends TValue> entry : arg0.entries()) {
					result |= multimap.put(entry.getKey(), entry.getValue());
				}
				return result;
			}

			@Override
			public boolean putAll(TKey arg0, Iterable<? extends TValue> arg1) {
				return multimap.putAll(arg0, arg1);
			}

			@Override
			public boolean remove(Object arg0, Object arg1) {
				return multimap.remove(arg0, arg1);
			}

			@Override
			public Collection<TValue> removeAll(Object arg0) {
				return multimap.removeAll(arg0);
			}

			@Override
			public Collection<TValue> replaceValues(TKey arg0, Iterable<? extends TValue> arg1) {
				return multimap.replaceValues(arg0, arg1);
			}

			@Override
			public int size() {
				return multimap.size();
			}

			@Override
			public Collection<TValue> values() {
				return multimap.values();
			}
		};
		
		try {
			result.size(); // Test
			return result;
		} catch (LinkageError e) {
			// Occurs on Cauldron 1.7.10
			USE_REFLECTION_FALLBACK = true;
			return GuavaReflection.getBukkitMultimap(multimap);
		}
	}
	
	public static <TValue> Multiset<TValue> getBukkitMultiset(final com.google.common.collect.Multiset<TValue> multiset) {
		if (USE_REFLECTION_FALLBACK) {
			return GuavaReflection.getBukkitMultiset(multiset);
		}
		
		Multiset<TValue> result = new Multiset<TValue>() {
			@Override
			public int add(TValue arg0, int arg1) {
				return multiset.add(arg0, arg1);
			}

			@Override
			public boolean add(TValue arg0) {
				return multiset.add(arg0);
			}

			@Override
			public boolean addAll(Collection<? extends TValue> c) {
				return multiset.addAll(c);
			}

			@Override
			public void clear() {
				multiset.clear();
			}

			@Override
			public boolean contains(Object arg0) {
				return multiset.contains(arg0);
			}

			@Override
			public boolean containsAll(Collection<?> arg0) {
				return multiset.containsAll(arg0);
			}

			@Override
			public int count(Object arg0) {
				return multiset.count(arg0);
			}

			@Override
			public Set<TValue> elementSet() {
				return multiset.elementSet();
			}

			@Override
			public Set<Multiset.Entry<TValue>> entrySet() {
				return new ConvertedSet<
					com.google.common.collect.Multiset.Entry<TValue>,
					Multiset.Entry<TValue>>
				(multiset.entrySet()) {
					
					@Override
					protected com.google.common.collect.Multiset.Entry<TValue> toOuter(
							com.google.common.collect.Multiset.Entry<TValue> inner) {
						return getBukkitEntry(inner);
					}

					@Override
					protected com.google.common.collect.Multiset.Entry<TValue> toInner(
							com.google.common.collect.Multiset.Entry<TValue> outer) {
						throw new UnsupportedOperationException("Cannot convert " + outer);
					}
				};
			}

			@Override
			public boolean equals(Object arg0) {
				return multiset.equals(arg0);
			}

			@Override
			public int hashCode() {
				return multiset.hashCode();
			}

			@Override
			public boolean isEmpty() {
				return multiset.isEmpty();
			}

			@Override
			public Iterator<TValue> iterator() {
				return multiset.iterator();
			}

			@Override
			public int remove(Object arg0, int arg1) {
				return multiset.remove(arg0, arg1);
			}

			@Override
			public boolean remove(Object arg0) {
				return multiset.remove(arg0);
			}

			@Override
			public boolean removeAll(Collection<?> arg0) {
				return multiset.removeAll(arg0);
			}

			@Override
			public boolean retainAll(Collection<?> arg0) {
				return multiset.retainAll(arg0);
			}

			@Override
			public boolean setCount(TValue arg0, int arg1, int arg2) {
				return multiset.setCount(arg0, arg1, arg2);
			}

			@Override
			public int setCount(TValue arg0, int arg1) {
				return multiset.setCount(arg0, arg1);
			}

			@Override
			public int size() {
				return multiset.size();
			}

			@Override
			public Object[] toArray() {
				return multiset.toArray();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				return multiset.toArray(a);
			}

			@Override
			public String toString() {
				return multiset.toString();
			}
		};
		
		try {
			result.size(); // Test
			return result;
		} catch (LinkageError e) {
			USE_REFLECTION_FALLBACK = true;
			return GuavaReflection.getBukkitMultiset(multiset);
		}
	}
	
	private static <TValue> Multiset.Entry<TValue> getBukkitEntry(final com.google.common.collect.Multiset.Entry<TValue> entry) {
		return new Multiset.Entry<TValue>() {
			@Override
			public boolean equals(Object arg0) {
				return entry.equals(arg0);
			}

			@Override
			public int getCount() {
				return entry.getCount();
			}

			@Override
			public TValue getElement() {
				return entry.getElement();
			}

			@Override
			public int hashCode() {
				return entry.hashCode();
			}

			@Override
			public String toString() {
				return entry.toString();
			}
		};
	}
}
