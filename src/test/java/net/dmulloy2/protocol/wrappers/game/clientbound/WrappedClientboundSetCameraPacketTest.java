package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetCameraPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetCameraPacket w = new WrappedClientboundSetCameraPacket(3);

        assertEquals(PacketType.Play.Server.CAMERA, w.getHandle().getType());

        assertEquals(3, w.getCameraEntityId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetCameraPacket w = new WrappedClientboundSetCameraPacket();

        assertEquals(PacketType.Play.Server.CAMERA, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetCameraPacket source = new WrappedClientboundSetCameraPacket(3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetCameraPacket wrapper = new WrappedClientboundSetCameraPacket(container);

        assertEquals(3, wrapper.getCameraEntityId());

        wrapper.setCameraEntityId(9);

        assertEquals(9, wrapper.getCameraEntityId());

        assertEquals(9, source.getCameraEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetCameraPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
