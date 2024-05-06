package com.comphenix.protocol.utility;

import java.lang.reflect.Modifier;

import org.bukkit.Bukkit;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;

/**
 * Static getter for the registry access accessor which is need for most of the methods
 * since 1.20.5 that access the registry and in form.
 */
public class MinecraftRegistryAccess {

	private static MethodAccessor GET_SERVER = null;
	private static MethodAccessor REGISTRY_ACCESS = null;

	// lazy initialized
	private static Object registryAccess = null;

	static {
		if (MinecraftVersion.v1_20_5.atOrAbove()) {
			GET_SERVER = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getCraftServer(), false)
					.getMethod(FuzzyMethodContract.newBuilder()
							.banModifier(Modifier.STATIC)
							.returnDerivedOf(MinecraftReflection.getMinecraftServerClass())
							.build()));

			REGISTRY_ACCESS = Accessors.getMethodAccessor(
					FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass(), false)
					.getMethod(FuzzyMethodContract.newBuilder()
							.banModifier(Modifier.STATIC)
							.returnDerivedOf(MinecraftReflection.getRegistryAccessClass())
							.build()));
		}
	}

	/**
	 * Returns the composite global registry access. Equiv. of
	 * <pre>((CraftServer) Bukkit.getServer()).getServer().registryAccess()</pre>
	 * 
	 * @return composite registy acesss
	 */
	public static Object get() {
		if (GET_SERVER == null || REGISTRY_ACCESS == null) {
			return null;
		}

		if (registryAccess == null) {
			Object server = GET_SERVER.invoke(Bukkit.getServer());
			registryAccess = REGISTRY_ACCESS.invoke(server);
		}

		return registryAccess;
	}
}
