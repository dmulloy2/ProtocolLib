package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRotateHeadPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundRotateHeadPacket w = new WrappedClientboundRotateHeadPacket(3, (byte) 1);

        assertEquals(PacketType.Play.Server.ENTITY_HEAD_ROTATION, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals((byte) 1, w.getYHeadRot());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRotateHeadPacket w = new WrappedClientboundRotateHeadPacket();

        assertEquals(PacketType.Play.Server.ENTITY_HEAD_ROTATION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundRotateHeadPacket source = new WrappedClientboundRotateHeadPacket(3, (byte) 1);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundRotateHeadPacket wrapper = new WrappedClientboundRotateHeadPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals((byte) 1, wrapper.getYHeadRot());

        wrapper.setEntityId(9);
        wrapper.setYHeadRot((byte) -1);

        assertEquals(9, wrapper.getEntityId());
        assertEquals((byte) -1, wrapper.getYHeadRot());

        assertEquals(9, source.getEntityId());
        assertEquals((byte) -1, source.getYHeadRot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRotateHeadPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
