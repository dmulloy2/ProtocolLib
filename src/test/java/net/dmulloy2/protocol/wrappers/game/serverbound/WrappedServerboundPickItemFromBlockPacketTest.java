package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPickItemFromBlockPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundPickItemFromBlockPacket w = new WrappedServerboundPickItemFromBlockPacket(
                new BlockPosition(1, 2, 3), true);
        assertEquals(PacketType.Play.Client.PICK_ITEM_FROM_BLOCK, w.getHandle().getType());
        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertTrue(w.isIncludeData());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPickItemFromBlockPacket w = new WrappedServerboundPickItemFromBlockPacket();
        assertEquals(PacketType.Play.Client.PICK_ITEM_FROM_BLOCK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPickItemFromBlockPacket src = new WrappedServerboundPickItemFromBlockPacket(
                new BlockPosition(1, 2, 3), true);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedServerboundPickItemFromBlockPacket wrapper = new WrappedServerboundPickItemFromBlockPacket(container);
        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertTrue(wrapper.isIncludeData());
        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setIncludeData(false);
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertFalse(wrapper.isIncludeData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPickItemFromBlockPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
