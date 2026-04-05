package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveEntityPosRotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundMoveEntityPosRotPacket w = new WrappedClientboundMoveEntityPosRotPacket(3, (short) 7, (short) -1, (short) 12, (byte) 1, (byte) 7, true);

        assertEquals(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals((short) 7, w.getDx());
        assertEquals((short) -1, w.getDy());
        assertEquals((short) 12, w.getDz());
        assertEquals((byte) 1, w.getYaw());
        assertEquals((byte) 7, w.getPitch());
        assertTrue(w.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMoveEntityPosRotPacket w = new WrappedClientboundMoveEntityPosRotPacket();

        assertEquals(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundMoveEntityPosRotPacket source = new WrappedClientboundMoveEntityPosRotPacket(3, (short) 7, (short) -1, (short) 12, (byte) 1, (byte) 7, true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityPosRotPacket wrapper = new WrappedClientboundMoveEntityPosRotPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals((short) 7, wrapper.getDx());
        assertEquals((short) -1, wrapper.getDy());
        assertEquals((short) 12, wrapper.getDz());
        assertEquals((byte) 1, wrapper.getYaw());
        assertEquals((byte) 7, wrapper.getPitch());
        assertTrue(wrapper.isOnGround());

        wrapper.setEntityId(9);
        wrapper.setDx((short) -1);
        wrapper.setDy((short) 0);
        wrapper.setDz((short) 5);
        wrapper.setYaw((byte) -1);
        wrapper.setPitch((byte) 0);
        wrapper.setOnGround(false);

        assertEquals(9, wrapper.getEntityId());
        assertEquals((short) -1, wrapper.getDx());
        assertEquals((short) 0, wrapper.getDy());
        assertEquals((short) 5, wrapper.getDz());
        assertEquals((byte) -1, wrapper.getYaw());
        assertEquals((byte) 0, wrapper.getPitch());
        assertFalse(wrapper.isOnGround());

        assertEquals(9, source.getEntityId());
        assertEquals((short) -1, source.getDx());
        assertEquals((short) 0, source.getDy());
        assertEquals((short) 5, source.getDz());
        assertEquals((byte) -1, source.getYaw());
        assertEquals((byte) 0, source.getPitch());
        assertFalse(source.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveEntityPosRotPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
