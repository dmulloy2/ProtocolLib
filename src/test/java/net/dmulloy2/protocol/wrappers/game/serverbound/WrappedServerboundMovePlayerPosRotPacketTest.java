package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundMovePlayerPosRotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedServerboundMovePlayerPosRotPacket w = new WrappedServerboundMovePlayerPosRotPacket();
        w.setX(10.5);
        w.setY(64.0);
        w.setZ(-5.5);
        w.setYRot(90.0f);
        w.setXRot(-15.0f);
        w.setOnGround(true);
        w.setHorizontalCollision(false);

        assertEquals(PacketType.Play.Client.POSITION_LOOK, w.getHandle().getType());

        ServerboundMovePlayerPacket.PosRot p = (ServerboundMovePlayerPacket.PosRot) w.getHandle().getHandle();

        assertEquals(10.5, p.getX(0.0), 1e-9);
        assertEquals(64.0, p.getY(0.0), 1e-9);
        assertEquals(-5.5, p.getZ(0.0), 1e-9);
        assertTrue(p.isOnGround());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundMovePlayerPacket.PosRot nmsPacket = new ServerboundMovePlayerPacket.PosRot(
                1.0, 65.0, 2.0, 180.0f, 10.0f, false, true
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMovePlayerPosRotPacket wrapper = new WrappedServerboundMovePlayerPosRotPacket(container);

        assertEquals(1.0, wrapper.getX(), 1e-9);
        assertEquals(65.0, wrapper.getY(), 1e-9);
        assertEquals(2.0, wrapper.getZ(), 1e-9);
        assertEquals(180.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(10.0f, wrapper.getXRot(), 1e-4f);
        assertFalse(wrapper.isOnGround());
        assertTrue(wrapper.isHorizontalCollision());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundMovePlayerPacket.PosRot nmsPacket = new ServerboundMovePlayerPacket.PosRot(
                0.0, 64.0, 0.0, 0.0f, 0.0f, false, false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMovePlayerPosRotPacket wrapper = new WrappedServerboundMovePlayerPosRotPacket(container);

        wrapper.setOnGround(true);

        assertEquals(0.0, wrapper.getX(), 1e-9);
        assertEquals(64.0, wrapper.getY(), 1e-9);
        assertTrue(wrapper.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMovePlayerPosRotPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
