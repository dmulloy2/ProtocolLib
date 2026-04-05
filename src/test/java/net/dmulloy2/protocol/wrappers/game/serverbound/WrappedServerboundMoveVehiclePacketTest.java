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
    void testAllArgsCreate() {
        WrappedServerboundMoveVehiclePacket w = new WrappedServerboundMoveVehiclePacket(new Vector(1.0, 2.0, 3.0), 0.5f, -3.0f, true);

        assertEquals(PacketType.Play.Client.VEHICLE_MOVE, w.getHandle().getType());

        assertEquals(new Vector(1.0, 2.0, 3.0), w.getPosition());
        assertEquals(0.5f, w.getYRot(), 1e-4f);
        assertEquals(-3.0f, w.getXRot(), 1e-4f);
        assertTrue(w.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundMoveVehiclePacket w = new WrappedServerboundMoveVehiclePacket();

        assertEquals(PacketType.Play.Client.VEHICLE_MOVE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundMoveVehiclePacket source = new WrappedServerboundMoveVehiclePacket(new Vector(1.0, 2.0, 3.0), 0.5f, -3.0f, true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMoveVehiclePacket wrapper = new WrappedServerboundMoveVehiclePacket(container);

        assertEquals(new Vector(1.0, 2.0, 3.0), wrapper.getPosition());
        assertEquals(0.5f, wrapper.getYRot(), 1e-4f);
        assertEquals(-3.0f, wrapper.getXRot(), 1e-4f);
        assertTrue(wrapper.isOnGround());

        wrapper.setPosition(new Vector(10.0, 20.0, 30.0));
        wrapper.setYRot(-3.0f);
        wrapper.setXRot(1.0f);
        wrapper.setOnGround(false);

        assertEquals(new Vector(10.0, 20.0, 30.0), wrapper.getPosition());
        assertEquals(-3.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(1.0f, wrapper.getXRot(), 1e-4f);
        assertFalse(wrapper.isOnGround());

        assertEquals(new Vector(10.0, 20.0, 30.0), source.getPosition());
        assertEquals(-3.0f, source.getYRot(), 1e-4f);
        assertEquals(1.0f, source.getXRot(), 1e-4f);
        assertFalse(source.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMoveVehiclePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
