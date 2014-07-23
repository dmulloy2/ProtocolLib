package com.comphenix.protocol.wrappers;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.wrappers.collection.ConvertedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/**
 * Wrap multimap and multiset from another version of Guava by using reflection.
 * <p>
 * This is often the last resort, if we can't remap manually.
 * @author Kristian
 */
class GuavaReflection {
	/**
	 * Wrap a Bukkit multimap around Minecraft's internal multimap.
	 * @param multimap - the multimap to wrap.
	 * @return The Bukkit multimap.
	 */
	public static <TKey, TValue> Multimap<TKey, TValue> getBukkitMultimap(
			final Object multimap) {
		return new Multimap<TKey, TValue>() {
			private Class<?> multimapClass = multimap.getClass();
			private MethodAccessor methodAsMap = 		 Accessors.getMethodAccessor(multimapClass, "asMap");
			private MethodAccessor methodClear = 		 Accessors.getMethodAccessor(multimapClass, "clear");
			private MethodAccessor methodContainsEntry = Accessors.getMethodAccessor(multimapClass, "containsEntry",Object.class, Object.class);
			private MethodAccessor methodContainsKey = 	 Accessors.getMethodAccessor(multimapClass, "containsKey", Object.class);
			private MethodAccessor methodContainsValue = Accessors.getMethodAccessor(multimapClass, "containsValue", Object.class);
			private MethodAccessor methodEntries = 	     Accessors.getMethodAccessor(multimapClass, "entries");
			private MethodAccessor methodGet = 			 Accessors.getMethodAccessor(multimapClass, "get", Object.class);
			private MethodAccessor methodIsEmpty = 		 Accessors.getMethodAccessor(multimapClass, "isEmpty");
			private MethodAccessor methodKeySet = 		 Accessors.getMethodAccessor(multimapClass, "keySet");
			private MethodAccessor methodKeys = 		 Accessors.getMethodAccessor(multimapClass, "keys");
			private MethodAccessor methodPut = 			 Accessors.getMethodAccessor(multimapClass, "put", Object.class, Object.class);
			private MethodAccessor methodPutAll = 		 Accessors.getMethodAccessor(multimapClass, "putAll", Object.class, Iterable.class);
			private MethodAccessor methodRemove = 		 Accessors.getMethodAccessor(multimapClass, "remove", Object.class, Object.class);
			private MethodAccessor methodRemoveAll = 	 Accessors.getMethodAccessor(multimapClass, "removeAll", Object.class);
			private MethodAccessor methodReplaceValues = Accessors.getMethodAccessor(multimapClass, "replaceValues", Object.class, Iterable.class);
			private MethodAccessor methodSize = 		 Accessors.getMethodAccessor(multimapClass, "size");
			private MethodAccessor methodValues =		 Accessors.getMethodAccessor(multimapClass, "values");
			
			@SuppressWarnings("unchecked")
			public Map<TKey, Collection<TValue>> asMap() {
				return (Map<TKey, Collection<TValue>>) methodAsMap.invoke(multimap);
			}

			public void clear() {
				methodClear.invoke(multimap);
			}

			public boolean containsEntry(Object arg0, Object arg1) {
				return (Boolean) methodContainsEntry.invoke(multimap, arg0, arg1);
			}

			public boolean containsKey(Object arg0) {
				return (Boolean) methodContainsKey.invoke(multimap, arg0);
			}

			public boolean containsValue(Object arg0) {
				return (Boolean) methodContainsValue.invoke(multimap, arg0);
			}

			@SuppressWarnings("unchecked")
			public Collection<Entry<TKey, TValue>> entries() {
				return (Collection<Entry<TKey, TValue>>) methodEntries.invoke(multimap);
			}

			public boolean equals(Object arg0) {
				return multimap.equals(arg0);
			}

			public int hashCode() {
				return multimap.hashCode();
			}
			
			@Override
			public String toString() {
				return multimap.toString();
			}
			
			@SuppressWarnings("unchecked")
			public Collection<TValue> get(TKey arg0) {
				return (Collection<TValue>) methodGet.invoke(multimap, arg0);
			}

			public boolean isEmpty() {
				return (Boolean) methodIsEmpty.invoke(multimap);
			}

			@SuppressWarnings("unchecked")
			public Set<TKey> keySet() {
				return (Set<TKey>) methodKeySet.invoke(multimap);
			}

			public Multiset<TKey> keys() {
				return getBukkitMultiset(methodKeys.invoke(multimap));
			}

			public boolean put(TKey arg0, TValue arg1) {
				return (Boolean) methodPut.invoke(multimap, arg0, arg1);
			}

			public boolean putAll(com.google.common.collect.Multimap<? extends TKey, ? extends TValue> arg0) {
				boolean result = false;
				
				// Add each entry
				for (Entry<? extends TKey, ? extends TValue> entry : arg0.entries()) {
					result |= (Boolean) methodPut.invoke(multimap, entry.getKey(), entry.getValue());
				}
				return result;
			}

			public boolean putAll(TKey arg0, Iterable<? extends TValue> arg1) {
				return (Boolean) methodPutAll.invoke(arg0, arg1);
			}

			public boolean remove(Object arg0, Object arg1) {
				return (Boolean) methodRemove.invoke(multimap, arg0, arg1);
			}

			@SuppressWarnings("unchecked")
			public Collection<TValue> removeAll(Object arg0) {
				return (Collection<TValue>) methodRemoveAll.invoke(multimap, arg0);
			}

			@SuppressWarnings("unchecked")
			public Collection<TValue> replaceValues(TKey arg0, Iterable<? extends TValue> arg1) {
				return (Collection<TValue>) methodReplaceValues.invoke(multimap, arg0, arg1);
			}

			public int size() {
				return (Integer) methodSize.invoke(multimap);
			}

			@SuppressWarnings("unchecked")
			public Collection<TValue> values() {
				return (Collection<TValue>) methodValues.invoke(multimap);
			}			
		};
	}
	
