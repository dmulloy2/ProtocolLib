package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderCenterPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetBorderCenterPacket w = new WrappedClientboundSetBorderCenterPacket(3.14, 100.0);

        assertEquals(PacketType.Play.Server.SET_BORDER_CENTER, w.getHandle().getType());

        assertEquals(3.14, w.getX(), 1e-9);
        assertEquals(100.0, w.getZ(), 1e-9);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetBorderCenterPacket w = new WrappedClientboundSetBorderCenterPacket();

        assertEquals(PacketType.Play.Server.SET_BORDER_CENTER, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetBorderCenterPacket source = new WrappedClientboundSetBorderCenterPacket(3.14, 100.0);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetBorderCenterPacket wrapper = new WrappedClientboundSetBorderCenterPacket(container);

        assertEquals(3.14, wrapper.getX(), 1e-9);
        assertEquals(100.0, wrapper.getZ(), 1e-9);

        wrapper.setX(2.71);
        wrapper.setZ(-5.0);

        assertEquals(2.71, wrapper.getX(), 1e-9);
        assertEquals(-5.0, wrapper.getZ(), 1e-9);

        assertEquals(2.71, source.getX(), 1e-9);
        assertEquals(-5.0, source.getZ(), 1e-9);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderCenterPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
