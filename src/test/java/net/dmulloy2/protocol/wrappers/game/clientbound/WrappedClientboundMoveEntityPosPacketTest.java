package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveEntityPosPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundMoveEntityPosPacket w = new WrappedClientboundMoveEntityPosPacket();
        w.setEntityId(12);
        w.setDx((short) 128);
        w.setDy((short) 0);
        w.setDz((short) -256);
        w.setOnGround(true);

        assertEquals(PacketType.Play.Server.REL_ENTITY_MOVE, w.getHandle().getType());

        ClientboundMoveEntityPacket.Pos p = (ClientboundMoveEntityPacket.Pos) w.getHandle().getHandle();

        assertEquals((short) 128, p.getXa());
        assertEquals((short) 0, p.getYa());
        assertEquals((short) -256, p.getZa());
        assertTrue(p.isOnGround());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundMoveEntityPacket.Pos nmsPacket = new ClientboundMoveEntityPacket.Pos(
                7, (short) 100, (short) 50, (short) -100, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityPosPacket wrapper = new WrappedClientboundMoveEntityPosPacket(container);

        assertEquals(7, wrapper.getEntityId());
        assertEquals((short) 100, wrapper.getDx());
        assertEquals((short) 50, wrapper.getDy());
        assertEquals((short) -100, wrapper.getDz());
        assertFalse(wrapper.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundMoveEntityPacket.Pos nmsPacket = new ClientboundMoveEntityPacket.Pos(
                7, (short) 100, (short) 50, (short) -100, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityPosPacket wrapper = new WrappedClientboundMoveEntityPosPacket(container);

        wrapper.setDx((short) 4096);

        assertEquals(7, wrapper.getEntityId());
        assertEquals((short) 4096, wrapper.getDx());
        assertEquals((short) 50, wrapper.getDy());
        assertEquals((short) -100, wrapper.getDz());
        assertFalse(wrapper.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveEntityPosPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
