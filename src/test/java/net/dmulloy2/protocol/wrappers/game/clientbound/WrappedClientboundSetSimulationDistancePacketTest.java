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
    void testAllArgsCreate() {
        WrappedClientboundSetSimulationDistancePacket w = new WrappedClientboundSetSimulationDistancePacket(3);

        assertEquals(PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE, w.getHandle().getType());

        ClientboundSetSimulationDistancePacket p = (ClientboundSetSimulationDistancePacket) w.getHandle().getHandle();

        assertEquals(3, p.simulationDistance());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetSimulationDistancePacket w = new WrappedClientboundSetSimulationDistancePacket();

        assertEquals(PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE, w.getHandle().getType());

        ClientboundSetSimulationDistancePacket p = (ClientboundSetSimulationDistancePacket) w.getHandle().getHandle();

        assertEquals(0, p.simulationDistance());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetSimulationDistancePacket nmsPacket = new ClientboundSetSimulationDistancePacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetSimulationDistancePacket wrapper = new WrappedClientboundSetSimulationDistancePacket(container);

        assertEquals(3, wrapper.getSimulationDistance());

        wrapper.setSimulationDistance(9);

        assertEquals(9, nmsPacket.simulationDistance());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetSimulationDistancePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
