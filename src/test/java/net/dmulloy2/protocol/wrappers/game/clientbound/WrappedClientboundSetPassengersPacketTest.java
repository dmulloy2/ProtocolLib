package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetPassengersPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetPassengersPacket w = new WrappedClientboundSetPassengersPacket();
        int[] passengers = new int[]{5, 6, 7};
        w.setVehicleId(1);
        w.setPassengerIds(passengers);
        assertEquals(1, w.getVehicleId());
        assertArrayEquals(passengers, w.getPassengerIds());
        assertEquals(PacketType.Play.Server.MOUNT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.MOUNT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 99);
        raw.getIntegerArrays().write(0, new int[]{10, 20});

        WrappedClientboundSetPassengersPacket w = new WrappedClientboundSetPassengersPacket(raw);
        assertEquals(99, w.getVehicleId());
        assertArrayEquals(new int[]{10, 20}, w.getPassengerIds());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetPassengersPacket w = new WrappedClientboundSetPassengersPacket();
        w.setVehicleId(1);

        new WrappedClientboundSetPassengersPacket(w.getHandle()).setVehicleId(42);

        assertEquals(42, w.getVehicleId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetPassengersPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
