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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.reflect.compiler.CompileListener;
import com.comphenix.protocol.reflect.compiler.CompiledStructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Caches structure modifiers.
 * @author Kristian
 */
public class StructureCache {
	// Structure modifiers
	private static ConcurrentMap<PacketType, StructureModifier<Object>> structureModifiers =
			new ConcurrentHashMap<PacketType, StructureModifier<Object>>();

	private static Set<PacketType> compiling = new HashSet<PacketType>();

	/**
	 * Creates an empty Minecraft packet of the given id.
	 * <p>
	 * Decreated: Use {@link #newPacket(PacketType)} instead.
	 * @param legacyId - legacy (1.6.4) packet id.
	 * @return Created packet.
	 */
	@Deprecated
	public static Object newPacket(int legacyId) {
		return newPacket(PacketType.findLegacy(legacyId));
	}

	/**
	 * Creates an empty Minecraft packet of the given type.
	 * @param type - packet type.
	 * @return Created packet.
	 */
	public static Object newPacket(PacketType type) {
		Class<?> clazz = PacketRegistry.getPacketClassFromType(type, true);

		// Check the return value
		if (clazz != null) {
			// TODO: Optimize DefaultInstances
			Object result = DefaultInstances.DEFAULT.create(clazz);

			if (result != null) {
				return result;
			}
		}
		throw new IllegalArgumentException("Cannot find associated packet class: " + type);
	}

	/**
	 * Retrieve a cached structure modifier for the given packet id.
	 * <p>
	 * Deprecated: Use {@link #getStructure(PacketType)} instead.
	 * @param legacyId - the legacy (1.6.4) packet ID.
	 * @return A structure modifier.
	 */
	@Deprecated
	public static StructureModifier<Object> getStructure(int legacyId) {
		return getStructure(PacketType.findLegacy(legacyId));
	}

	/**
	 * Retrieve a cached structure modifier for the given packet type.
	 * @param type - packet type.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(PacketType type) {
		// Compile structures by default
		return getStructure(type, true);
	}

	/**
	 * Retrieve a cached structure modifier given a packet type.
	 * @param packetType - packet type.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(Class<?> packetType) {
		// Compile structures by default
		return getStructure(packetType, true);
	}

	/**
	 * Retrieve a cached structure modifier given a packet type.
	 * @param packetType - packet type.
	 * @param compile - whether or not to asynchronously compile the structure modifier.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(Class<?> packetType, boolean compile) {
		// Get the ID from the class
		return getStructure(PacketRegistry.getPacketType(packetType), compile);
	}

	/**
	 * Retrieve a cached structure modifier for the given packet ID.
	 * <p>
	 * Deprecated: Use {@link #getStructure(PacketType, boolean)} instead.
	 * @param legacyId - the legacy (1.6.4) packet ID.
	 * @param compile - whether or not to asynchronously compile the structure modifier.
	 * @return A structure modifier.
	 */
	@Deprecated
	public static StructureModifier<Object> getStructure(final int legacyId, boolean compile) {
		return getStructure(PacketType.findLegacy(legacyId), compile);
	}

	/**
	 * Retrieve a cached structure modifier for the given packet type.
	 * @param type - packet type.
	 * @param compile - whether or not to asynchronously compile the structure modifier.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(final PacketType type, boolean compile) {
		StructureModifier<Object> result = structureModifiers.get(type);

		// We don't want to create this for every lookup
		if (result == null) {
			// Use the vanilla class definition
			final StructureModifier<Object> value = new StructureModifier<Object>(
					PacketRegistry.getPacketClassFromType(type, true), MinecraftReflection.getPacketClass(), true);

			result = structureModifiers.putIfAbsent(type, value);

			// We may end up creating multiple modifiers, but we'll agree on which to use
			if (result == null) {
				result = value;
			}
		}

		// Automatically compile the structure modifier
		if (compile && !(result instanceof CompiledStructureModifier)) {
			// Compilation is many orders of magnitude slower than synchronization
			synchronized (compiling) {
				final BackgroundCompiler compiler = BackgroundCompiler.getInstance();

				if (!compiling.contains(type) && compiler != null) {
					compiler.scheduleCompilation(result, new CompileListener<Object>() {
						@Override
						public void onCompiled(StructureModifier<Object> compiledModifier) {
							structureModifiers.put(type, compiledModifier);
						}
					});
					compiling.add(type);
				}
			}
		}
		return result;
	}
}