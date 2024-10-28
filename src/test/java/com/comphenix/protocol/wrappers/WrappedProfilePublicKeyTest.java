package com.comphenix.protocol.wrappers;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.WrappedProfilePublicKey.WrappedProfileKeyData;

import net.minecraft.world.entity.player.ProfilePublicKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        ProfilePublicKey.Data data = assertInstanceOf(ProfilePublicKey.Data.class, handle);

        assertFalse(data.hasExpired());
        assertEquals(timeout, data.expiresAt());
        assertEquals(keyPair.getPublic(), data.key());
        assertArrayEquals(keyData.getSignature(), data.keySignature());

        // test key data unwrapping
        WrappedProfileKeyData unwrapped = BukkitConverters.getWrappedPublicKeyDataConverter().getSpecific(data);
        assertNotNull(unwrapped);
        assertFalse(unwrapped.isExpired());
        assertEquals(keyData.getKey(), unwrapped.getKey());
        assertEquals(keyData.getExpireTime(), unwrapped.getExpireTime());
        assertArrayEquals(keyData.getSignature(), unwrapped.getSignature());

        // test key data wrapping
        Object wrappedData = BukkitConverters.getWrappedPublicKeyDataConverter().getGeneric(keyData);
        ProfilePublicKey.Data wrapped = assertInstanceOf(ProfilePublicKey.Data.class, wrappedData);

        assertFalse(wrapped.hasExpired());
        assertEquals(timeout, wrapped.expiresAt());
        assertEquals(keyPair.getPublic(), wrapped.key());
        assertArrayEquals(keyData.getSignature(), wrapped.keySignature());

        // test profile key unwrapping
        WrappedProfilePublicKey profilePublicKey = new WrappedProfilePublicKey(keyData);

        Object keyHandle = profilePublicKey.getHandle();
        ProfilePublicKey profileKey = assertInstanceOf(ProfilePublicKey.class, keyHandle);

        assertNotNull(profileKey.data());

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
