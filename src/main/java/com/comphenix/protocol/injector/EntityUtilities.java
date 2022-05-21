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

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedIntHashMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Used to perform certain operations on entities.
 *
 * @author Kristian
 */
class EntityUtilities {

	private static final EntityUtilities INSTANCE = new EntityUtilities();
	private static final boolean NEW_TRACKER = MinecraftVersion.VILLAGE_UPDATE.atOrAbove();

	private final Map<Class<?>, MethodAccessor> scanPlayersMethods = new HashMap<>();

	private FieldAccessor chunkMapField;
	private FieldAccessor entityTrackerField;
	private FieldAccessor trackedPlayersField;
	private FieldAccessor trackedEntitiesField;

	private MethodAccessor getChunkProvider;

	private EntityUtilities() {
	}

	public static EntityUtilities getInstance() {
		return INSTANCE;
	}

	public void updateEntity(Entity entity, List<Player> observers) {
		if (entity == null || !entity.isValid()) {
			return;
		}

		Collection<?> trackedPlayers = this.getTrackedPlayers(entity);
		List<Object> nmsPlayers = this.unwrapBukkit(observers);

		List<Object> removingEntries =
				MinecraftVersion.CAVES_CLIFFS_1.atOrAbove() ? this.getPlayerConnections(nmsPlayers) : nmsPlayers;
		trackedPlayers.removeAll(removingEntries);

		// there can be multiple different entity tracker entry impls, see GH-732....
		Object trackerEntry = this.getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
		this.scanPlayersMethods.computeIfAbsent(trackerEntry.getClass(), this::findScanPlayers)
				.invoke(trackerEntry, nmsPlayers);
	}

