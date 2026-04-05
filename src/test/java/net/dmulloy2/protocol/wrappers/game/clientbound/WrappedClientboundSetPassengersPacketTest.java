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
    void testAllArgsCreate() {
        WrappedClientboundSetPassengersPacket w = new WrappedClientboundSetPassengersPacket(3, new int[] { 4, 5, 6 });

        assertEquals(PacketType.Play.Server.MOUNT, w.getHandle().getType());

        assertEquals(3, w.getVehicleId());
        assertArrayEquals(new int[] { 4, 5, 6 }, w.getPassengerIds());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetPassengersPacket w = new WrappedClientboundSetPassengersPacket();

        assertEquals(PacketType.Play.Server.MOUNT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetPassengersPacket source = new WrappedClientboundSetPassengersPacket(3, new int[] { 4, 5, 6 });
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetPassengersPacket wrapper = new WrappedClientboundSetPassengersPacket(container);

        assertEquals(3, wrapper.getVehicleId());
        assertArrayEquals(new int[] { 4, 5, 6 }, wrapper.getPassengerIds());

        wrapper.setVehicleId(9);
        wrapper.setPassengerIds(new int[] { 10, 20, 30 });

        assertEquals(9, wrapper.getVehicleId());
        assertArrayEquals(new int[] { 10, 20, 30 }, wrapper.getPassengerIds());

        assertEquals(9, source.getVehicleId());
        assertArrayEquals(new int[] { 10, 20, 30 }, source.getPassengerIds());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetPassengersPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
