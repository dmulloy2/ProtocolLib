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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.reflect.compiler.CompiledStructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.ByteBuddyFactory;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.ZeroBuffer;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Caches structure modifiers.
 *
 * @author Kristian
 */
public class StructureCache {

	// prevent duplicate compilations
	private static final Set<PacketType> COMPILING = new HashSet<>();

	// Structure modifiers
	private static final Map<Class<?>, Supplier<Object>> PACKET_INSTANCE_CREATORS = new ConcurrentHashMap<>();
	private static final Map<PacketType, StructureModifier<Object>> STRUCTURE_MODIFIER_CACHE = new ConcurrentHashMap<>();

	// packet data serializer which always returns an empty nbt tag compound
	private static boolean TRICK_TRIED = false;
	private static ConstructorAccessor TRICKED_DATA_SERIALIZER;

	public static Object newPacket(Class<?> clazz) {
		Object result = DefaultInstances.DEFAULT.create(clazz);

		if (result == null) {
			return PACKET_INSTANCE_CREATORS.computeIfAbsent(clazz, $ -> {
				ConstructorAccessor accessor = Accessors.getConstructorAccessorOrNull(clazz,
						MinecraftReflection.getPacketDataSerializerClass());
				if (accessor != null) {
					return () -> {
						try {
							return accessor.invoke(MinecraftReflection.getPacketDataSerializer(new ZeroBuffer()));
						} catch (Exception exception) {
							// try trick nms around as they want a non-null compound in the map_chunk packet constructor
							ConstructorAccessor trickyDataSerializerAccessor = getTrickDataSerializerOrNull();
							if (trickyDataSerializerAccessor != null) {
								return accessor.invoke(trickyDataSerializerAccessor.invoke(new ZeroBuffer()));
							}
							// the tricks are over
							throw new IllegalArgumentException("Unable to create packet " + clazz, exception);
						}
					};
				}
				throw new IllegalArgumentException("No matching constructor to create packet in class " + clazz);
			}).get();
		}

		return result;
	}

	/**
	 * Creates an empty Minecraft packet of the given type.
	 *
	 * @param type - packet type.
	 * @return Created packet.
	 */
	public static Object newPacket(PacketType type) {
		return newPacket(PacketRegistry.getPacketClassFromType(type));
	}

	/**
	 * Retrieve a cached structure modifier for the given packet type.
	 *
	 * @param type - packet type.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(PacketType type) {
		// Compile structures by default
		return getStructure(type, true);
	}

	/**
	 * Retrieve a cached structure modifier given a packet type.
	 *
	 * @param packetType - packet type.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(Class<?> packetType) {
		// Compile structures by default
		return getStructure(packetType, true);
	}

	/**
	 * Retrieve a cached structure modifier given a packet type.
	 *
	 * @param packetType - packet type.
	 * @param compile    - whether or not to asynchronously compile the structure modifier.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(Class<?> packetType, boolean compile) {
		// Get the ID from the class
		PacketType type = PacketRegistry.getPacketType(packetType);
		Preconditions.checkNotNull(type, "No packet type associated with " + packetType);
		return getStructure(type, compile);
	}

	/**
	 * Retrieve a cached structure modifier for the given packet type.
	 *
	 * @param packetType - packet type.
	 * @param compile    - whether or not to asynchronously compile the structure modifier.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(final PacketType packetType, boolean compile) {
		Preconditions.checkNotNull(packetType, "type cannot be null");

		StructureModifier<Object> modifier = STRUCTURE_MODIFIER_CACHE.computeIfAbsent(packetType, type -> {
			Class<?> packetClass = PacketRegistry.getPacketClassFromType(type);
			return new StructureModifier<>(packetClass, MinecraftReflection.getPacketClass(), true);
		});

		// check if we should compile the structure modifier now
		if (compile && !(modifier instanceof CompiledStructureModifier) && COMPILING.add(packetType)) {
			// compile now
			BackgroundCompiler compiler = BackgroundCompiler.getInstance();
			if (compiler != null) {
				compiler.scheduleCompilation(
						modifier,
						compiled -> STRUCTURE_MODIFIER_CACHE.put(packetType, compiled));
			}
		}

		return modifier;
	}

	/**
	 * Creates a packet data serializer sub-class if needed to allow the fixed read of a NbtTagCompound because of a null
	 * check in the MapChunk packet constructor.
	 *
	 * @return an accessor to a constructor which creates a data serializer.
	 */
	private static ConstructorAccessor getTrickDataSerializerOrNull() {
		if (TRICKED_DATA_SERIALIZER == null && !TRICK_TRIED) {
			// ensure that we only try once to create the class
			TRICK_TRIED = true;
			try {
				// create an empty instance of a nbt tag compound that we can re-use when needed
				Object compound = Accessors.getConstructorAccessor(MinecraftReflection.getNBTCompoundClass()).invoke();
				// create the method in the class to read an empty nbt tag compound (currently used for MAP_CHUNK because of null check)
				Class<?> generatedClass = ByteBuddyFactory.getInstance()
						.createSubclass(MinecraftReflection.getPacketDataSerializerClass())
						.name(MinecraftMethods.class.getPackage().getName() + ".ProtocolLibTricksNmsDataSerializer")
						.method(ElementMatchers.returns(MinecraftReflection.getNBTCompoundClass())
								.and(ElementMatchers.takesArguments(MinecraftReflection.getNBTReadLimiterClass())))
						.intercept(FixedValue.value(compound))
						.make()
						.load(ByteBuddyFactory.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
						.getLoaded();
				TRICKED_DATA_SERIALIZER = Accessors.getConstructorAccessor(generatedClass, ByteBuf.class);
			} catch (Exception ignored) {
				// can happen if unsupported
			}
		}
		return TRICKED_DATA_SERIALIZER;
	}
}
