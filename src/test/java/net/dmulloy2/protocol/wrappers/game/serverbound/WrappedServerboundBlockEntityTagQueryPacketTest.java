package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundBlockEntityTagQueryPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundBlockEntityTagQueryPacket w = new WrappedServerboundBlockEntityTagQueryPacket(3, new BlockPosition(4, 5, 6));

        assertEquals(PacketType.Play.Client.TILE_NBT_QUERY, w.getHandle().getType());

        assertEquals(3, w.getTransactionId());
        assertEquals(new BlockPosition(4, 5, 6), w.getPos());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundBlockEntityTagQueryPacket w = new WrappedServerboundBlockEntityTagQueryPacket();

        assertEquals(PacketType.Play.Client.TILE_NBT_QUERY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundBlockEntityTagQueryPacket source = new WrappedServerboundBlockEntityTagQueryPacket(3, new BlockPosition(4, 5, 6));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundBlockEntityTagQueryPacket wrapper = new WrappedServerboundBlockEntityTagQueryPacket(container);

        assertEquals(3, wrapper.getTransactionId());
        assertEquals(new BlockPosition(4, 5, 6), wrapper.getPos());

        wrapper.setTransactionId(9);
        wrapper.setPos(new BlockPosition(10, 20, 30));

        assertEquals(9, wrapper.getTransactionId());
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());

        assertEquals(9, source.getTransactionId());
        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundBlockEntityTagQueryPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
