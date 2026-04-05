package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveEntityPosPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundMoveEntityPosPacket w = new WrappedClientboundMoveEntityPosPacket(3, (short) 7, (short) -1, (short) 12, false);

        assertEquals(PacketType.Play.Server.REL_ENTITY_MOVE, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals((short) 7, w.getDx());
        assertEquals((short) -1, w.getDy());
        assertEquals((short) 12, w.getDz());
        assertFalse(w.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMoveEntityPosPacket w = new WrappedClientboundMoveEntityPosPacket();

        assertEquals(PacketType.Play.Server.REL_ENTITY_MOVE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundMoveEntityPosPacket source = new WrappedClientboundMoveEntityPosPacket(3, (short) 7, (short) -1, (short) 12, false);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityPosPacket wrapper = new WrappedClientboundMoveEntityPosPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals((short) 7, wrapper.getDx());
        assertEquals((short) -1, wrapper.getDy());
        assertEquals((short) 12, wrapper.getDz());
        assertFalse(wrapper.isOnGround());

        wrapper.setEntityId(9);
        wrapper.setDx((short) -1);
        wrapper.setDy((short) 0);
        wrapper.setDz((short) 5);
        wrapper.setOnGround(true);

        assertEquals(9, wrapper.getEntityId());
        assertEquals((short) -1, wrapper.getDx());
        assertEquals((short) 0, wrapper.getDy());
        assertEquals((short) 5, wrapper.getDz());
        assertTrue(wrapper.isOnGround());

        assertEquals(9, source.getEntityId());
        assertEquals((short) -1, source.getDx());
        assertEquals((short) 0, source.getDy());
        assertEquals((short) 5, source.getDz());
        assertTrue(source.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveEntityPosPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
