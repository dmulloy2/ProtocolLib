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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedIntHashMap;
import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Used to perform certain operations on entities.
 *
 * @author Kristian
 */
class EntityUtilities {
	private static final boolean NEW_TRACKER = MinecraftVersion.VILLAGE_UPDATE.atOrAbove();
	private static final EntityUtilities INSTANCE = new EntityUtilities();

	public static EntityUtilities getInstance() {
		return INSTANCE;
	}

	private EntityUtilities() { }

	private FieldAccessor entityTrackerField;
	private FieldAccessor trackedEntitiesField;
	private FieldAccessor trackedPlayersField;

	private Map<Class<?>, MethodAccessor> scanPlayersMethods = new HashMap<>();

	public void updateEntity(Entity entity, List<Player> observers) {
		if (entity == null || !entity.isValid()) {
			return;
		}

		Collection<?> trackedPlayers = getTrackedPlayers(entity);
		List<Object> nmsPlayers = unwrapBukkit(observers);

		trackedPlayers.removeAll(nmsPlayers);

		Object trackerEntry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());

		// there can be multiple different entity tracker entry impls, see GH-732....
		scanPlayersMethods.computeIfAbsent(trackerEntry.getClass(), this::findScanPlayers).invoke(trackerEntry, nmsPlayers);
	}

	private MethodAccessor findScanPlayers(Class<?> trackerClass) {
		MethodAccessor candidate = Accessors.getMethodAcccessorOrNull(trackerClass, "scanPlayers");
		if (candidate != null) {
			return candidate;
		}

		FuzzyReflection fuzzy = FuzzyReflection.fromClass(trackerClass, true);
		return Accessors.getMethodAccessor(
				fuzzy.getMethod(
						FuzzyMethodContract.newBuilder().returnTypeVoid().parameterExactArray(List.class).build()));
	}

	/**
	 * Retrieve every client that is receiving information about a given entity.
	 * @param entity - the entity that is being tracked.
	 * @return Every client/player that is tracking the given entity.
	 * @throws FieldAccessException If reflection failed.
	 */
	public List<Player> getEntityTrackers(Entity entity) {
		if (entity == null || !entity.isValid()) {
			return new ArrayList<>();
		}

		List<Player> result = new ArrayList<>();
		Collection<?> trackedPlayers = getTrackedPlayers(entity);

		// Wrap every player - we also ensure that the underlying tracker list is immutable
		for (Object tracker : trackedPlayers) {
			if (MinecraftReflection.isMinecraftPlayer(tracker)) {
				result.add((Player) MinecraftReflection.getBukkitEntity(tracker));
			}
		}

		return result;
	}

	private Collection<?> getTrackedPlayers(Entity entity) {
		Validate.notNull(entity, "entity cannot be null");

		Object trackerEntry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
		Validate.notNull(trackerEntry, "Could not find entity trackers for " + entity);

		if (trackedPlayersField == null) {
			trackedPlayersField = Accessors.getFieldAccessor(FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*"));
		}

		Validate.notNull(trackedPlayersField, "Could not find trackedPlayers field");

		Object value = trackedPlayersField.get(trackerEntry);
		if (value instanceof Collection) {
			return (Collection<?>) value;
		} else if (value instanceof Map) {
			return ((Map<?, ?>) value).keySet();
		} else {
			// Please. No more changes.
			throw new IllegalStateException("trackedPlayers field was an unknown type: expected Collection or Map, but got " + value.getClass());
		}
	}

	private MethodAccessor getChunkProvider;
	private FieldAccessor chunkMapField;

	@SuppressWarnings("unchecked")
	private Object getNewEntityTracker(Object worldServer, int entityId) {
		if (getChunkProvider == null) {
			Class<?> chunkProviderClass = MinecraftReflection.getChunkProviderServer();
			getChunkProvider = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(worldServer.getClass(), false).getMethod(
							FuzzyMethodContract.newBuilder().parameterCount(0).returnTypeExact(chunkProviderClass).build()));
		}

		Object chunkProvider = getChunkProvider.invoke(worldServer);

		if (chunkMapField == null) {
			Class<?> chunkMapClass = MinecraftReflection.getPlayerChunkMap();
			chunkMapField = Accessors.getFieldAccessor(
					FuzzyReflection.fromClass(chunkProvider.getClass(), false).getField(
							FuzzyFieldContract.newBuilder().typeExact(chunkMapClass).build()));
		}

		Object playerChunkMap = chunkMapField.get(chunkProvider);

		if (trackedEntitiesField == null) {
			trackedEntitiesField = Accessors.getFieldAccessor(
					FuzzyReflection.fromClass(playerChunkMap.getClass(), false).getField(
							FuzzyFieldContract.newBuilder().typeDerivedOf(Map.class).nameExact("trackedEntities").build()));
		}

		Map<Integer, Object> trackedEntities = (Map<Integer, Object>) trackedEntitiesField.get(playerChunkMap);
		return trackedEntities.get(entityId);
	}

	private Object getEntityTrackerEntry(World world, int entityID) {
		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		Object worldServer = unwrapper.unwrapItem(world);

		if (NEW_TRACKER) {
			return getNewEntityTracker(worldServer, entityID);
		}

		if (entityTrackerField == null)
			entityTrackerField = Accessors.getFieldAccessor(FuzzyReflection.fromObject(worldServer).
									getFieldByType("tracker", MinecraftReflection.getEntityTrackerClass()));

		// Get the tracker
		Object tracker = entityTrackerField.get(worldServer);

		// Looking for an IntHashMap in the tracker entry
		if (trackedEntitiesField == null) {
			trackedEntitiesField = Accessors.getFieldAccessor(FuzzyReflection.fromObject(tracker, false)
					.getFieldByType("trackedEntities", MinecraftReflection.getIntHashMapClass()));
		}

		// Read the map
		Object trackedEntities = trackedEntitiesField.get(tracker);
		return WrappedIntHashMap.fromHandle(trackedEntities).get(entityID);
	}

	private Map<Class<?>, FieldAccessor> trackerFields = new ConcurrentHashMap<>();
	private MethodAccessor getEntityFromId;

	/**
	 * Retrieve entity from a ID, even it it's newly created.
	 * @return The associated entity.
	 * @throws FieldAccessException Reflection error.
	 */
	public Entity getEntityFromID(World world, int entityID) {
		Validate.notNull(world, "world cannot be null");
		Validate.isTrue(entityID >= 0, "entityID cannot be negative");

		try {
			// first, try to read from the world
			// this should be good enough for most cases, but only exists in 1.14+
			if (NEW_TRACKER) {
				Object worldServer = BukkitUnwrapper.getInstance().unwrapItem(world);

				if (getEntityFromId == null) {
					FuzzyReflection fuzzy = FuzzyReflection.fromClass(worldServer.getClass(), false);
					getEntityFromId = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
							.parameterExactArray(int.class)
							.returnTypeExact(MinecraftReflection.getEntityClass())
							.build()));
				}

				Object entity = getEntityFromId.invoke(worldServer, entityID);
				if (entity != null) {
					return (Entity) MinecraftReflection.getBukkitEntity(entity);
				}
			}

			// then go into the trackers
			Object trackerEntry = getEntityTrackerEntry(world, entityID);
			Object tracker = null;

			if (trackerEntry != null) {
				// plugins like citizens will use their own tracker
				FieldAccessor trackerField = trackerFields.computeIfAbsent(trackerEntry.getClass(), x -> {
					try {
						return Accessors.getFieldAccessor(trackerEntry.getClass(), "tracker", true);
					} catch (Exception e) {
						// Assume it's the first entity field then
						return Accessors.getFieldAccessor(FuzzyReflection.fromObject(trackerEntry, true)
								.getFieldByType("tracker", MinecraftReflection.getEntityClass()));
					}
				});

				tracker = trackerField.get(trackerEntry);
			}

			// If the tracker is NULL, we'll just assume this entity doesn't exist
			return tracker != null ? (Entity) MinecraftReflection.getBukkitEntity(tracker) : null;
		} catch (Exception e) {
			throw new FieldAccessException("Cannot find entity from ID " + entityID + ".", e);
		}
	}

	private List<Object> unwrapBukkit(List<Player> players) {
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
