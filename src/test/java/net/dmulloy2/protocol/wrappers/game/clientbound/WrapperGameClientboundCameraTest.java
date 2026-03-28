package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundCameraTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundCamera w = new WrapperGameClientboundCamera();
        w.setCameraEntityId(100);
        assertEquals(100, w.getCameraEntityId());
        assertEquals(PacketType.Play.Server.CAMERA, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.CAMERA);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 42);

        WrapperGameClientboundCamera w = new WrapperGameClientboundCamera(raw);
        assertEquals(42, w.getCameraEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundCamera w = new WrapperGameClientboundCamera();
        w.setCameraEntityId(1);

        new WrapperGameClientboundCamera(w.getHandle()).setCameraEntityId(5);

        assertEquals(5, w.getCameraEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundCamera(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
