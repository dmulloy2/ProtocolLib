package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveEntityRotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundMoveEntityRotPacket w = new WrappedClientboundMoveEntityRotPacket();
        w.setEntityId(33);
        w.setYaw((byte) 64);
        w.setPitch((byte) 0);
        w.setOnGround(true);

        assertEquals(PacketType.Play.Server.ENTITY_LOOK, w.getHandle().getType());

        ClientboundMoveEntityPacket.Rot p = (ClientboundMoveEntityPacket.Rot) w.getHandle().getHandle();

        assertTrue(p.isOnGround());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundMoveEntityPacket.Rot nmsPacket = new ClientboundMoveEntityPacket.Rot(
                10, (byte) 64, (byte) 0, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityRotPacket wrapper = new WrappedClientboundMoveEntityRotPacket(container);

        assertEquals(10, wrapper.getEntityId());
        assertEquals((byte) 64, wrapper.getYaw());
        assertFalse(wrapper.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundMoveEntityPacket.Rot nmsPacket = new ClientboundMoveEntityPacket.Rot(
                10, (byte) 64, (byte) 0, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveEntityRotPacket wrapper = new WrappedClientboundMoveEntityRotPacket(container);

        wrapper.setOnGround(true);

        assertEquals(10, wrapper.getEntityId());
        assertEquals((byte) 64, wrapper.getYaw());
        assertTrue(wrapper.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveEntityRotPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
