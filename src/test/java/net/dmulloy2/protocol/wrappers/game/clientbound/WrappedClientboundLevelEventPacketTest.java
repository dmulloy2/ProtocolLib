package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLevelEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundLevelEventPacket w = new WrappedClientboundLevelEventPacket();
        w.setType(1004);
        w.setPos(new BlockPosition(5, 64, -3));
        w.setData(0);
        w.setBroadcastToAll(false);

        assertEquals(PacketType.Play.Server.WORLD_EVENT, w.getHandle().getType());

        ClientboundLevelEventPacket p = (ClientboundLevelEventPacket) w.getHandle().getHandle();

        assertEquals(1004, p.getType());
        assertEquals(0, p.getData());
        assertFalse(p.isGlobalEvent());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundLevelEventPacket nmsPacket = new ClientboundLevelEventPacket(
                2001, new net.minecraft.core.BlockPos(1, 2, 3), 10, true
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLevelEventPacket wrapper = new WrappedClientboundLevelEventPacket(container);

        assertEquals(2001, wrapper.getType());
        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertEquals(10, wrapper.getData());
        assertTrue(wrapper.isBroadcastToAll());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundLevelEventPacket nmsPacket = new ClientboundLevelEventPacket(
                2001, new net.minecraft.core.BlockPos(1, 2, 3), 10, true
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLevelEventPacket wrapper = new WrappedClientboundLevelEventPacket(container);

        wrapper.setData(42);

        assertEquals(2001, wrapper.getType());
        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertEquals(42, wrapper.getData());
        assertTrue(wrapper.isBroadcastToAll());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLevelEventPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