	private MethodAccessor findScanPlayers(Class<?> trackerClass) {
		MethodAccessor candidate = Accessors.getMethodAcccessorOrNull(trackerClass, "scanPlayers");
		if (candidate != null) {
			return candidate;
		}

		FuzzyReflection fuzzy = FuzzyReflection.fromClass(trackerClass, true);
		return Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
				.returnTypeVoid()
				.parameterExactArray(List.class)
				.build()));
	}

	/**
	 * Retrieve every client that is receiving information about a given entity.
	 *
	 * @param entity - the entity that is being tracked.
	 * @return Every client/player that is tracking the given entity.
	 * @throws FieldAccessException If reflection failed.
	 */
	public List<Player> getEntityTrackers(Entity entity) {
		if (entity == null || !entity.isValid()) {
			return new ArrayList<>();
		}

		List<Player> result = new ArrayList<>();
		Collection<?> trackedPlayers = this.getTrackedPlayers(entity);

		// Wrap every player - we also ensure that the underlying tracker list is immutable
		for (Object tracker : trackedPlayers) {
			if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove() && MinecraftReflection.isServerHandler(tracker)) {
				result.add(MinecraftReflection.getBukkitPlayerFromConnection(tracker));
			} else if (MinecraftReflection.isMinecraftPlayer(tracker)) {
				result.add((Player) MinecraftReflection.getBukkitEntity(tracker));
			}
		}

		return result;
	}

	private Collection<?> getTrackedPlayers(Entity entity) {
		Validate.notNull(entity, "entity cannot be null");

		Object trackerEntry = this.getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
		Validate.notNull(trackerEntry, "Could not find entity trackers for " + entity);

		if (this.trackedPlayersField == null) {
			this.trackedPlayersField = Accessors.getFieldAccessor(
					FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*"));
		}

		Validate.notNull(this.trackedPlayersField, "Could not find trackedPlayers field");

		Object value = this.trackedPlayersField.get(trackerEntry);
		if (value instanceof Collection) {
			return (Collection<?>) value;
		} else if (value instanceof Map) {
			return ((Map<?, ?>) value).keySet();
		} else {
			// Please. No more changes.
			throw new IllegalStateException(
					"trackedPlayers field was an unknown type: expected Collection or Map, but got " + value.getClass());
		}
	}

	@SuppressWarnings("unchecked")
	private Object getNewEntityTracker(Object worldServer, int entityId) {
		if (this.getChunkProvider == null) {
			Class<?> chunkProviderClass = MinecraftReflection.getChunkProviderServer();
			this.getChunkProvider = Accessors.getMethodAccessor(FuzzyReflection.fromClass(worldServer.getClass(), false)
					.getMethod(FuzzyMethodContract.newBuilder().parameterCount(0).returnTypeExact(chunkProviderClass).build()));
		}

		Object chunkProvider = this.getChunkProvider.invoke(worldServer);

		if (this.chunkMapField == null) {
			Class<?> chunkMapClass = MinecraftReflection.getPlayerChunkMap();
			this.chunkMapField = Accessors.getFieldAccessor(FuzzyReflection.fromClass(chunkProvider.getClass(), false)
					.getField(FuzzyFieldContract.newBuilder().typeExact(chunkMapClass).build()));
		}

		Object playerChunkMap = this.chunkMapField.get(chunkProvider);

		if (this.trackedEntitiesField == null) {
			if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
				this.trackedEntitiesField = Accessors.getFieldAccessor(
						FuzzyReflection.fromClass(playerChunkMap.getClass(), true)
								.getField(FuzzyFieldContract.newBuilder()
										.banModifier(Modifier.STATIC)
										.requirePublic()
										.typeExact(MinecraftReflection.getInt2ObjectMapClass())
										.build()));
			} else {
				this.trackedEntitiesField = Accessors.getFieldAccessor(
						FuzzyReflection.fromClass(playerChunkMap.getClass(), false).getField(
								FuzzyFieldContract.newBuilder().typeDerivedOf(Map.class).nameExact("trackedEntities").build()));
			}
		}

		Map<Integer, Object> trackedEntities = (Map<Integer, Object>) this.trackedEntitiesField.get(playerChunkMap);
		return trackedEntities.get(entityId);
	}

	private Object getEntityTrackerEntry(World world, int entityID) {
		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		Object worldServer = unwrapper.unwrapItem(world);

		if (NEW_TRACKER) {
			return this.getNewEntityTracker(worldServer, entityID);
		}

		if (this.entityTrackerField == null) {
			this.entityTrackerField = Accessors.getFieldAccessor(FuzzyReflection.fromObject(worldServer).
					getFieldByType("tracker", MinecraftReflection.getEntityTrackerClass()));
		}

		// Get the tracker
		Object tracker = this.entityTrackerField.get(worldServer);

		// Looking for an IntHashMap in the tracker entry
		if (this.trackedEntitiesField == null) {
			this.trackedEntitiesField = Accessors.getFieldAccessor(FuzzyReflection.fromObject(tracker, false)
					.getFieldByType("trackedEntities", MinecraftReflection.getIntHashMapClass()));
		}

		// Read the map
		Object trackedEntities = this.trackedEntitiesField.get(tracker);
		return WrappedIntHashMap.fromHandle(trackedEntities).get(entityID);
	}

	private List<Object> getPlayerConnections(List<Object> nmsPlayers) {
		List<Object> connections = new ArrayList<>(nmsPlayers.size());
		nmsPlayers.forEach(nmsPlayer -> connections.add(MinecraftFields.getPlayerConnection(nmsPlayer)));
		return connections;
	}

	private List<Object> unwrapBukkit(List<Player> players) {
		List<Object> output = Lists.newArrayList();
		BukkitUnwrapper unwrapper = new BukkitUnwrapper();

		// Get the NMS equivalent
		for (Player player : players) {
			Object result = unwrapper.unwrapItem(player);

			if (result != null) {
				output.add(result);
			} else {
				throw new IllegalArgumentException("Cannot unwrap item " + player);
			}
		}

		return output;
	}
}
