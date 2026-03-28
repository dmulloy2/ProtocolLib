package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveEntityPosRotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundMoveEntityPosRotPacket w = new WrappedClientboundMoveEntityPosRotPacket();
        w.setEntityId(20);
        w.setDx((short) 64);
        w.setDy((short) 0);
        w.setDz((short) -64);
        w.setYaw((byte) 32);
        w.setPitch((byte) 8);
        w.setOnGround(false);

        assertEquals(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, w.getHandle().getType());

        ClientboundMoveEntityPacket.PosRot p = (ClientboundMoveEntityPacket.PosRot) w.getHandle().getHandle();

        assertEquals((short) 64, p.getXa());
        assertEquals((byte) 32, (byte) Math.round(p.getYRot() / 360.0f * 256.0f));
        assertFalse(p.isOnGround());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundMoveEntityPacket.PosRot nmsPacket = new ClientboundMoveEntityPacket.PosRot(
                5, (short) 200, (short) 100, (short) 50, (byte) 32, (byte) 8, true
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityPosRotPacket wrapper = new WrappedClientboundMoveEntityPosRotPacket(container);

        assertEquals(5, wrapper.getEntityId());
        assertEquals((short) 200, wrapper.getDx());
        assertEquals((byte) 32, wrapper.getYaw());
        assertTrue(wrapper.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundMoveEntityPacket.PosRot nmsPacket = new ClientboundMoveEntityPacket.PosRot(
                5, (short) 200, (short) 100, (short) 50, (byte) 32, (byte) 8, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityPosRotPacket wrapper = new WrappedClientboundMoveEntityPosRotPacket(container);

        wrapper.setOnGround(true);

        assertEquals(5, wrapper.getEntityId());
        assertEquals((short) 200, wrapper.getDx());
        assertEquals((byte) 32, wrapper.getYaw());
        assertTrue(wrapper.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveEntityPosRotPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
