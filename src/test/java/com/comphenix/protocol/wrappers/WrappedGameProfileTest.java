package com.comphenix.protocol.wrappers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.comphenix.protocol.BukkitInitialization;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WrappedGameProfileTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testWrapper() {
        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes("ProtocolLib".getBytes(StandardCharsets.UTF_8)),
                "ProtocolLib");
        WrappedGameProfile wrapper = WrappedGameProfile.fromHandle(profile);

        assertEquals(profile.id(), wrapper.getUUID());
        assertEquals(profile.name(), wrapper.getName());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testSkinUpdate() {
        final UUID uuid = UUID.nameUUIDFromBytes("123".getBytes(StandardCharsets.UTF_8));

        assertNull(new WrappedGameProfile((String) null, "Test").getId());
        assertEquals(uuid, new WrappedGameProfile("123", "Test").getUUID());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testNullFailure() {
        assertThrows(RuntimeException.class, () -> new WrappedGameProfile((String) null, null));
    }

    @Test
    void testGetProperties() {
        String name = "test";
        String value = "test";
        String signature = null;

        Multimap<String, Property> properties = ImmutableListMultimap.of(name, new Property(name, value));

        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes("ProtocolLib".getBytes(StandardCharsets.UTF_8)),
                "ProtocolLib", new PropertyMap(properties));

        WrappedGameProfile wrapper = WrappedGameProfile.fromHandle(profile);
        Multimap<String, WrappedSignedProperty> wrappedProperties = wrapper.getProperties();
        WrappedSignedProperty property = wrappedProperties.get(name).iterator().next();

        assertEquals(property.getName(), name);
        assertEquals(property.getValue(), value);
        assertEquals(property.getSignature(), signature);
    }

    @Test
    void testAddProperties() {
        String name = "test";
        String value = "test";
        String signature = null;

        WrappedGameProfile wrapper = new WrappedGameProfile(UUID.nameUUIDFromBytes("ProtocolLib".getBytes(StandardCharsets.UTF_8)),
                "ProtocolLib");
        wrapper.getProperties().put(name, new WrappedSignedProperty(name, value, signature));

        GameProfile profile = (GameProfile) wrapper.getHandle();
        PropertyMap properties = profile.properties();
        Property property = properties.get(name).iterator().next();

        assertEquals(property.name(), name);
        assertEquals(property.value(), value);
        assertEquals(property.signature(), signature);
    }
}
