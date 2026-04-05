package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundEntityEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundEntityEventPacket w = new WrappedClientboundEntityEventPacket(3, (byte) 1);

        assertEquals(PacketType.Play.Server.ENTITY_STATUS, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals((byte) 1, w.getStatus());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundEntityEventPacket w = new WrappedClientboundEntityEventPacket();

        assertEquals(PacketType.Play.Server.ENTITY_STATUS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundEntityEventPacket source = new WrappedClientboundEntityEventPacket(3, (byte) 1);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundEntityEventPacket wrapper = new WrappedClientboundEntityEventPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals((byte) 1, wrapper.getStatus());

        wrapper.setEntityId(9);
        wrapper.setStatus((byte) -1);

        assertEquals(9, wrapper.getEntityId());
        assertEquals((byte) -1, wrapper.getStatus());

        assertEquals(9, source.getEntityId());
        assertEquals((byte) -1, source.getStatus());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundEntityEventPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
