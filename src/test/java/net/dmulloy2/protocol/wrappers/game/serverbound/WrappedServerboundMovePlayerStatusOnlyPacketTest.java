package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundMovePlayerStatusOnlyPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundMovePlayerStatusOnlyPacket w = new WrappedServerboundMovePlayerStatusOnlyPacket(true, false);
        assertEquals(PacketType.Play.Client.GROUND, w.getHandle().getType());
        assertTrue(w.isOnGround());
        assertFalse(w.isHorizontalCollision());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundMovePlayerStatusOnlyPacket w = new WrappedServerboundMovePlayerStatusOnlyPacket();
        assertEquals(PacketType.Play.Client.GROUND, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundMovePlayerStatusOnlyPacket src = new WrappedServerboundMovePlayerStatusOnlyPacket(true, false);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedServerboundMovePlayerStatusOnlyPacket wrapper = new WrappedServerboundMovePlayerStatusOnlyPacket(container);
        assertTrue(wrapper.isOnGround());
        assertFalse(wrapper.isHorizontalCollision());
        wrapper.setOnGround(false);
        wrapper.setHorizontalCollision(true);
        assertFalse(wrapper.isOnGround());
        assertTrue(wrapper.isHorizontalCollision());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMovePlayerStatusOnlyPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
