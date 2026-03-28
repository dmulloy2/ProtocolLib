package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundRelEntityMoveLookTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundRelEntityMoveLook w = new WrapperGameClientboundRelEntityMoveLook();
        w.setEntityId(20);
        w.setDx((short) 64);
        w.setDy((short) 0);
        w.setDz((short) -64);
        byte yaw = WrapperGameClientboundSpawnEntity.angleToByte(45.0f);
        byte pitch = WrapperGameClientboundSpawnEntity.angleToByte(0.0f);
        w.setYaw(yaw);
        w.setPitch(pitch);
        w.setOnGround(false);
        assertEquals(20, w.getEntityId());
        assertEquals((short) 64, w.getDx());
        assertEquals(yaw, w.getYaw());
        assertFalse(w.isOnGround());
        assertEquals(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 5);
        raw.getShorts().write(0, (short) 200);
        raw.getShorts().write(1, (short) 100);
        raw.getShorts().write(2, (short) 50);
        raw.getBytes().write(0, (byte) 32);
        raw.getBytes().write(1, (byte) 8);
        raw.getBooleans().write(0, true);

        WrapperGameClientboundRelEntityMoveLook w = new WrapperGameClientboundRelEntityMoveLook(raw);
        assertEquals(5, w.getEntityId());
        assertEquals((short) 200, w.getDx());
        assertEquals((byte) 32, w.getYaw());
        assertTrue(w.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundRelEntityMoveLook w = new WrapperGameClientboundRelEntityMoveLook();
        w.setOnGround(false);

        new WrapperGameClientboundRelEntityMoveLook(w.getHandle()).setOnGround(true);

        assertTrue(w.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundRelEntityMoveLook(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
