package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTickingStepPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundTickingStepPacket w = new WrappedClientboundTickingStepPacket();
        w.setTickSteps(3);

        assertEquals(PacketType.Play.Server.TICKING_STEP_STATE, w.getHandle().getType());

        ClientboundTickingStepPacket p = (ClientboundTickingStepPacket) w.getHandle().getHandle();

        assertEquals(3, p.tickSteps());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundTickingStepPacket nmsPacket = new ClientboundTickingStepPacket(7);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTickingStepPacket wrapper = new WrappedClientboundTickingStepPacket(container);

        assertEquals(7, wrapper.getTickSteps());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTickingStepPacket nmsPacket = new ClientboundTickingStepPacket(1);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTickingStepPacket wrapper = new WrappedClientboundTickingStepPacket(container);

        wrapper.setTickSteps(10);

        assertEquals(10, wrapper.getTickSteps());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTickingStepPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
