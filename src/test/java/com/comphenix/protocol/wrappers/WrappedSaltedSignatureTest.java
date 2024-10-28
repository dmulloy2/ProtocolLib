package com.comphenix.protocol.wrappers;

import java.util.concurrent.ThreadLocalRandom;

import com.comphenix.protocol.BukkitInitialization;

import net.minecraft.util.Crypt;
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
        Crypt.SaltSignaturePair data = assertInstanceOf(Crypt.SaltSignaturePair.class, handle);

        assertTrue(data.isValid());
        assertArrayEquals(signature, data.signature());
        assertEquals(salt, data.salt());

        // test key data unwrapping
        WrappedSaltedSignature unwrapped = BukkitConverters.getWrappedSignatureConverter().getSpecific(data);
        assertNotNull(unwrapped);
        assertTrue(unwrapped.isSigned());
        assertEquals(loginSignature.getSalt(), unwrapped.getSalt());
        assertArrayEquals(loginSignature.getSignature(), unwrapped.getSignature());
        assertArrayEquals(loginSignature.getSaltBytes(), unwrapped.getSaltBytes());

        // test key data wrapping
        Object wrappedData = BukkitConverters.getWrappedSignatureConverter().getGeneric(loginSignature);
        Crypt.SaltSignaturePair wrapped = assertInstanceOf(Crypt.SaltSignaturePair.class, wrappedData);

        assertTrue(wrapped.isValid());
        assertEquals(loginSignature.getSalt(), wrapped.salt());
        assertArrayEquals(loginSignature.getSignature(), wrapped.signature());
        assertArrayEquals(loginSignature.getSaltBytes(), wrapped.saltAsBytes());
    }

    @Test
    void testSignedMessageWithoutSignature() {
        long salt = ThreadLocalRandom.current().nextLong();
        byte[] signature = {};

        // test key data conversion
        WrappedSaltedSignature loginSignature = new WrappedSaltedSignature(salt, signature);

        Object handle = loginSignature.getHandle();
        Crypt.SaltSignaturePair data = assertInstanceOf(Crypt.SaltSignaturePair.class, handle);

        assertFalse(data.isValid());
        assertArrayEquals(signature, data.signature());
        assertEquals(salt, data.salt());

        // test key data unwrapping
        WrappedSaltedSignature unwrapped = BukkitConverters.getWrappedSignatureConverter().getSpecific(data);
        assertNotNull(unwrapped);
        assertFalse(unwrapped.isSigned());
        assertEquals(loginSignature.getSalt(), unwrapped.getSalt());
        assertArrayEquals(loginSignature.getSignature(), unwrapped.getSignature());
        assertArrayEquals(loginSignature.getSaltBytes(), unwrapped.getSaltBytes());

        // test key data wrapping
        Object wrappedData = BukkitConverters.getWrappedSignatureConverter().getGeneric(loginSignature);
        Crypt.SaltSignaturePair wrapped = assertInstanceOf(Crypt.SaltSignaturePair.class, wrappedData);

        assertFalse(wrapped.isValid());
        assertEquals(loginSignature.getSalt(), wrapped.salt());
        assertArrayEquals(loginSignature.getSignature(), wrapped.signature());
        assertArrayEquals(loginSignature.getSaltBytes(), wrapped.saltAsBytes());
    }
}
