package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetTestBlockPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetTestBlockPacket w = new WrappedServerboundSetTestBlockPacket(
                new BlockPosition(1, 2, 3), WrappedServerboundSetTestBlockPacket.TestBlockMode.LOG, "test message");

        assertEquals(PacketType.Play.Client.SET_TEST_BLOCK, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getPosition());
        assertEquals(WrappedServerboundSetTestBlockPacket.TestBlockMode.LOG, w.getMode());
        assertEquals("test message", w.getMessage());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetTestBlockPacket w = new WrappedServerboundSetTestBlockPacket();
        assertEquals(PacketType.Play.Client.SET_TEST_BLOCK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetTestBlockPacket source = new WrappedServerboundSetTestBlockPacket(
                new BlockPosition(1, 2, 3), WrappedServerboundSetTestBlockPacket.TestBlockMode.LOG, "test message");
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetTestBlockPacket wrapper = new WrappedServerboundSetTestBlockPacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPosition());
        assertEquals(WrappedServerboundSetTestBlockPacket.TestBlockMode.LOG, wrapper.getMode());
        assertEquals("test message", wrapper.getMessage());

        wrapper.setPosition(new BlockPosition(10, 20, 30));
        wrapper.setMode(WrappedServerboundSetTestBlockPacket.TestBlockMode.ACCEPT);
        wrapper.setMessage("modified");

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPosition());
        assertEquals(WrappedServerboundSetTestBlockPacket.TestBlockMode.ACCEPT, wrapper.getMode());
        assertEquals("modified", wrapper.getMessage());

        assertEquals(new BlockPosition(10, 20, 30), source.getPosition());
        assertEquals(WrappedServerboundSetTestBlockPacket.TestBlockMode.ACCEPT, source.getMode());
        assertEquals("modified", source.getMessage());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetTestBlockPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
