package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundMoveVehiclePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        // NMS constructor takes Vec3; use wrapper-based approach
        WrappedServerboundMoveVehiclePacket w = new WrappedServerboundMoveVehiclePacket();
        w.setPosition(new Vector(1.0, 64.0, -1.0));
        w.setYRot(90.0f);
        w.setXRot(-15.0f);
        w.setOnGround(true);

        assertEquals(PacketType.Play.Client.VEHICLE_MOVE, w.getHandle().getType());
        assertEquals(new Vector(1.0, 64.0, -1.0), w.getPosition());
        assertEquals(90.0f, w.getYRot(), 1e-4f);
        assertEquals(-15.0f, w.getXRot(), 1e-4f);
        assertTrue(w.isOnGround());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedServerboundMoveVehiclePacket src = new WrappedServerboundMoveVehiclePacket();
        src.setPosition(new Vector(5.0, 70.0, 5.0));
        src.setYRot(45.0f);
        src.setXRot(10.0f);
        src.setOnGround(false);

        WrappedServerboundMoveVehiclePacket wrapper = new WrappedServerboundMoveVehiclePacket(src.getHandle());

        assertEquals(new Vector(5.0, 70.0, 5.0), wrapper.getPosition());
        assertEquals(45.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(10.0f, wrapper.getXRot(), 1e-4f);
        assertFalse(wrapper.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundMoveVehiclePacket w = new WrappedServerboundMoveVehiclePacket();
        w.setPosition(new Vector(0.0, 64.0, 0.0));
        w.setYRot(0.0f);
        w.setXRot(0.0f);
        w.setOnGround(true);

        w.setYRot(180.0f);

        assertEquals(180.0f, w.getYRot(), 1e-4f);
        assertTrue(w.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMoveVehiclePacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
