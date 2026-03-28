package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundEntityHeadRotationTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundEntityHeadRotation w = new WrapperGameClientboundEntityHeadRotation();
        w.setEntityId(8);
        // 90° → byte 64 (64/256 * 360 ≈ 90°)
        w.setYHeadRot(WrapperGameClientboundSpawnEntity.angleToByte(90.0f));

        assertEquals(8,        w.getEntityId());
        assertEquals((byte) 64, w.getYHeadRot());
        assertEquals(PacketType.Play.Server.ENTITY_HEAD_ROTATION, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 20);
        raw.getBytes().write(0, (byte) 32);

        WrapperGameClientboundEntityHeadRotation w = new WrapperGameClientboundEntityHeadRotation(raw);
        assertEquals(20,        w.getEntityId());
        assertEquals((byte) 32, w.getYHeadRot());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundEntityHeadRotation w = new WrapperGameClientboundEntityHeadRotation();
        w.setEntityId(1);
        w.setYHeadRot((byte) 0);

        new WrapperGameClientboundEntityHeadRotation(w.getHandle()).setEntityId(99);

        assertEquals(99, w.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundEntityHeadRotation(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
