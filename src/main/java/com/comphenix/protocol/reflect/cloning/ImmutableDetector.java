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

package com.comphenix.protocol.reflect.cloning;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.security.PublicKey;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.crypto.SecretKey;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;

/**
 * Detects classes that are immutable, and thus doesn't require cloning.
 * <p>
 * This ought to have no false positives, but plenty of false negatives.
 * 
 * @author Kristian
 */
public class ImmutableDetector implements Cloner {
	// Notable immutable classes we might encounter
	private static final Set<Class<?>> immutableClasses = ImmutableSet.of(
			StackTraceElement.class, BigDecimal.class,
			BigInteger.class, Locale.class, UUID.class,
			URL.class, URI.class, Inet4Address.class,
			Inet6Address.class, InetSocketAddress.class,
			SecretKey.class, PublicKey.class
	);

	private static final Set<Class<?>> immutableNMS = Sets.newConcurrentHashSet();

	static {
		add(MinecraftReflection::getGameProfileClass);
		add(MinecraftReflection::getDataWatcherSerializerClass);
		add(() -> MinecraftReflection.getMinecraftClass("SoundEffect"));
		add(MinecraftReflection::getBlockClass);
		add(MinecraftReflection::getItemClass);

		if (MinecraftVersion.atOrAbove(MinecraftVersion.AQUATIC_UPDATE)) {
			add(() -> MinecraftReflection.getMinecraftClass("Particle"));
			add(MinecraftReflection::getFluidTypeClass);
			add(MinecraftReflection::getParticleTypeClass);
		}

		if (MinecraftVersion.atOrAbove(MinecraftVersion.VILLAGE_UPDATE)) {
			add(() -> MinecraftReflection.getMinecraftClass("EntityTypes"));
		}
	}

	private static void add(Supplier<Class<?>> getClass) {
		try {
			Class<?> clazz = getClass.get();
			if (clazz != null) {
				immutableNMS.add(clazz);
			}
		} catch (RuntimeException ignored) { }
	}
	
	@Override
	public boolean canClone(Object source) {
		// Don't accept NULL
		if (source == null) {
			return false;
		}
		
		return isImmutable(source.getClass());
	}
	
	/**
	 * Determine if the given type is probably immutable.
	 * @param type - the type to check.
	 * @return TRUE if the type is immutable, FALSE otherwise.
	 */
	public static boolean isImmutable(Class<?> type) {
		// Cases that are definitely not true
		if (type.isArray()) {
			return false;
		}
		
		// All primitive types
		if (Primitives.isWrapperType(type) || String.class.equals(type)) {
			return true;
		}

		// May not be true, but if so, that kind of code is broken anyways
		if (isEnumWorkaround(type)) {
			return true;
		}

		// No good way to clone lambdas
		if (type.getName().contains("$$Lambda$")) {
			return true;
		}

		if (immutableClasses.contains(type)) {
			return true;
		}

		for (Class<?> clazz : immutableNMS) {
			if (MinecraftReflection.is(clazz, type)) {
				return true;
			}
		}

		// Probably not
		return false;
	}
	
	// This is just great. Just great.
	private static boolean isEnumWorkaround(Class<?> enumClass) {
		while (enumClass != null) {
			if (enumClass.isEnum()) {
				return true;
			}

			enumClass = enumClass.getSuperclass();
		}

		return false;
	}
	
	@Override
	public Object clone(Object source) {
		// Safe if the class is immutable
		return source;
	}
}
