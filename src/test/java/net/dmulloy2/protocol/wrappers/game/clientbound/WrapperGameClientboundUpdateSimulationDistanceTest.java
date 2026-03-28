package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundUpdateSimulationDistanceTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundUpdateSimulationDistance w = new WrapperGameClientboundUpdateSimulationDistance();
        w.setSimulationDistance(8);
        assertEquals(8, w.getSimulationDistance());
        assertEquals(PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 6);

        WrapperGameClientboundUpdateSimulationDistance w = new WrapperGameClientboundUpdateSimulationDistance(raw);
        assertEquals(6, w.getSimulationDistance());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundUpdateSimulationDistance w = new WrapperGameClientboundUpdateSimulationDistance();
        w.setSimulationDistance(4);

        new WrapperGameClientboundUpdateSimulationDistance(w.getHandle()).setSimulationDistance(12);

        assertEquals(12, w.getSimulationDistance());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundUpdateSimulationDistance(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
