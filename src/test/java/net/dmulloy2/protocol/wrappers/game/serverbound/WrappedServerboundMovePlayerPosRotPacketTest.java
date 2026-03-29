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
    void testAllArgsCreate() {
        WrappedServerboundMovePlayerPosRotPacket w = new WrappedServerboundMovePlayerPosRotPacket(1.0, 65.0, 2.0, 180.0f, 10.0f, false, true);

        assertEquals(PacketType.Play.Client.POSITION_LOOK, w.getHandle().getType());

        ServerboundMovePlayerPacket.PosRot p = (ServerboundMovePlayerPacket.PosRot) w.getHandle().getHandle();

        assertEquals(1.0, p.getX(0.0), 1e-9);
        assertEquals(65.0, p.getY(0.0), 1e-9);
        assertEquals(2.0, p.getZ(0.0), 1e-9);
        assertFalse(p.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundMovePlayerPosRotPacket w = new WrappedServerboundMovePlayerPosRotPacket();

        assertEquals(PacketType.Play.Client.POSITION_LOOK, w.getHandle().getType());

        ServerboundMovePlayerPacket.PosRot p = (ServerboundMovePlayerPacket.PosRot) w.getHandle().getHandle();

        assertEquals(0.0, p.getX(0.0), 1e-9);
        assertEquals(0.0, p.getY(0.0), 1e-9);
        assertEquals(0.0, p.getZ(0.0), 1e-9);
        assertFalse(p.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundMovePlayerPacket.PosRot nmsPacket = new ServerboundMovePlayerPacket.PosRot(1.0, 65.0, 2.0, 180.0f, 10.0f, false, true);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMovePlayerPosRotPacket wrapper = new WrappedServerboundMovePlayerPosRotPacket(container);

        assertEquals(1.0, wrapper.getX(), 1e-9);
        assertEquals(65.0, wrapper.getY(), 1e-9);
        assertEquals(2.0, wrapper.getZ(), 1e-9);
        assertEquals(180.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(10.0f, wrapper.getXRot(), 1e-4f);
        assertFalse(wrapper.isOnGround());
        assertTrue(wrapper.isHorizontalCollision());

        wrapper.setX(50.0);
        wrapper.setY(70.0);
        wrapper.setZ(75.0);
        wrapper.setYRot(90.0f);
        wrapper.setXRot(-5.0f);
        wrapper.setOnGround(true);
        wrapper.setHorizontalCollision(false);

        assertEquals(50.0, nmsPacket.getX(0.0), 1e-9);
        assertEquals(70.0, nmsPacket.getY(0.0), 1e-9);
        assertEquals(75.0, nmsPacket.getZ(0.0), 1e-9);
        assertTrue(nmsPacket.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMovePlayerPosRotPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
