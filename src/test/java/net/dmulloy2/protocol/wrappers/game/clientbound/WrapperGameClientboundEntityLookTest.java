package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundEntityLookTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundEntityLook w = new WrapperGameClientboundEntityLook();
        byte yaw = WrapperGameClientboundSpawnEntity.angleToByte(90.0f);
        byte pitch = WrapperGameClientboundSpawnEntity.angleToByte(0.0f);
        w.setEntityId(33);
        w.setYaw(yaw);
        w.setPitch(pitch);
        w.setOnGround(true);
        assertEquals(33, w.getEntityId());
        assertEquals(yaw, w.getYaw());
        assertEquals(pitch, w.getPitch());
        assertTrue(w.isOnGround());
        assertEquals(PacketType.Play.Server.ENTITY_LOOK, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 10);
        raw.getBytes().write(0, (byte) 64);
        raw.getBytes().write(1, (byte) 0);
        raw.getBooleans().write(0, false);

        WrapperGameClientboundEntityLook w = new WrapperGameClientboundEntityLook(raw);
        assertEquals(10, w.getEntityId());
        assertEquals((byte) 64, w.getYaw());
        assertFalse(w.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundEntityLook w = new WrapperGameClientboundEntityLook();
        w.setOnGround(false);

        new WrapperGameClientboundEntityLook(w.getHandle()).setOnGround(true);

        assertTrue(w.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundEntityLook(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
