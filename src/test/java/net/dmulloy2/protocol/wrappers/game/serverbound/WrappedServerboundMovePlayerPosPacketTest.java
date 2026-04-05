package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundMovePlayerPosPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundMovePlayerPosPacket w = new WrappedServerboundMovePlayerPosPacket(3.14, 100.0, -2.5, true, false);

        assertEquals(PacketType.Play.Client.POSITION, w.getHandle().getType());

        assertEquals(3.14, w.getX(), 1e-9);
        assertEquals(100.0, w.getY(), 1e-9);
        assertEquals(-2.5, w.getZ(), 1e-9);
        assertTrue(w.isOnGround());
        assertFalse(w.isHorizontalCollision());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundMovePlayerPosPacket w = new WrappedServerboundMovePlayerPosPacket();

        assertEquals(PacketType.Play.Client.POSITION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundMovePlayerPosPacket source = new WrappedServerboundMovePlayerPosPacket(3.14, 100.0, -2.5, true, false);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMovePlayerPosPacket wrapper = new WrappedServerboundMovePlayerPosPacket(container);

        assertEquals(3.14, wrapper.getX(), 1e-9);
        assertEquals(100.0, wrapper.getY(), 1e-9);
        assertEquals(-2.5, wrapper.getZ(), 1e-9);
        assertTrue(wrapper.isOnGround());
        assertFalse(wrapper.isHorizontalCollision());

        wrapper.setX(2.71);
        wrapper.setY(-5.0);
        wrapper.setZ(0.0);
        wrapper.setOnGround(false);
        wrapper.setHorizontalCollision(true);

        assertEquals(2.71, wrapper.getX(), 1e-9);
        assertEquals(-5.0, wrapper.getY(), 1e-9);
        assertEquals(0.0, wrapper.getZ(), 1e-9);
        assertFalse(wrapper.isOnGround());
        assertTrue(wrapper.isHorizontalCollision());

        assertEquals(2.71, source.getX(), 1e-9);
        assertEquals(-5.0, source.getY(), 1e-9);
        assertEquals(0.0, source.getZ(), 1e-9);
        assertFalse(source.isOnGround());
        assertTrue(source.isHorizontalCollision());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMovePlayerPosPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
