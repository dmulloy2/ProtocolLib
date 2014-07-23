package com.comphenix.protocol.wrappers;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.comphenix.protocol.wrappers.collection.ConvertedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

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
			final net.minecraft.util.com.google.common.collect.Multimap<TKey, TValue> multimap) {
		
		if (USE_REFLECTION_FALLBACK) {
			return GuavaReflection.getBukkitMultimap(multimap);
		}
		
		Multimap<TKey, TValue> result = new Multimap<TKey, TValue>() {
			public Map<TKey, Collection<TValue>> asMap() {
				return multimap.asMap();
			}

			public void clear() {
				multimap.clear();
			}

			public boolean containsEntry(Object arg0, Object arg1) {
				return multimap.containsEntry(arg0, arg1);
			}

			public boolean containsKey(Object arg0) {
				return multimap.containsKey(arg0);
			}

			public boolean containsValue(Object arg0) {
				return multimap.containsValue(arg0);
			}

			public Collection<Entry<TKey, TValue>> entries() {
				return multimap.entries();
			}

			public boolean equals(Object arg0) {
				return multimap.equals(arg0);
			}

			public Collection<TValue> get(TKey arg0) {
				return multimap.get(arg0);
			}

			public int hashCode() {
				return multimap.hashCode();
			}

			public boolean isEmpty() {
				return multimap.isEmpty();
			}

			public Set<TKey> keySet() {
				return multimap.keySet();
			}

			public Multiset<TKey> keys() {
				return getBukkitMultiset(multimap.keys());
			}

			public boolean put(TKey arg0, TValue arg1) {
				return multimap.put(arg0, arg1);
			}

			public boolean putAll(com.google.common.collect.Multimap<? extends TKey, ? extends TValue> arg0) {
				boolean result = false;
				
				// Add each entry
				for (Entry<? extends TKey, ? extends TValue> entry : arg0.entries()) {
					result |= multimap.put(entry.getKey(), entry.getValue());
				}
				return result;
			}

			public boolean putAll(TKey arg0, Iterable<? extends TValue> arg1) {
				return multimap.putAll(arg0, arg1);
			}

			public boolean remove(Object arg0, Object arg1) {
				return multimap.remove(arg0, arg1);
			}

			public Collection<TValue> removeAll(Object arg0) {
				return multimap.removeAll(arg0);
			}

			public Collection<TValue> replaceValues(TKey arg0, Iterable<? extends TValue> arg1) {
				return multimap.replaceValues(arg0, arg1);
			}

			public int size() {
				return multimap.size();
			}

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
	
	public static <TValue> Multiset<TValue> getBukkitMultiset(final net.minecraft.util.com.google.common.collect.Multiset<TValue> multiset) {
		if (USE_REFLECTION_FALLBACK) {
			return GuavaReflection.getBukkitMultiset(multiset);
		}
		
		Multiset<TValue> result = new Multiset<TValue>() {
			public int add(TValue arg0, int arg1) {
				return multiset.add(arg0, arg1);
			}

			public boolean add(TValue arg0) {
				return multiset.add(arg0);
			}

			public boolean addAll(Collection<? extends TValue> c) {
				return multiset.addAll(c);
			}

			public void clear() {
				multiset.clear();
			}

			public boolean contains(Object arg0) {
				return multiset.contains(arg0);
			}

			public boolean containsAll(Collection<?> arg0) {
				return multiset.containsAll(arg0);
			}

			public int count(Object arg0) {
				return multiset.count(arg0);
			}

			public Set<TValue> elementSet() {
				return multiset.elementSet();
			}

			public Set<Multiset.Entry<TValue>> entrySet() {
				return new ConvertedSet<
					net.minecraft.util.com.google.common.collect.Multiset.Entry<TValue>, 
					Multiset.Entry<TValue>>
				(multiset.entrySet()) {
					
					@Override
					protected com.google.common.collect.Multiset.Entry<TValue> toOuter(
							net.minecraft.util.com.google.common.collect.Multiset.Entry<TValue> inner) {
						return getBukkitEntry(inner);
					}

					@Override
					protected net.minecraft.util.com.google.common.collect.Multiset.Entry<TValue> toInner(
							com.google.common.collect.Multiset.Entry<TValue> outer) {
						throw new UnsupportedOperationException("Cannot convert " + outer);
					}
				};
			}

			public boolean equals(Object arg0) {
				return multiset.equals(arg0);
			}

			public int hashCode() {
				return multiset.hashCode();
			}

			public boolean isEmpty() {
				return multiset.isEmpty();
			}

			public Iterator<TValue> iterator() {
				return multiset.iterator();
			}

			public int remove(Object arg0, int arg1) {
				return multiset.remove(arg0, arg1);
			}

			public boolean remove(Object arg0) {
				return multiset.remove(arg0);
			}

			public boolean removeAll(Collection<?> arg0) {
				return multiset.removeAll(arg0);
			}

			public boolean retainAll(Collection<?> arg0) {
				return multiset.retainAll(arg0);
			}

			public boolean setCount(TValue arg0, int arg1, int arg2) {
				return multiset.setCount(arg0, arg1, arg2);
			}

			public int setCount(TValue arg0, int arg1) {
				return multiset.setCount(arg0, arg1);
			}

			public int size() {
				return multiset.size();
			}

			public Object[] toArray() {
				return multiset.toArray();
			}

			public <T> T[] toArray(T[] a) {
				return multiset.toArray(a);
			}

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
	
	private static <TValue> Multiset.Entry<TValue> getBukkitEntry(final net.minecraft.util.com.google.common.collect.Multiset.Entry<TValue> entry) {
		return new Multiset.Entry<TValue>() {
			public boolean equals(Object arg0) {
				return entry.equals(arg0);
			}

			public int getCount() {
				return entry.getCount();
			}

			public TValue getElement() {
				return entry.getElement();
			}

			public int hashCode() {
				return entry.hashCode();
			}

			public String toString() {
				return entry.toString();
			}
		};
	}
}
