package com.comphenix.protocol.injector.packet;

import java.util.Map;

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
}
