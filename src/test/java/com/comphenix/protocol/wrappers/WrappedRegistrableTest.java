package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WrappedRegistrableTest {

    @BeforeAll
    static void initialize() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testRegistrables() {
        // some randomly selected registrables which we can prove that work using the bukkit api
        validate(MinecraftReflection.getEntityTypes(), EntityType.WARDEN.getKey());
        validate(MinecraftReflection.getItemClass(), Material.DIAMOND_AXE.getKey());
        validate(MinecraftReflection.getAttributeBase(), Attribute.GENERIC_MAX_HEALTH.getKey());
        validate(MinecraftReflection.getSoundEffectClass(), Sound.ENTITY_WARDEN_SNIFF.getKey());
        validate(MinecraftReflection.getMobEffectListClass(), PotionEffectType.REGENERATION.getKey());
    }

    void validate(Class<?> registryType, NamespacedKey key) {
        MinecraftKey minecraftKey = new MinecraftKey(key.getNamespace(), key.getKey());
        WrappedRegistrable registrable = WrappedRegistrable.fromClassAndKey(registryType, minecraftKey);
        assertNotNull(registrable);

        Object registrableHandle = registrable.getHandle();
        assertNotNull(registrableHandle);
        assertInstanceOf(registryType, registrableHandle);

        MinecraftKey registrableKey = registrable.getKey();
        assertEquals(key.getNamespace(), registrableKey.getPrefix());
        assertEquals(key.getKey(), registrableKey.getKey());
    }
}

