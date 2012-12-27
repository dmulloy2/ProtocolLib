package com.comphenix.protocol.reflect.cloning;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

/**
 * Represents an object that can clone a specific list of Bukkit- and Minecraft-related objects.
 * 
 * @author Kristian
 */
public class BukkitCloner implements Cloner {
	// List of classes we support
	private Class<?>[] clonableClasses = { MinecraftReflection.getItemStackClass(), MinecraftReflection.getChunkPositionClass(), 
										   MinecraftReflection.getDataWatcherClass() };
	
	private int findMatchingClass(Class<?> type) {
		// See if is a subclass of any of our supported superclasses
		for (int i = 0; i < clonableClasses.length; i++) {
			if (clonableClasses[i].isAssignableFrom(type))
				return i;
		}
		
		return -1;
	}
	
	@Override
	public boolean canClone(Object source) {
		if (source == null)
			return false;
		
		return findMatchingClass(source.getClass()) >= 0;
	}
	
	@Override
	public Object clone(Object source) {
		if (source == null)
			throw new IllegalArgumentException("source cannot be NULL.");
		
		// Convert to a wrapper 
		switch (findMatchingClass(source.getClass())) {
			case 0:
				return MinecraftReflection.getMinecraftItemStack(MinecraftReflection.getBukkitItemStack(source).clone());
			case 1:
				EquivalentConverter<ChunkPosition> chunkConverter = ChunkPosition.getConverter();
				return chunkConverter.getGeneric(clonableClasses[1], chunkConverter.getSpecific(source));
			case 2:
				EquivalentConverter<WrappedDataWatcher> dataConverter = BukkitConverters.getDataWatcherConverter();
				return dataConverter.getGeneric(clonableClasses[2], dataConverter.getSpecific(source).deepClone());
			default:
				throw new IllegalArgumentException("Cannot clone objects of type " + source.getClass());
		}
	}
}
