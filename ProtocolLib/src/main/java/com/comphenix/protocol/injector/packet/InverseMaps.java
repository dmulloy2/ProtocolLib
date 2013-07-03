package com.comphenix.protocol.injector.packet;

import java.lang.reflect.Field;
import java.util.Map;

import com.comphenix.protocol.reflect.FieldUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class InverseMaps {
	private InverseMaps() {
		// Not constructable
	}
	
	public static <K, V> Multimap<K, V> inverseMultimap(final Map<V, K> map, final Predicate<Map.Entry<V, K>> filter) {
		final MapContainer container = new MapContainer(map);
		
		return new ForwardingMultimap<K, V>() {
			// The cached multimap
			private Multimap<K, V> inverseMultimap;
			
			@Override
			protected Multimap<K, V> delegate() {
				if (container.hasChanged()) {
					inverseMultimap = HashMultimap.create();
					
					// Construct the inverse map
					for (Map.Entry<V, K> entry : map.entrySet()) {
						if (filter.apply(entry)) {
							inverseMultimap.put(entry.getValue(), entry.getKey());
						}
					}
					container.setChanged(false);
				}
				return inverseMultimap;
			}
		};
	}
	
	public static <K, V> Map<K, V> inverseMap(final Map<V, K> map, final Predicate<Map.Entry<V, K>> filter) {
		final MapContainer container = new MapContainer(map);
		
		return new ForwardingMap<K, V>() {
			// The cached map
			private Map<K, V> inverseMap;
			
			@Override
			protected Map<K, V> delegate() {
				if (container.hasChanged()) {
					inverseMap = Maps.newHashMap();
					
					// Construct the inverse map
					for (Map.Entry<V, K> entry : map.entrySet()) {
						if (filter.apply(entry)) {
							inverseMap.put(entry.getValue(), entry.getKey());
						}
					}
					container.setChanged(false);
				}
				return inverseMap;
			}
		};
	}
	
	/**
	 * Represents a class that can detect if a map has changed.
	 * @author Kristian
	 */
	private static class MapContainer {
		// For detecting changes
		private Field modCountField;
		private int lastModCount;
		
		// The object along with whether or not this is the initial run
		private Object source;
		private boolean changed;
		
		public MapContainer(Object source) {
			this.source = source;
			this.changed = true;
			this.modCountField = FieldUtils.getField(source.getClass(), "modCount", true);
		}
		
		/**
		 * Determine if the map has changed.
		 * @return TRUE if it has, FALSE otherwise.
		 */
		public boolean hasChanged() {
			// Check if unchanged
			checkChanged();
			return changed;
		}
		
		/**
		 * Mark the map as changed or unchanged.
		 * @param changed - TRUE if the map has changed, FALSE otherwise.
		 */
		public void setChanged(boolean changed) {
			this.changed = changed;
		}
		
		/**
		 * Check for modifications to the current map.
		 */
		protected void checkChanged() {
			if (!changed) {
				if (getModificationCount() != lastModCount) {
					lastModCount = getModificationCount();
					changed = true;
				}
			}
		}
		
		/**
		 * Retrieve the current modification count.
		 * @return The current count, or something different than lastModCount if not accessible.
		 */
		private int getModificationCount() {
			try {
				return modCountField != null ? modCountField.getInt(source) : lastModCount + 1;
			} catch (Exception e) {
				throw new RuntimeException("Unable to retrieve modCount.", e);
			}
		}
	}
}
