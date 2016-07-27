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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	// Fix: PaperSpigot changed the trackedPlayers field
	private static Collection<?> getCollection(Object value) {
		if (value instanceof Collection<?>) {
			return (Collection<?>) value;
		} else if (value instanceof Map<?, ?>) {
			return ((Map<?, ?>) value).keySet();
		} else {
			throw new IllegalArgumentException("Expected Collection or Map but got " + value.getClass());
		}
	}

	/*
	 * While this function may look pretty bad, it's essentially just a reflection-warped 
	 * version of the following:
	 * 
	 *  	@SuppressWarnings("unchecked")
	 *	 	public static void updateEntity2(Entity entity, List<Player> observers) {
	 *
	 *			World world = entity.getWorld();
	 *			WorldServer worldServer = ((CraftWorld) world).getHandle();
	 *
	 *			EntityTracker tracker = worldServer.tracker;
	 *			EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities.get(entity.getEntityId());
	 *
	 *			List<EntityPlayer> nmsPlayers = getNmsPlayers(observers);
	 *
	 *			entry.trackedPlayers.removeAll(nmsPlayers);
	 *			entry.scanPlayers(nmsPlayers);
	 *		}
	 *
	 *		private static List<EntityPlayer> getNmsPlayers(List<Player> players) {
	 *			List<EntityPlayer> nsmPlayers = new ArrayList<EntityPlayer>();
	 *	
	 *			for (Player bukkitPlayer : players) {
	 *				CraftPlayer craftPlayer = (CraftPlayer) bukkitPlayer;
	 *				nsmPlayers.add(craftPlayer.getHandle());
	 *			}
	 *	
	 *			return nsmPlayers;
	 *		}
	 *
	 */
	public static void updateEntity(Entity entity, List<Player> observers) throws FieldAccessException {
		try {
			// Fix: Throw a more informative error
			Object trackerEntry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
			if (trackerEntry == null) {
				throw new IllegalArgumentException("Cannot find entity trackers for " + entity + (entity.isDead() ? " - entity is dead." : "."));
			}
			
			if (trackedPlayersField == null) {
				// This one is fairly easy
				trackedPlayersField = FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*");
			}
			
			// Phew, finally there.
			Collection<?> trackedPlayers = getCollection(FieldUtils.readField(trackedPlayersField, trackerEntry, false));
			List<Object> nmsPlayers = unwrapBukkit(observers);
			
			trackedPlayers.removeAll(nmsPlayers);
			
			// We have to rely on a NAME once again. Damn it.
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
		try {
			List<Player> result = new ArrayList<Player>();
			
			Object trackerEntry = getEntityTrackerEntry(entity.getWorld(), entity.getEntityId());
			if (trackerEntry == null) {
				throw new IllegalArgumentException("Cannot find entity trackers for " + entity + (entity.isDead() ? " - entity is dead." : "."));
			}
			
			if (trackedPlayersField == null) {
				trackedPlayersField = FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*");
			}
			
			Collection<?> trackedPlayers = getCollection(FieldUtils.readField(trackedPlayersField, trackerEntry, false));
			
			// Wrap every player - we also ensure that the underlying tracker list is immutable
			for (Object tracker : trackedPlayers) {
				if (MinecraftReflection.isMinecraftPlayer(tracker)) {
					result.add((Player) MinecraftReflection.getBukkitEntity(tracker));
				}
			}
			return result;
			
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Security limitation prevented access to the list of tracked players.", e);
		} catch (InvocationTargetException e) {
			throw new FieldAccessException("Exception occurred in Minecraft.", e);
		}
	}
	
	/**
	 * Retrieve the entity tracker entry given a ID.
	 * @param world - world server.
	 * @param entityID - entity ID.
	 * @return The entity tracker entry.
	 * @throws FieldAccessException 
	 */
	private static Object getEntityTrackerEntry(World world, int entityID) throws FieldAccessException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
		
		if (trackedEntitiesField == null) {
			@SuppressWarnings("rawtypes")
			Set<Class> ignoredTypes = new HashSet<Class>(); 
			
			// Well, this is more difficult. But we're looking for a Minecraft object that is not 
			// created by the constructor(s).
			for (Constructor<?> constructor : tracker.getClass().getConstructors()) {
				for (Class<?> type : constructor.getParameterTypes()) {
					ignoredTypes.add(type);
				}
			}
			
			// The Minecraft field that's NOT filled in by the constructor
			trackedEntitiesField = FuzzyReflection.fromObject(tracker, true).
						getFieldByType(MinecraftReflection.getMinecraftObjectRegex(), ignoredTypes);
		}
		
		// Read the entity hashmap
		Object trackedEntities = null;
		
		try {
			trackedEntities = FieldUtils.readField(trackedEntitiesField, tracker, true);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot access 'trackedEntities' field due to security limitations.", e);
		}
		
		return WrappedIntHashMap.fromHandle(trackedEntities).get(entityID);
	}
	
	/**
	 * Retrieve entity from a ID, even it it's newly created.
	 * @return The asssociated entity.
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
						trackerField = trackerEntry.getClass().getField("tracker");
					} catch (NoSuchFieldException e) {
						// Assume it's the first public entity field then
						trackerField = FuzzyReflection.fromObject(trackerEntry).getFieldByType(
								"tracker", MinecraftReflection.getEntityClass());
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
