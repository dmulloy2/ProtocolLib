package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
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
        int[] passengers = {5, 6, 7};
        w.setVehicleId(1);
        w.setPassengerIds(passengers);

        assertEquals(PacketType.Play.Server.MOUNT, w.getHandle().getType());

        ClientboundSetPassengersPacket p = (ClientboundSetPassengersPacket) w.getHandle().getHandle();

        assertEquals(1, p.getVehicle());
        assertArrayEquals(passengers, p.getPassengers());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.MOUNT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 99);
        raw.getIntegerArrays().write(0, new int[]{10, 20});

        WrappedClientboundSetPassengersPacket wrapper = new WrappedClientboundSetPassengersPacket(raw);

        assertEquals(99, wrapper.getVehicleId());
        assertArrayEquals(new int[]{10, 20}, wrapper.getPassengerIds());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.MOUNT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 1);
        raw.getIntegerArrays().write(0, new int[]{3});

        WrappedClientboundSetPassengersPacket wrapper = new WrappedClientboundSetPassengersPacket(raw);
        wrapper.setVehicleId(42);

        assertEquals(42, wrapper.getVehicleId());
        assertArrayEquals(new int[]{3}, wrapper.getPassengerIds());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetPassengersPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
