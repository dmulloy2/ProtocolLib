package com.comphenix.protocol.utility;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.comphenix.protocol.BukkitInitialization;

public class MinecraftMethodsTest {

    @BeforeAll
    public static void initializeReflection() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testSendPacketMethods() {
        assertNotNull(MinecraftMethods.getPlayerConnectionSendMethod());
        assertNotNull(MinecraftMethods.getPlayerConnectionDisconnectMethod());

        assertNotNull(MinecraftMethods.getNetworkManagerSendMethod());
        assertNotNull(MinecraftMethods.getNetworkManagerReadPacketMethod());
        assertNotNull(MinecraftMethods.getNetworkManagerDisconnectMethod());
    }

    private void setNull(final String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = MinecraftMethods.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void initializePacket() throws NoSuchFieldException, IllegalAccessException {
        this.setNull("packetReadByteBuf");
        this.setNull("packetWriteByteBuf");

		// the write and read method got replaced by the StreamCodec class and each
		// packet now has it's own unique codec instance
        if (!MinecraftVersion.v1_20_5.atOrAbove()) {
            assertNotNull(MinecraftMethods.getPacketWriteByteBufMethod());
            // TODO it's now a constructor
            // assertNotNull(MinecraftMethods.getPacketReadByteBufMethod());
        }
    }
}
