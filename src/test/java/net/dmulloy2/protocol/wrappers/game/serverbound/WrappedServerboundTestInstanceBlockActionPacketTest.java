package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundTestInstanceBlockActionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION, new WrappedServerboundTestInstanceBlockActionPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundTestInstanceBlockActionPacket w = new WrappedServerboundTestInstanceBlockActionPacket();
        assertEquals(PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION);
        WrappedServerboundTestInstanceBlockActionPacket wrapper = new WrappedServerboundTestInstanceBlockActionPacket(container);
        assertEquals(PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundTestInstanceBlockActionPacket(new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
