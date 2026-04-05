package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveEntityRotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundMoveEntityRotPacket w = new WrappedClientboundMoveEntityRotPacket(3, (byte) 1, (byte) 7, true);

        assertEquals(PacketType.Play.Server.ENTITY_LOOK, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals((byte) 1, w.getYaw());
        assertEquals((byte) 7, w.getPitch());
        assertTrue(w.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMoveEntityRotPacket w = new WrappedClientboundMoveEntityRotPacket();

        assertEquals(PacketType.Play.Server.ENTITY_LOOK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundMoveEntityRotPacket source = new WrappedClientboundMoveEntityRotPacket(3, (byte) 1, (byte) 7, true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityRotPacket wrapper = new WrappedClientboundMoveEntityRotPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals((byte) 1, wrapper.getYaw());
        assertEquals((byte) 7, wrapper.getPitch());
        assertTrue(wrapper.isOnGround());

        wrapper.setEntityId(9);
        wrapper.setYaw((byte) -1);
        wrapper.setPitch((byte) 0);
        wrapper.setOnGround(false);

        assertEquals(9, wrapper.getEntityId());
        assertEquals((byte) -1, wrapper.getYaw());
        assertEquals((byte) 0, wrapper.getPitch());
        assertFalse(wrapper.isOnGround());

        assertEquals(9, source.getEntityId());
        assertEquals((byte) -1, source.getYaw());
        assertEquals((byte) 0, source.getPitch());
        assertFalse(source.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveEntityRotPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
