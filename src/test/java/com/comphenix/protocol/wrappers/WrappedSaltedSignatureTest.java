package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.util.MinecraftEncryption;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedSaltedSignatureTest {

    @BeforeAll
    static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testLoginSignature() {
        long salt = ThreadLocalRandom.current().nextLong();
        byte[] signature = new byte[512];
        ThreadLocalRandom.current().nextBytes(signature);

        // test key data conversion
        WrappedSaltedSignature loginSignature = new WrappedSaltedSignature(salt, signature);

        Object handle = loginSignature.getHandle();
        MinecraftEncryption.b data = assertInstanceOf(MinecraftEncryption.b.class, handle);

        assertTrue(data.a());
        assertArrayEquals(signature, data.d());
        assertEquals(salt, data.c());

        // test key data unwrapping
        WrappedSaltedSignature unwrapped = BukkitConverters.getWrappedSignatureConverter().getSpecific(data);
        assertNotNull(unwrapped);
        assertTrue(unwrapped.isSigned());
        assertEquals(loginSignature.getSalt(), unwrapped.getSalt());
        assertArrayEquals(loginSignature.getSignature(), unwrapped.getSignature());
        assertArrayEquals(loginSignature.getSaltBytes(), unwrapped.getSaltBytes());

        // test key data wrapping
        Object wrappedData = BukkitConverters.getWrappedSignatureConverter().getGeneric(loginSignature);
        MinecraftEncryption.b wrapped = assertInstanceOf(MinecraftEncryption.b.class, wrappedData);

        assertTrue(wrapped.a());
        assertEquals(loginSignature.getSalt(), wrapped.c());
        assertArrayEquals(loginSignature.getSignature(), wrapped.d());
        assertArrayEquals(loginSignature.getSaltBytes(), wrapped.b());
    }

    @Test
    void testSignedMessageWithoutSignature() {
        long salt = ThreadLocalRandom.current().nextLong();
        byte[] signature = {};

        // test key data conversion
        WrappedSaltedSignature loginSignature = new WrappedSaltedSignature(salt, signature);

        Object handle = loginSignature.getHandle();
        MinecraftEncryption.b data = assertInstanceOf(MinecraftEncryption.b.class, handle);

        assertFalse(data.a());
        assertArrayEquals(signature, data.d());
        assertEquals(salt, data.c());

        // test key data unwrapping
        WrappedSaltedSignature unwrapped = BukkitConverters.getWrappedSignatureConverter().getSpecific(data);
        assertNotNull(unwrapped);
        assertFalse(unwrapped.isSigned());
        assertEquals(loginSignature.getSalt(), unwrapped.getSalt());
        assertArrayEquals(loginSignature.getSignature(), unwrapped.getSignature());
        assertArrayEquals(loginSignature.getSaltBytes(), unwrapped.getSaltBytes());

        // test key data wrapping
        Object wrappedData = BukkitConverters.getWrappedSignatureConverter().getGeneric(loginSignature);
        MinecraftEncryption.b wrapped = assertInstanceOf(MinecraftEncryption.b.class, wrappedData);

        assertFalse(wrapped.a());
        assertEquals(loginSignature.getSalt(), wrapped.c());
        assertArrayEquals(loginSignature.getSignature(), wrapped.d());
        assertArrayEquals(loginSignature.getSaltBytes(), wrapped.b());
    }
}
