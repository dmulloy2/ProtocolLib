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
    void testCreate() {
        WrappedServerboundMovePlayerPosPacket w = new WrappedServerboundMovePlayerPosPacket();
        w.setX(3.0);
        w.setY(70.0);
        w.setZ(-8.0);
        w.setOnGround(true);
        w.setHorizontalCollision(false);

        assertEquals(PacketType.Play.Client.POSITION, w.getHandle().getType());

        ServerboundMovePlayerPacket.Pos p = (ServerboundMovePlayerPacket.Pos) w.getHandle().getHandle();

        assertEquals(3.0, p.getX(0.0), 1e-9);
        assertEquals(70.0, p.getY(0.0), 1e-9);
        assertEquals(-8.0, p.getZ(0.0), 1e-9);
        assertTrue(p.isOnGround());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundMovePlayerPacket.Pos nmsPacket = new ServerboundMovePlayerPacket.Pos(
                5.0, 63.5, 10.0, true, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMovePlayerPosPacket wrapper = new WrappedServerboundMovePlayerPosPacket(container);

        assertEquals(5.0, wrapper.getX(), 1e-9);
        assertEquals(63.5, wrapper.getY(), 1e-9);
        assertEquals(10.0, wrapper.getZ(), 1e-9);
        assertTrue(wrapper.isOnGround());
        assertFalse(wrapper.isHorizontalCollision());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundMovePlayerPacket.Pos nmsPacket = new ServerboundMovePlayerPacket.Pos(
                0.0, 64.0, 0.0, false, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMovePlayerPosPacket wrapper = new WrappedServerboundMovePlayerPosPacket(container);

        wrapper.setOnGround(true);

        assertEquals(0.0, wrapper.getX(), 1e-9);
        assertEquals(64.0, wrapper.getY(), 1e-9);
        assertTrue(wrapper.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMovePlayerPosPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
