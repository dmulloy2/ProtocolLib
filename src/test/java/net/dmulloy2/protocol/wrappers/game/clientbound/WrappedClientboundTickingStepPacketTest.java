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
    void testAllArgsCreate() {
        WrappedClientboundTickingStepPacket w = new WrappedClientboundTickingStepPacket(3);

        assertEquals(PacketType.Play.Server.TICKING_STEP_STATE, w.getHandle().getType());

        ClientboundTickingStepPacket p = (ClientboundTickingStepPacket) w.getHandle().getHandle();

        assertEquals(3, p.tickSteps());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTickingStepPacket w = new WrappedClientboundTickingStepPacket();

        assertEquals(PacketType.Play.Server.TICKING_STEP_STATE, w.getHandle().getType());

        ClientboundTickingStepPacket p = (ClientboundTickingStepPacket) w.getHandle().getHandle();

        assertEquals(0, p.tickSteps());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTickingStepPacket nmsPacket = new ClientboundTickingStepPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTickingStepPacket wrapper = new WrappedClientboundTickingStepPacket(container);

        assertEquals(3, wrapper.getTickSteps());

        wrapper.setTickSteps(9);

        assertEquals(9, nmsPacket.tickSteps());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTickingStepPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
