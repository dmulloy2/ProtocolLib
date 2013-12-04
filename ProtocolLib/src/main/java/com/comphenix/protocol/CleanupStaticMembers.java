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

package com.comphenix.protocol;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.server.AbstractInputStreamLookup;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.injector.spigot.SpigotPacketInjector;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodUtils;
import com.comphenix.protocol.reflect.ObjectWriter;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.reflect.compiler.StructureCompiler;
import com.comphenix.protocol.reflect.instances.CollectionGenerator;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.PrimitiveGenerator;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;

/**
 * Used to fix ClassLoader leaks that may lead to filling up the permanent generation.
 * 
 * @author Kristian
 */
class CleanupStaticMembers {
	// Reports
	public final static ReportType REPORT_CANNOT_RESET_FIELD = new ReportType("Unable to reset field %s: %s");
	public final static ReportType REPORT_CANNOT_UNLOAD_CLASS = new ReportType("Unable to unload class %s.");
	
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
		@SuppressWarnings("deprecation")
		Class<?>[] publicClasses = { 
				AsyncListenerHandler.class, ListeningWhitelist.class, PacketContainer.class, 
				BukkitUnwrapper.class, DefaultInstances.class, CollectionGenerator.class,
				PrimitiveGenerator.class, FuzzyReflection.class, MethodUtils.class, 
				BackgroundCompiler.class, StructureCompiler.class,
				ObjectWriter.class, Packets.Server.class, Packets.Client.class, 
				ChunkPosition.class, WrappedDataWatcher.class, WrappedWatchableObject.class,
				AbstractInputStreamLookup.class, TemporaryPlayerFactory.class, SpigotPacketInjector.class,
				MinecraftReflection.class, NbtBinarySerializer.class
		};
							   			
		String[] internalClasses = {
			 "com.comphenix.protocol.events.SerializedOfflinePlayer",
			 "com.comphenix.protocol.injector.player.InjectedServerConnection",
			 "com.comphenix.protocol.injector.player.NetworkFieldInjector",
			 "com.comphenix.protocol.injector.player.NetworkObjectInjector",
			 "com.comphenix.protocol.injector.player.NetworkServerInjector",
			 "com.comphenix.protocol.injector.player.PlayerInjector",
			 "com.comphenix.protocol.injector.EntityUtilities",
			 "com.comphenix.protocol.injector.packet.PacketRegistry",
			 "com.comphenix.protocol.injector.packet.PacketInjector",
			 "com.comphenix.protocol.injector.packet.ReadPacketModifier",
			 "com.comphenix.protocol.injector.StructureCache",
			 "com.comphenix.protocol.reflect.compiler.BoxingHelper",
			 "com.comphenix.protocol.reflect.compiler.MethodDescriptor",
			 "com.comphenix.protocol.wrappers.nbt.WrappedElement",
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
					!type.isPrimitive() && !type.equals(String.class) && 
					!type.equals(ReportType.class)) {
				
				try {
					setFinalStatic(field, null);
				} catch (IllegalAccessException e) {
					// Just inform the player
					reporter.reportWarning(this, 
							Report.newBuilder(REPORT_CANNOT_RESET_FIELD).error(e).messageParam(field.getName(), e.getMessage())
					);
					e.printStackTrace();
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
				reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_UNLOAD_CLASS).error(e).messageParam(name));
			}
		}
		
		return output.toArray(new Class<?>[0]);
	}
}
