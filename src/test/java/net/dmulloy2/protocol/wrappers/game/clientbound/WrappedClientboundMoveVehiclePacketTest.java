package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveVehiclePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundMoveVehiclePacket w = new WrappedClientboundMoveVehiclePacket(new Vector(1.0, 2.0, 3.0), 0.5f, -3.0f);

        assertEquals(PacketType.Play.Server.VEHICLE_MOVE, w.getHandle().getType());

        assertEquals(new Vector(1.0, 2.0, 3.0), w.getPosition());
        assertEquals(0.5f, w.getYRot(), 1e-4f);
        assertEquals(-3.0f, w.getXRot(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMoveVehiclePacket w = new WrappedClientboundMoveVehiclePacket();

        assertEquals(PacketType.Play.Server.VEHICLE_MOVE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundMoveVehiclePacket source = new WrappedClientboundMoveVehiclePacket(new Vector(1.0, 2.0, 3.0), 0.5f, -3.0f);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMoveVehiclePacket wrapper = new WrappedClientboundMoveVehiclePacket(container);

        assertEquals(new Vector(1.0, 2.0, 3.0), wrapper.getPosition());
        assertEquals(0.5f, wrapper.getYRot(), 1e-4f);
        assertEquals(-3.0f, wrapper.getXRot(), 1e-4f);

        wrapper.setPosition(new Vector(10.0, 20.0, 30.0));
        wrapper.setYRot(-3.0f);
        wrapper.setXRot(1.0f);

        assertEquals(new Vector(10.0, 20.0, 30.0), wrapper.getPosition());
        assertEquals(-3.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(1.0f, wrapper.getXRot(), 1e-4f);

        assertEquals(new Vector(10.0, 20.0, 30.0), source.getPosition());
        assertEquals(-3.0f, source.getYRot(), 1e-4f);
        assertEquals(1.0f, source.getXRot(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveVehiclePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
