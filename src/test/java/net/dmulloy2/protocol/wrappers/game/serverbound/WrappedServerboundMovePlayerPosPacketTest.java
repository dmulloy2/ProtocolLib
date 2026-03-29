package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
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
        WrappedServerboundMovePlayerPosPacket w = new WrappedServerboundMovePlayerPosPacket(5.0, 63.5, 10.0, true, false);

        assertEquals(PacketType.Play.Client.POSITION, w.getHandle().getType());

        ServerboundMovePlayerPacket.Pos p = (ServerboundMovePlayerPacket.Pos) w.getHandle().getHandle();

        assertEquals(5.0, p.getX(0.0), 1e-9);
        assertEquals(63.5, p.getY(0.0), 1e-9);
        assertEquals(10.0, p.getZ(0.0), 1e-9);
        assertTrue(p.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundMovePlayerPosPacket w = new WrappedServerboundMovePlayerPosPacket();

        assertEquals(PacketType.Play.Client.POSITION, w.getHandle().getType());

        ServerboundMovePlayerPacket.Pos p = (ServerboundMovePlayerPacket.Pos) w.getHandle().getHandle();

        assertEquals(0.0, p.getX(0.0), 1e-9);
        assertEquals(0.0, p.getY(0.0), 1e-9);
        assertEquals(0.0, p.getZ(0.0), 1e-9);
        assertFalse(p.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundMovePlayerPacket.Pos nmsPacket = new ServerboundMovePlayerPacket.Pos(5.0, 63.5, 10.0, true, false);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMovePlayerPosPacket wrapper = new WrappedServerboundMovePlayerPosPacket(container);

        assertEquals(5.0, wrapper.getX(), 1e-9);
        assertEquals(63.5, wrapper.getY(), 1e-9);
        assertEquals(10.0, wrapper.getZ(), 1e-9);
        assertTrue(wrapper.isOnGround());
        assertFalse(wrapper.isHorizontalCollision());

        wrapper.setX(100.0);
        wrapper.setY(70.0);
        wrapper.setZ(200.0);
        wrapper.setOnGround(false);
        wrapper.setHorizontalCollision(true);

        assertEquals(100.0, nmsPacket.getX(0.0), 1e-9);
        assertEquals(70.0, nmsPacket.getY(0.0), 1e-9);
        assertEquals(200.0, nmsPacket.getZ(0.0), 1e-9);
        assertFalse(nmsPacket.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMovePlayerPosPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
