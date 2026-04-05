package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLevelEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundLevelEventPacket w = new WrappedClientboundLevelEventPacket(3, new BlockPosition(4, 5, 6), 5, true);

        assertEquals(PacketType.Play.Server.WORLD_EVENT, w.getHandle().getType());

        assertEquals(3, w.getType());
        assertEquals(new BlockPosition(4, 5, 6), w.getPos());
        assertEquals(5, w.getData());
        assertTrue(w.isBroadcastToAll());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLevelEventPacket w = new WrappedClientboundLevelEventPacket();

        assertEquals(PacketType.Play.Server.WORLD_EVENT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundLevelEventPacket source = new WrappedClientboundLevelEventPacket(3, new BlockPosition(4, 5, 6), 5, true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLevelEventPacket wrapper = new WrappedClientboundLevelEventPacket(container);

        assertEquals(3, wrapper.getType());
        assertEquals(new BlockPosition(4, 5, 6), wrapper.getPos());
        assertEquals(5, wrapper.getData());
        assertTrue(wrapper.isBroadcastToAll());

        wrapper.setType(9);
        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setData(0);
        wrapper.setBroadcastToAll(false);

        assertEquals(9, wrapper.getType());
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertEquals(0, wrapper.getData());
        assertFalse(wrapper.isBroadcastToAll());

        assertEquals(9, source.getType());
        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertEquals(0, source.getData());
        assertFalse(source.isBroadcastToAll());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLevelEventPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
