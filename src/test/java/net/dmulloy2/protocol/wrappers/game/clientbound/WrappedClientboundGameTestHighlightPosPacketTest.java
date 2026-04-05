package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundGameTestHighlightPosPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundGameTestHighlightPosPacket w = new WrappedClientboundGameTestHighlightPosPacket(
                new BlockPosition(1, 2, 3), new BlockPosition(4, 5, 6));
        assertEquals(PacketType.Play.Server.GAME_TEST_HIGHLIGHT_POS, w.getHandle().getType());
        assertEquals(new BlockPosition(1, 2, 3), w.getAbsolutePos());
        assertEquals(new BlockPosition(4, 5, 6), w.getRelativePos());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundGameTestHighlightPosPacket w = new WrappedClientboundGameTestHighlightPosPacket();
        assertEquals(PacketType.Play.Server.GAME_TEST_HIGHLIGHT_POS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundGameTestHighlightPosPacket src = new WrappedClientboundGameTestHighlightPosPacket(
                new BlockPosition(1, 2, 3), new BlockPosition(4, 5, 6));
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedClientboundGameTestHighlightPosPacket wrapper = new WrappedClientboundGameTestHighlightPosPacket(container);
        assertEquals(new BlockPosition(1, 2, 3), wrapper.getAbsolutePos());
        assertEquals(new BlockPosition(4, 5, 6), wrapper.getRelativePos());
        wrapper.setAbsolutePos(new BlockPosition(10, 20, 30));
        wrapper.setRelativePos(new BlockPosition(7, 8, 9));
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getAbsolutePos());
        assertEquals(new BlockPosition(7, 8, 9), wrapper.getRelativePos());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundGameTestHighlightPosPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
