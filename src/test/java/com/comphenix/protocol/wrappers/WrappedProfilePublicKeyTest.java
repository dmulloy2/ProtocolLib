package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.WrappedProfilePublicKey.WrappedProfileKeyData;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WrappedProfilePublicKeyTest {

	private static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		return generator.generateKeyPair();
	}

	@BeforeAll
	static void initializeBukkit() {
		BukkitInitialization.initializeAll();
	}

	@Test
	void testPublicKey() throws Exception {
		KeyPair keyPair = generateKeyPair();
		Instant timeout = Instant.now().plusSeconds(120);

		// test key data conversion
		WrappedProfileKeyData keyData = new WrappedProfileKeyData(timeout, keyPair.getPublic(), new byte[]{0x01, 0x0F});

		Object handle = keyData.getHandle();
		ProfilePublicKey.a data = assertInstanceOf(ProfilePublicKey.a.class, handle);

		assertFalse(data.a());
		assertEquals(timeout, data.b());
		assertEquals(keyPair.getPublic(), data.c());
		assertArrayEquals(keyData.getSignature(), data.d());

		// test key data unwrapping
		WrappedProfileKeyData unwrapped = BukkitConverters.getWrappedPublicKeyDataConverter().getSpecific(data);
		assertNotNull(unwrapped);
		assertFalse(unwrapped.isExpired());
		assertEquals(keyData.getKey(), unwrapped.getKey());
		assertEquals(keyData.getExpireTime(), unwrapped.getExpireTime());
		assertArrayEquals(keyData.getSignature(), unwrapped.getSignature());

		// test key data wrapping
		Object wrappedData = BukkitConverters.getWrappedPublicKeyDataConverter().getGeneric(keyData);
		ProfilePublicKey.a wrapped = assertInstanceOf(ProfilePublicKey.a.class, wrappedData);

		assertFalse(wrapped.a());
		assertEquals(timeout, wrapped.b());
		assertEquals(keyPair.getPublic(), wrapped.c());
		assertArrayEquals(keyData.getSignature(), wrapped.d());

		// test profile key unwrapping
		WrappedProfilePublicKey profilePublicKey = new WrappedProfilePublicKey(keyData);

		Object keyHandle = profilePublicKey.getHandle();
		ProfilePublicKey profileKey = assertInstanceOf(ProfilePublicKey.class, keyHandle);

		assertNotNull(profileKey.b());

		// test profile key wrapping
		WrappedProfilePublicKey wrappedKey = BukkitConverters.getWrappedProfilePublicKeyConverter().getSpecific(keyHandle);
		assertNotNull(wrappedKey);
		assertNotNull(wrappedKey.getKeyData());

		WrappedProfileKeyData wrappedKeyData = wrappedKey.getKeyData();
		assertFalse(wrappedKeyData.isExpired());
		assertEquals(keyData.getKey(), wrappedKeyData.getKey());
		assertEquals(keyData.getExpireTime(), wrappedKeyData.getExpireTime());
		assertArrayEquals(keyData.getSignature(), wrappedKeyData.getSignature());
	}
}