	public static <TValue> Multiset<TValue> getBukkitMultiset(final Object multiset) {
		return new Multiset<TValue>() {
			private Class<?> multisetClass = multiset.getClass();
			private MethodAccessor methodAddMany = 		 	Accessors.getMethodAccessor(multisetClass, "add", Object.class, int.class);
			private MethodAccessor methodAddOne = 		 	Accessors.getMethodAccessor(multisetClass, "add", Object.class);
			private MethodAccessor methodAddAll = 		 	Accessors.getMethodAccessor(multisetClass, "addAll", Collection.class);
			private MethodAccessor methodClear = 	     	Accessors.getMethodAccessor(multisetClass, "clear");
			private MethodAccessor methodContains  =     	Accessors.getMethodAccessor(multisetClass, "contains", Object.class);
			private MethodAccessor methodContainsAll =   	Accessors.getMethodAccessor(multisetClass, "containsAll", Collection.class);
			private MethodAccessor methodCount = 		 	Accessors.getMethodAccessor(multisetClass, "count", Object.class);
			private MethodAccessor methodElementSet = 		Accessors.getMethodAccessor(multisetClass, "elementSet");
			private MethodAccessor methodEntrySet = 		Accessors.getMethodAccessor(multisetClass, "entrySet");
			private MethodAccessor methodIsEmpty = 		 	Accessors.getMethodAccessor(multisetClass, "isEmpty");
			private MethodAccessor methodIterator = 	 	Accessors.getMethodAccessor(multisetClass, "iterator");
			private MethodAccessor methodRemoveCount = 		Accessors.getMethodAccessor(multisetClass, "remove", Object.class, int.class);
			private MethodAccessor methodRemoveOne = 		Accessors.getMethodAccessor(multisetClass, "remove", Object.class);
			private MethodAccessor methodRemoveAll = 	 	Accessors.getMethodAccessor(multisetClass, "removeAll", Collection.class);
			private MethodAccessor methodRetainAll = 	 	Accessors.getMethodAccessor(multisetClass, "retainAll", Collection.class);
			private MethodAccessor methodSetCountOldNew = 	Accessors.getMethodAccessor(multisetClass, "setCount", Object.class, int.class, int.class);
			private MethodAccessor methodSetCountNew = 	    Accessors.getMethodAccessor(multisetClass, "setCount", Object.class, int.class);
			private MethodAccessor methodSize = 		 	Accessors.getMethodAccessor(multisetClass, "size");
			private MethodAccessor methodToArray =		 	Accessors.getMethodAccessor(multisetClass, "toArray");
			private MethodAccessor methodToArrayBuffer =	Accessors.getMethodAccessor(multisetClass, "toArray", Object[].class);
			
			public int add(TValue arg0, int arg1) {
				return (Integer) methodAddMany.invoke(multiset, arg0, arg1);
			}

			public boolean add(TValue arg0) {
				return (Boolean) methodAddOne.invoke(multiset, arg0);
			}

			public boolean addAll(Collection<? extends TValue> c) {
				return (Boolean) methodAddAll.invoke(multiset, c);
			} 

			public void clear() {
				methodClear.invoke(multiset);
			}

			public boolean contains(Object arg0) {
				return (Boolean) methodContains.invoke(multiset, arg0);
			}

			public boolean containsAll(Collection<?> arg0) {
				return (Boolean) methodContainsAll.invoke(multiset, arg0);
			}

			public int count(Object arg0) {
				return (Integer) methodCount.invoke(multiset, arg0);
			}

			@SuppressWarnings("unchecked")
			public Set<TValue> elementSet() {
				return (Set<TValue>) methodElementSet.invoke(multiset);
			}

			@SuppressWarnings({"unchecked", "rawtypes"})
			public Set<Multiset.Entry<TValue>> entrySet() {
				return new ConvertedSet<
					Object, 
					Multiset.Entry<TValue>>
				((Set) methodEntrySet.invoke(multiset)) {
					
					@Override
					protected com.google.common.collect.Multiset.Entry<TValue> toOuter(
							Object inner) {
						return getBukkitEntry(inner);
					}

					@Override
					protected Object toInner(
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
				return (Boolean) methodIsEmpty.invoke(multiset);
			}

			@SuppressWarnings("unchecked")
			public Iterator<TValue> iterator() {
				return (Iterator<TValue>) methodIterator.invoke(multiset);
			}

			public int remove(Object arg0, int arg1) {
				return (Integer) methodRemoveCount.invoke(multiset, arg0, arg1);
			}

			public boolean remove(Object arg0) {
				return (Boolean) methodRemoveOne.invoke(multiset, arg0);
			}

			public boolean removeAll(Collection<?> arg0) {
				return (Boolean) methodRemoveAll.invoke(multiset, arg0);
			}

			public boolean retainAll(Collection<?> arg0) {
				return (Boolean) methodRetainAll.invoke(multiset, arg0);
			}

			public boolean setCount(TValue arg0, int arg1, int arg2) {
				return (Boolean) methodSetCountOldNew.invoke(multiset, arg0, arg1, arg2);
			}

			public int setCount(TValue arg0, int arg1) {
				return (Integer) methodSetCountNew.invoke(multiset, arg0, arg1);
			}

			public int size() {
				return (Integer) methodSize.invoke(multiset);
			}

			public Object[] toArray() {
				return (Object[]) methodToArray.invoke(multiset);
			}

			@SuppressWarnings("unchecked")
			public <T> T[] toArray(T[] a) {
				return (T[]) methodToArrayBuffer.invoke(multiset, a);
			}

			public String toString() {
				return multiset.toString();
			}
		};
	}
	
	private static <TValue> Multiset.Entry<TValue> getBukkitEntry(final Object entry) {
		return new Multiset.Entry<TValue>() {
			private Class<?> entryClass = entry.getClass();
			private MethodAccessor methodEquals = 		 	Accessors.getMethodAccessor(entryClass, "equals", Object.class);
			private MethodAccessor methodGetCount = 		Accessors.getMethodAccessor(entryClass, "getCount");
			private MethodAccessor methodGetElement =	 	Accessors.getMethodAccessor(entryClass, "getElement");
			
			public boolean equals(Object arg0) {
				return (Boolean) methodEquals.invoke(entry, arg0);
			}

			public int getCount() {
				return (Integer) methodGetCount.invoke(entry);
			}

			@SuppressWarnings("unchecked")
			public TValue getElement() {
				return (TValue) methodGetElement.invoke(entry);
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
