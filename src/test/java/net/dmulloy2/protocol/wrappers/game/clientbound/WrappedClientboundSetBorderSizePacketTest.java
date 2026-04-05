package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderSizePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetBorderSizePacket w = new WrappedClientboundSetBorderSizePacket(3.14);

        assertEquals(PacketType.Play.Server.SET_BORDER_SIZE, w.getHandle().getType());

        assertEquals(3.14, w.getDiameter(), 1e-9);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetBorderSizePacket w = new WrappedClientboundSetBorderSizePacket();

        assertEquals(PacketType.Play.Server.SET_BORDER_SIZE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetBorderSizePacket source = new WrappedClientboundSetBorderSizePacket(3.14);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetBorderSizePacket wrapper = new WrappedClientboundSetBorderSizePacket(container);

        assertEquals(3.14, wrapper.getDiameter(), 1e-9);

        wrapper.setDiameter(2.71);

        assertEquals(2.71, wrapper.getDiameter(), 1e-9);

        assertEquals(2.71, source.getDiameter(), 1e-9);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderSizePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
