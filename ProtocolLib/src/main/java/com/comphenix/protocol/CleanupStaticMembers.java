package com.comphenix.protocol;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodUtils;
import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.reflect.compiler.StructureCompiler;
import com.comphenix.protocol.reflect.instances.CollectionGenerator;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.PrimitiveGenerator;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

/**
 * Used to fix ClassLoader leaks that may lead to filling up the permanent generation.
 * 
 * @author Kristian
 */
class CleanupStaticMembers {

	private ClassLoader loader;
	private ErrorReporter reporter;

	public CleanupStaticMembers(ClassLoader loader, ErrorReporter reporter) {
		this.loader = loader;
		this.reporter = reporter;
	}
	
	/**
	 * Ensure that the previous ClassLoader is not leaking.
	 */
	public void resetAll() {
		// This list must always be updated
		Class<?>[] publicClasses = { 
				AsyncListenerHandler.class, ListeningWhitelist.class, PacketContainer.class, 
				BukkitUnwrapper.class, DefaultInstances.class, CollectionGenerator.class,
				PrimitiveGenerator.class, FuzzyReflection.class, MethodUtils.class, 
				BackgroundCompiler.class, StructureCompiler.class,
				ObjectWriter.class, Packets.Server.class, Packets.Client.class, 
				ChunkPosition.class, WrappedDataWatcher.class, WrappedWatchableObject.class
		};
							   			
		String[] internalClasses = {
			 "com.comphenix.protocol.events.SerializedOfflinePlayer",
			 "com.comphenix.protocol.injector.player.InjectedServerConnection",
			 "com.comphenix.protocol.injector.player.NetworkFieldInjector",
			 "com.comphenix.protocol.injector.player.NetworkObjectInjector",
			 "com.comphenix.protocol.injector.player.NetworkServerInjector",
			 "com.comphenix.protocol.injector.player.PlayerInjector",
			 "com.comphenix.protocol.injector.player.TemporaryPlayerFactory",
			 "com.comphenix.protocol.injector.EntityUtilities",
			 "com.comphenix.protocol.injector.MinecraftRegistry",
			 "com.comphenix.protocol.injector.PacketInjector",
			 "com.comphenix.protocol.injector.ReadPacketModifier",
			 "com.comphenix.protocol.injector.StructureCache",
			 "com.comphenix.protocol.reflect.compiler.BoxingHelper",
			 "com.comphenix.protocol.reflect.compiler.MethodDescriptor"
		};
		
		resetClasses(publicClasses);
		resetClasses(getClasses(loader, internalClasses));
	}
	
	private void resetClasses(Class<?>[] classes) {
		// Reset each class one by one
		for (Class<?> clazz : classes) {
			resetClass(clazz);
		}
	}
	
	private void resetClass(Class<?> clazz) {
		for (Field field : clazz.getFields()) {
			Class<?> type = field.getType();
			
			// Only check static non-primitive fields. We also skip strings.
			if (Modifier.isStatic(field.getModifiers()) && 
					!type.isPrimitive() && !type.equals(String.class)) {
				
				try {
					setFinalStatic(field, null);
				} catch (IllegalAccessException e) {
					// Just inform the player
					reporter.reportWarning(this, "Unable to reset field " + field.getName() + ": " + e.getMessage(), e);
				}
			}
		}
	}

	// HACK! HAACK!
	private static void setFinalStatic(Field field, Object newValue) throws IllegalAccessException {
		int modifier = field.getModifiers();
		boolean isFinal = Modifier.isFinal(modifier);
		
		Field modifiersField = isFinal ? FieldUtils.getField(Field.class, "modifiers", true) : null;

		// We have to remove the final field first
		if (isFinal) {
			FieldUtils.writeField(modifiersField, field, modifier & ~Modifier.FINAL, true);
		}
			
		// Now we can safely modify the field
		FieldUtils.writeStaticField(field, newValue, true);
		
		// Revert modifier
		if (isFinal) {
			FieldUtils.writeField(modifiersField, field, modifier, true);
		}
	}
	
	private Class<?>[] getClasses(ClassLoader loader, String[] names) {
		List<Class<?>> output = new ArrayList<Class<?>>();
		
		for (String name : names) {
			try {
				output.add(loader.loadClass(name));
			} catch (ClassNotFoundException e) {
				// Warn the user
				reporter.reportWarning(this, "Unable to unload class " + name, e);
			}
		}
		
		return output.toArray(new Class<?>[0]);
	}
}
