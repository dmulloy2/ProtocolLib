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

package com.comphenix.protocol.injector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedIntHashMap;
import com.google.common.collect.Lists;

/**
 * Used to perform certain operations on entities.
 * 
 * @author Kristian
 */
class EntityUtilities {

	private static Field entityTrackerField;
	private static Field trackedEntitiesField;
	private static Field trackedPlayersField;
	private static Field trackerField;
	
	private static Method scanPlayersMethod;

	/*
	public static void updateEntity2(Entity entity, List<Player> observers) {
		EntityTrackerEntry entry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());

		List<EntityPlayer> nmsPlayers = getNmsPlayers(observers);

		entry.trackedPlayers.removeAll(nmsPlayers);
		entry.scanPlayers(nmsPlayers);
	}
	*/

	public static void updateEntity(Entity entity, List<Player> observers) throws FieldAccessException {
		if (entity == null || !entity.isValid()) {
			return;
		}

		try {
			Object trackerEntry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
			if (trackerEntry == null) {
				throw new IllegalArgumentException("Cannot find entity trackers for " + entity + ".");
			}

			if (trackedPlayersField == null) {
				trackedPlayersField = FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*");
			}
			
			// Phew, finally there.
			Collection<?> trackedPlayers = getTrackedPlayers(trackedPlayersField, trackerEntry);
			List<Object> nmsPlayers = unwrapBukkit(observers);

			trackedPlayers.removeAll(nmsPlayers);
			
			// We have to rely on a NAME once again. Damn it.
			// TODO: Make sure this stays up to date with version changes - 1.8 - 1.10
			if (scanPlayersMethod == null) {
				scanPlayersMethod = trackerEntry.getClass().getMethod("scanPlayers", List.class);
			}

			scanPlayersMethod.invoke(trackerEntry, nmsPlayers);			
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Security limitation prevents access to 'get' method in IntHashMap", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Exception occurred in Minecraft.", e);
		} catch (SecurityException e) {
			throw new FieldAccessException("Security limitation prevents access to 'scanPlayers' method in trackerEntry.", e);
		} catch (NoSuchMethodException e) {
			throw new FieldAccessException("Cannot find 'scanPlayers' method. Is ProtocolLib up to date?", e);
		}
	}

	/**
	 * Retrieve every client that is receiving information about a given entity.
	 * @param entity - the entity that is being tracked.
	 * @return Every client/player that is tracking the given entity.
	 * @throws FieldAccessException If reflection failed.
	 */
	public static List<Player> getEntityTrackers(Entity entity) {
		if (entity == null || !entity.isValid()) {
			return new ArrayList<>();
		}

		try {
			List<Player> result = new ArrayList<Player>();

			Object trackerEntry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
			if (trackerEntry == null) {
				throw new IllegalArgumentException("Cannot find entity trackers for " + entity + ".");
			}

			if (trackedPlayersField == null) {
				trackedPlayersField = FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*");
			}

			Collection<?> trackedPlayers = getTrackedPlayers(trackedPlayersField, trackerEntry);

			// Wrap every player - we also ensure that the underlying tracker list is immutable
			for (Object tracker : trackedPlayers) {
				if (MinecraftReflection.isMinecraftPlayer(tracker)) {
					result.add((Player) MinecraftReflection.getBukkitEntity(tracker));
				}
			}

			return result;
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Security limitation prevented access to the list of tracked players.", e);
		}
	}

	// Damn you, Paper
	private static Collection<?> getTrackedPlayers(Field field, Object entry) throws IllegalAccessException {
		Validate.notNull(field, "Cannot find 'trackedPlayers' field.");
		Validate.notNull(entry, "entry cannot be null!");

		Object value = FieldUtils.readField(field, entry, false);

		if (value instanceof Collection) {
			return (Collection<?>) value;
		} else if (value instanceof Map) {
			return ((Map<?, ?>) value).keySet();
		} else {
			// Please. No more changes.
			throw new IllegalStateException("trackedPlayers field was an unknown type: expected Collection or Map, but got " + value.getClass());
		}
	}

	/*
	private static EntityTrackerEntry getEntityTrackerEntry2(World world, int entityID) {
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		EntityTracker tracker = worldServer.tracker;
		return tracker.trackedEntities.get(entityID);
	}
	*/

	private static Object getEntityTrackerEntry(World world, int entityID) throws FieldAccessException, IllegalArgumentException {
		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		Object worldServer = unwrapper.unwrapItem(world);

		if (entityTrackerField == null)
			entityTrackerField = FuzzyReflection.fromObject(worldServer).
									getFieldByType("tracker", MinecraftReflection.getEntityTrackerClass());
		
		// Get the tracker
		Object tracker = null;
		
		try {
			tracker = FieldUtils.readField(entityTrackerField, worldServer, false);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot access 'tracker' field due to security limitations.", e);
		}

		// Looking for an IntHashMap in the tracker entry
		if (trackedEntitiesField == null) {
			trackedEntitiesField = FuzzyReflection.fromObject(tracker, false)
					.getFieldByType("trackedEntities", MinecraftReflection.getIntHashMapClass());
		}

		// Read the map
		Object trackedEntities = null;

		try {
			trackedEntities = FieldUtils.readField(trackedEntitiesField, tracker, false);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot access 'trackedEntities' field due to security limitations.", e);
		}

		return WrappedIntHashMap.fromHandle(trackedEntities).get(entityID);
	}

	/**
	 * Retrieve entity from a ID, even it it's newly created.
	 * @return The associated entity.
	 * @throws FieldAccessException Reflection error.
	 */
	public static Entity getEntityFromID(World world, int entityID) throws FieldAccessException {
		try {
			Object trackerEntry = getEntityTrackerEntry(world, entityID);
			Object tracker = null;

			// Handle NULL cases
			if (trackerEntry != null) {
				if (trackerField == null) {
					try {
						Class<?> entryClass = MinecraftReflection.getMinecraftClass("EntityTrackerEntry");
						trackerField = entryClass.getDeclaredField("tracker");
					} catch (NoSuchFieldException e) {
						// Assume it's the first entity field then
						trackerField = FuzzyReflection.fromObject(trackerEntry, true)
								.getFieldByType("tracker", MinecraftReflection.getEntityClass());
					}
				}

				tracker = FieldUtils.readField(trackerField, trackerEntry, true);
			}

			// If the tracker is NULL, we'll just assume this entity doesn't exist
			if (tracker != null)
				return (Entity) MinecraftReflection.getBukkitEntity(tracker);
			else
				return null;
		} catch (Exception e) {
			throw new FieldAccessException("Cannot find entity from ID " + entityID + ".", e);
		}
	}

	private static List<Object> unwrapBukkit(List<Player> players) {
		List<Object> output = Lists.newArrayList();
		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		
		// Get the NMS equivalent
		for (Player player : players) {
			Object result = unwrapper.unwrapItem(player);
			
			if (result != null)
				output.add(result);
			else
				throw new IllegalArgumentException("Cannot unwrap item " + player);
		}
		
		return output;
	}
}
