package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.comphenix.protocol.BukkitInitialization;
import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WrappedGameProfileTest {

	@BeforeAll
	public static void initializeBukkit() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void testWrapper() {
		GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes("ProtocolLib".getBytes(Charsets.UTF_8)),
				"ProtocolLib");
		WrappedGameProfile wrapper = WrappedGameProfile.fromHandle(profile);

		assertEquals(profile.getId(), wrapper.getUUID());
		assertEquals(profile.getName(), wrapper.getName());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testSkinUpdate() {
		final UUID uuid = UUID.nameUUIDFromBytes("123".getBytes(Charsets.UTF_8));

		assertNull(new WrappedGameProfile((String) null, "Test").getId());
		assertEquals(uuid, new WrappedGameProfile("123", "Test").getUUID());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testNullFailure() {
		assertThrows(RuntimeException.class, () -> new WrappedGameProfile((String) null, null));
	}

	@Test
	public void testGetProperties() {
		GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes("ProtocolLib".getBytes(Charsets.UTF_8)),
				"ProtocolLib");

		String name = "test";
		String value = "test";
		String signature = null;

		profile.getProperties().put(name, new Property(name, value, signature));

		WrappedGameProfile wrapper = WrappedGameProfile.fromHandle(profile);
		Multimap<String, WrappedSignedProperty> properties = wrapper.getProperties();
		WrappedSignedProperty property = properties.get(name).iterator().next();

		assertEquals(property.getName(), name);
		assertEquals(property.getValue(), value);
		assertEquals(property.getSignature(), signature);
	}

	@Test
	public void testAddProperties() {
		String name = "test";
		String value = "test";
		String signature = null;

		WrappedGameProfile wrapper = new WrappedGameProfile(UUID.nameUUIDFromBytes("ProtocolLib".getBytes(Charsets.UTF_8)),
				"ProtocolLib");
		wrapper.getProperties().put(name, new WrappedSignedProperty(name, value, signature));

		GameProfile profile = (GameProfile) wrapper.getHandle();
		PropertyMap properties = profile.getProperties();
		Property property = properties.get(name).iterator().next();

		assertEquals(property.getName(), name);
		assertEquals(property.getValue(), value);
		assertEquals(property.getSignature(), signature);
	}
}
