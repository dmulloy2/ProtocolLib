package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDebugBlockValuePacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        // No all-args constructor: update field has no ProtocolLib accessor
        assertEquals(PacketType.Play.Server.DEBUG_BLOCK_VALUE, new WrappedClientboundDebugBlockValuePacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDebugBlockValuePacket w = new WrappedClientboundDebugBlockValuePacket();
        assertEquals(PacketType.Play.Server.DEBUG_BLOCK_VALUE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.DEBUG_BLOCK_VALUE);
        WrappedClientboundDebugBlockValuePacket wrapper = new WrappedClientboundDebugBlockValuePacket(container);
        wrapper.setBlockPos(new BlockPosition(1, 2, 3));
        assertEquals(new BlockPosition(1, 2, 3), wrapper.getBlockPos());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDebugBlockValuePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
