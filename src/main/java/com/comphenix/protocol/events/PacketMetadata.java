/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2017 dmulloy2
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
package com.comphenix.protocol.events;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.apache.commons.lang.Validate;

/**
 * Stores and retrieves metadata for applicable packet objects.
 * @author dmulloy2
 */
class PacketMetadata {

	private static class MetaObject<T> {
		private final String key;
		private final T value;

		private MetaObject(String key, T value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(key, value);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;

			if (o instanceof MetaObject) {
				MetaObject that = (MetaObject) o;
				return that.key.equals(this.key) &&
			           that.value.equals(this.value);
			}

			return false;
		}

		@Override
		public String toString() {
			return "MetaObject[" + key + "=" + value + "]";
		}
	}

	// Packet meta cache
	private static Cache<Object, List<MetaObject>> META_CACHE;

	public static <T> Optional<T> get(Object packet, String key) {
		Validate.notNull(key, "Null keys are not permitted!");

		if (META_CACHE == null) {
			return Optional.empty();
		}

		List<MetaObject> meta = META_CACHE.getIfPresent(packet);
		if (meta == null) {
			return Optional.empty();
		}

		for (MetaObject object : meta) {
			if (object.key.equals(key)) {
				return Optional.of((T) object.value);
			}
		}

		return Optional.empty();
	}

	private static void createCache() {
		META_CACHE = CacheBuilder
				.newBuilder()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.build();
	}

	public static <T> void set(Object packet, String key, T value) {
		Validate.notNull(key, "Null keys are not permitted!");

		if (META_CACHE == null) {
			createCache();
		}

		List<MetaObject> packetMeta;

		try {
			packetMeta = META_CACHE.get(packet, ArrayList::new);
		} catch (ExecutionException ex) {
			// Not possible, but let's humor the array list constructor having an issue
			packetMeta = new ArrayList<>();
		}

		packetMeta.removeIf(meta -> meta.key.equals(key));
		packetMeta.add(new MetaObject<>(key, value));
		META_CACHE.put(packet, packetMeta);
	}

	public static <T> Optional<T> remove(Object packet, String key) {
		Validate.notNull(key, "Null keys are not permitted!");

		if (META_CACHE == null) {
			return Optional.empty();
		}

		List<MetaObject> packetMeta = META_CACHE.getIfPresent(packet);
		if (packetMeta == null) {
			return Optional.empty();
		}

		Optional<T> value = Optional.empty();
		Iterator<MetaObject> iter = packetMeta.iterator();
		while (iter.hasNext()) {
			MetaObject meta = iter.next();
			if (meta.key.equals(key)) {
				value = Optional.of((T) meta.value);
				iter.remove();
			}
		}

		return value;
	}
}
