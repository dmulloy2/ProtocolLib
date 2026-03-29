package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
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
        Vector pos = new Vector(1.0, 65.0, -3.0);
        WrappedServerboundMoveVehiclePacket w = new WrappedServerboundMoveVehiclePacket(pos, 180.0f, 10.0f, true);

        assertEquals(PacketType.Play.Client.VEHICLE_MOVE, w.getHandle().getType());

        assertEquals(pos, w.getPosition());
        assertEquals(180.0f, w.getYRot(), 1e-4f);
        assertEquals(10.0f, w.getXRot(), 1e-4f);
        assertTrue(w.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundMoveVehiclePacket w = new WrappedServerboundMoveVehiclePacket();

        assertEquals(PacketType.Play.Client.VEHICLE_MOVE, w.getHandle().getType());

        assertEquals(0.0f, w.getYRot(), 1e-4f);
        assertEquals(0.0f, w.getXRot(), 1e-4f);
        assertFalse(w.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        Vector pos = new Vector(1.0, 65.0, -3.0);
        WrappedServerboundMoveVehiclePacket src = new WrappedServerboundMoveVehiclePacket(pos, 180.0f, 10.0f, true);
        ServerboundMoveVehiclePacket nmsPacket = (ServerboundMoveVehiclePacket) src.getHandle().getHandle();

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundMoveVehiclePacket wrapper = new WrappedServerboundMoveVehiclePacket(container);

        assertEquals(pos, wrapper.getPosition());
        assertEquals(180.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(10.0f, wrapper.getXRot(), 1e-4f);
        assertTrue(wrapper.isOnGround());

        Vector newPos = new Vector(50.0, 70.0, 25.0);
        wrapper.setPosition(newPos);
        wrapper.setYRot(90.0f);
        wrapper.setXRot(-5.0f);
        wrapper.setOnGround(false);

        assertEquals(newPos, wrapper.getPosition());
        assertEquals(90.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(-5.0f, wrapper.getXRot(), 1e-4f);
        assertFalse(wrapper.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundMoveVehiclePacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
