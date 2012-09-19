package com.comphenix.protocol.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.injector.PacketConstructor.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
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
	
	private static Method hashGetMethod;
	private static Method scanPlayersMethod;
	
	public static void updateEntity(Entity entity, List<Player> observers) throws FieldAccessException {
		
		World world = entity.getWorld();
		Object worldServer = ((CraftWorld) world).getHandle();

		// We have to rely on the class naming here.
		if (entityTrackerField == null)
			entityTrackerField = FuzzyReflection.fromObject(worldServer).getFieldByType(".*Tracker");
		
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
						getFieldByType(FuzzyReflection.MINECRAFT_OBJECT, ignoredTypes);
		}
		
		// Read the entity hashmap
		Object trackedEntities = null;

		try {
			trackedEntities = FieldUtils.readField(trackedEntitiesField, tracker, true);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot access 'trackedEntities' field due to security limitations.", e);
		}
		
		// Getting the "get" method is pretty hard, but first - try to just get it by name
		if (hashGetMethod == null) {
			
			Class<?> type = trackedEntities.getClass();
			
			try {
				hashGetMethod = type.getMethod("get", int.class);
			} catch (NoSuchMethodException e) {
			
				Class<?>[] params = { int.class };
				
				// Then it's probably the lowest named method that takes an int-parameter
				for (Method method : type.getMethods()) {
					if (Arrays.equals(params, method.getParameterTypes())) {
						if (hashGetMethod == null ||
							method.getName().compareTo(hashGetMethod.getName()) < 0) {
							hashGetMethod = method;
						}
					}
				}
			}
		}
	
		try {
			//EntityTrackerEntry trackEntity = (EntityTrackerEntry) tracker.trackedEntities.get(entity.getEntityId());
			 Object trackerEntry = hashGetMethod.invoke(trackedEntities, entity.getEntityId());

			if (trackedPlayersField == null) {
				// This one is fairly easy
				trackedPlayersField = FuzzyReflection.fromObject(trackerEntry).getFieldByType("java\\.util\\..*");
			}
			
			// Phew, finally there.
			Collection<?> trackedPlayers = (Collection<?>) FieldUtils.readField(trackedPlayersField, trackerEntry, false);
			List<Object> nmsPlayers = unwrapBukkit(observers);
			
			// trackEntity.trackedPlayers.clear();
			trackedPlayers.removeAll(nmsPlayers);
			
			// We have to rely on a NAME once again. Damn it.
			if (scanPlayersMethod == null) {
				scanPlayersMethod = trackerEntry.getClass().getMethod("scanPlayers", List.class);
			}
			
			//trackEntity.scanPlayers(server.players);
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
			throw new FieldAccessException("Canot find 'scanPlayers' method. Is ProtocolLib up to date?", e);
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
