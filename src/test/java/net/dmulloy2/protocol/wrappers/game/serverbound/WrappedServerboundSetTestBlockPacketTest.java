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
        // No all-args constructor due to NMS-specific enum type
        assertEquals(PacketType.Play.Client.SET_TEST_BLOCK, new WrappedServerboundSetTestBlockPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetTestBlockPacket w = new WrappedServerboundSetTestBlockPacket();
        assertEquals(PacketType.Play.Client.SET_TEST_BLOCK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.SET_TEST_BLOCK);
        WrappedServerboundSetTestBlockPacket wrapper = new WrappedServerboundSetTestBlockPacket(container);
        wrapper.setPosition(new BlockPosition(1, 2, 3));
        wrapper.setMessage("test message");
        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPosition());
        assertEquals("test message", wrapper.getMessage());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetTestBlockPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
