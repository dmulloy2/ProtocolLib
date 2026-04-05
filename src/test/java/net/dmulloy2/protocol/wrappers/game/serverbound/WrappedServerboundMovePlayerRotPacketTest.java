package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundMovePlayerRotPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundMovePlayerRotPacket w = new WrappedServerboundMovePlayerRotPacket(45.0f, 30.0f, true, false);
        assertEquals(PacketType.Play.Client.LOOK, w.getHandle().getType());
        assertEquals(45.0f, w.getYRot(), 1e-4f);
        assertEquals(30.0f, w.getXRot(), 1e-4f);
        assertTrue(w.isOnGround());
        assertFalse(w.isHorizontalCollision());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundMovePlayerRotPacket w = new WrappedServerboundMovePlayerRotPacket();
        assertEquals(PacketType.Play.Client.LOOK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundMovePlayerRotPacket src = new WrappedServerboundMovePlayerRotPacket(45.0f, 30.0f, true, false);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedServerboundMovePlayerRotPacket wrapper = new WrappedServerboundMovePlayerRotPacket(container);
        assertEquals(45.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(30.0f, wrapper.getXRot(), 1e-4f);
        assertTrue(wrapper.isOnGround());
        assertFalse(wrapper.isHorizontalCollision());
        wrapper.setYRot(90.0f);
        wrapper.setXRot(-15.0f);
        wrapper.setOnGround(false);
        wrapper.setHorizontalCollision(true);
        assertEquals(90.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(-15.0f, wrapper.getXRot(), 1e-4f);
        assertFalse(wrapper.isOnGround());
        assertTrue(wrapper.isHorizontalCollision());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMovePlayerRotPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
