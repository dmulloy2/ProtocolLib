/**
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
package com.comphenix.protocol.reflect.cloning;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents an object that can clone a specific list of Bukkit- and Minecraft-related objects.
 *
 * @author Kristian
 */
public class BukkitCloner implements Cloner {
	private static final Map<Class<?>, Function<Object, Object>> CLONERS = new ConcurrentHashMap<>();

	private static void fromWrapper(Supplier<Class<?>> getClass, Function<Object, ClonableWrapper> fromHandle) {
		try {
			Class<?> nmsClass = getClass.get();
			if (nmsClass != null) {
				CLONERS.put(nmsClass, nmsObject -> fromHandle.apply(nmsObject).deepClone().getHandle());
			}
		} catch (Throwable ignored) { }
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void fromConverter(Supplier<Class<?>> getClass, EquivalentConverter converter) {
		try {
			Class<?> nmsClass = getClass.get();
			if (nmsClass != null) {
				CLONERS.put(nmsClass, nmsObject -> converter.getGeneric(converter.getSpecific(nmsObject)));
			}
		} catch (Throwable ignored) { }
	}

	private static void fromManual(Supplier<Class<?>> getClass, Function<Object, Object> cloner) {
		try {
			Class<?> nmsClass = getClass.get();
			if (nmsClass != null) {
				CLONERS.put(nmsClass, cloner);
			}
		} catch (Throwable ignored) { }
	}

	static {
		fromManual(MinecraftReflection::getItemStackClass, source ->
				MinecraftReflection.getMinecraftItemStack(MinecraftReflection.getBukkitItemStack(source).clone()));
		fromWrapper(MinecraftReflection::getDataWatcherClass, WrappedDataWatcher::new);
		fromConverter(MinecraftReflection::getBlockPositionClass, BlockPosition.getConverter());
		fromWrapper(MinecraftReflection::getServerPingClass, WrappedServerPing::fromHandle);
		fromConverter(MinecraftReflection::getMinecraftKeyClass, MinecraftKey.getConverter());
		fromWrapper(MinecraftReflection::getIBlockDataClass, WrappedBlockData::fromHandle);
		fromManual(MinecraftReflection::getNonNullListClass, source -> nonNullListCloner().clone(source));
		fromWrapper(MinecraftReflection::getNBTBaseClass, NbtFactory::fromNMS);
		fromWrapper(MinecraftReflection::getIChatBaseComponentClass, WrappedChatComponent::fromHandle);
		fromWrapper(WrappedVillagerData::getNmsClass, WrappedVillagerData::fromHandle);
		fromConverter(MinecraftReflection::getSectionPositionClass, BukkitConverters.getSectionPositionConverter());

		try {
			fromManual(ComponentConverter::getBaseComponentArrayClass, source ->
					ComponentConverter.clone((BaseComponent[]) source));
		} catch (Throwable ignored) { }

		try {
			fromManual(AdventureComponentConverter::getComponentClass, AdventureComponentConverter::clone);
		} catch (Throwable ignored) { }
	}

	private Function<Object, Object> findCloner(Class<?> type) {
		for (Entry<Class<?>, Function<Object, Object>> entry : CLONERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(type)) {
				return entry.getValue();
			}
		}

		return null;
	}

	@Override
	public boolean canClone(Object source) {
		if (source == null)
			return false;

		return findCloner(source.getClass()) != null;
	}

	@Override
	public Object clone(Object source) {
		if (source == null)
			throw new IllegalArgumentException("source cannot be NULL.");

		return findCloner(source.getClass()).apply(source);
	}

	private static Constructor<?> nonNullList = null;

	private static Cloner nonNullListCloner() {
		return new Cloner() {
			@Override
			public boolean canClone(Object source) {
				return MinecraftReflection.is(MinecraftReflection.getNonNullListClass(), source);
			}

			@Override
			public Object clone(Object source) {
				StructureModifier<Object> modifier = new StructureModifier<>(source.getClass()).withTarget(source);
				List<?> list = (List<?>) modifier.read(0);
				Object empty = modifier.read(1);

				if (nonNullList == null) {
					try {
						nonNullList = source.getClass().getDeclaredConstructor(List.class, Object.class);
						nonNullList.setAccessible(true);
					} catch (ReflectiveOperationException ex) {
						throw new RuntimeException("Could not find NonNullList constructor", ex);
					}
				}

				try {
					return nonNullList.newInstance(new ArrayList<>(list), empty);
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException("Could not create new NonNullList", ex);
				}
			}
		};
	}
}
