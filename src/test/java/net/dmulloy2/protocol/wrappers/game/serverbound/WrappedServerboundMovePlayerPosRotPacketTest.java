package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundMovePlayerPosRotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundMovePlayerPosRotPacket w = new WrappedServerboundMovePlayerPosRotPacket(3.14, 100.0, -2.5, 0.75f, 0.5f, true, true);

        assertEquals(PacketType.Play.Client.POSITION_LOOK, w.getHandle().getType());

        assertEquals(3.14, w.getX(), 1e-9);
        assertEquals(100.0, w.getY(), 1e-9);
        assertEquals(-2.5, w.getZ(), 1e-9);
        assertEquals(0.75f, w.getYRot(), 1e-4f);
        assertEquals(0.5f, w.getXRot(), 1e-4f);
        assertTrue(w.isOnGround());
        assertTrue(w.isHorizontalCollision());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundMovePlayerPosRotPacket w = new WrappedServerboundMovePlayerPosRotPacket();

        assertEquals(PacketType.Play.Client.POSITION_LOOK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundMovePlayerPosRotPacket source = new WrappedServerboundMovePlayerPosRotPacket(3.14, 100.0, -2.5, 0.75f, 0.5f, true, true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMovePlayerPosRotPacket wrapper = new WrappedServerboundMovePlayerPosRotPacket(container);

        assertEquals(3.14, wrapper.getX(), 1e-9);
        assertEquals(100.0, wrapper.getY(), 1e-9);
        assertEquals(-2.5, wrapper.getZ(), 1e-9);
        assertEquals(0.75f, wrapper.getYRot(), 1e-4f);
        assertEquals(0.5f, wrapper.getXRot(), 1e-4f);
        assertTrue(wrapper.isOnGround());
        assertTrue(wrapper.isHorizontalCollision());

        wrapper.setX(2.71);
        wrapper.setY(-5.0);
        wrapper.setZ(0.0);
        wrapper.setYRot(10.5f);
        wrapper.setXRot(0.25f);
        wrapper.setOnGround(false);
        wrapper.setHorizontalCollision(false);

        assertEquals(2.71, wrapper.getX(), 1e-9);
        assertEquals(-5.0, wrapper.getY(), 1e-9);
        assertEquals(0.0, wrapper.getZ(), 1e-9);
        assertEquals(10.5f, wrapper.getYRot(), 1e-4f);
        assertEquals(0.25f, wrapper.getXRot(), 1e-4f);
        assertFalse(wrapper.isOnGround());
        assertFalse(wrapper.isHorizontalCollision());

        assertEquals(2.71, source.getX(), 1e-9);
        assertEquals(-5.0, source.getY(), 1e-9);
        assertEquals(0.0, source.getZ(), 1e-9);
        assertEquals(10.5f, source.getYRot(), 1e-4f);
        assertEquals(0.25f, source.getXRot(), 1e-4f);
        assertFalse(source.isOnGround());
        assertFalse(source.isHorizontalCollision());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMovePlayerPosRotPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
