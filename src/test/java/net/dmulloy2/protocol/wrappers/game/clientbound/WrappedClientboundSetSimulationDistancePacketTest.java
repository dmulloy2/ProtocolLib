package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetSimulationDistancePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetSimulationDistancePacket w = new WrappedClientboundSetSimulationDistancePacket();
        w.setSimulationDistance(8);

        assertEquals(PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE, w.getHandle().getType());

        ClientboundSetSimulationDistancePacket p = (ClientboundSetSimulationDistancePacket) w.getHandle().getHandle();

        assertEquals(8, p.simulationDistance());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetSimulationDistancePacket nmsPacket = new ClientboundSetSimulationDistancePacket(6);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetSimulationDistancePacket wrapper = new WrappedClientboundSetSimulationDistancePacket(container);

        assertEquals(6, wrapper.getSimulationDistance());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetSimulationDistancePacket nmsPacket = new ClientboundSetSimulationDistancePacket(4);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetSimulationDistancePacket wrapper = new WrappedClientboundSetSimulationDistancePacket(container);

        wrapper.setSimulationDistance(12);

        assertEquals(12, wrapper.getSimulationDistance());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetSimulationDistancePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
