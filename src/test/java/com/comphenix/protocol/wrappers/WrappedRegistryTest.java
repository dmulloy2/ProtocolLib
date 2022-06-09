package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WrappedRegistryTest {

	@BeforeAll
	static void initialize() {
		BukkitInitialization.initializeAll();
	}

	@Test
	void testRegistries() {
		// some randomly selected registries which we can proof to work using the bukkit api
		validate(MinecraftReflection.getItemClass(), Material.DIAMOND_AXE.getKey());
		validate(MinecraftReflection.getAttributeBase(), Attribute.GENERIC_MAX_HEALTH.getKey());
		validate(MinecraftReflection.getSoundEffectClass(), Sound.ENTITY_WARDEN_SNIFF.getKey());
		validate(MinecraftReflection.getMobEffectListClass(), PotionEffectType.REGENERATION.getKey());
	}

	void validate(Class<?> registryType, NamespacedKey key) {
		WrappedRegistry registry = WrappedRegistry.getRegistry(registryType);
		assertNotNull(registry);

		Object registryEntry = registry.get(key.getKey());
		assertNotNull(registryEntry);
		assertInstanceOf(registryType, registryEntry);

		MinecraftKey entryKey = registry.getKey(registryEntry);
		assertEquals(key.getNamespace(), entryKey.getPrefix());
		assertEquals(key.getKey(), entryKey.getKey());

		int soundId = registry.getId(registryEntry);
		assertNotEquals(-1, soundId);
		assertEquals(soundId, registry.getId(entryKey));
	}
}

