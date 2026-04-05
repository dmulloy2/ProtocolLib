package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundAnimatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundAnimatePacket w = new WrappedClientboundAnimatePacket(3, 7);

        assertEquals(PacketType.Play.Server.ANIMATION, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(7, w.getAnimationId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundAnimatePacket w = new WrappedClientboundAnimatePacket();

        assertEquals(PacketType.Play.Server.ANIMATION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundAnimatePacket source = new WrappedClientboundAnimatePacket(3, 7);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundAnimatePacket wrapper = new WrappedClientboundAnimatePacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(7, wrapper.getAnimationId());

        wrapper.setEntityId(9);
        wrapper.setAnimationId(-5);

        assertEquals(9, wrapper.getEntityId());
        assertEquals(-5, wrapper.getAnimationId());

        assertEquals(9, source.getEntityId());
        assertEquals(-5, source.getAnimationId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundAnimatePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
