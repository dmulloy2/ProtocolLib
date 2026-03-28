package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
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
        assertEquals(3, w.getTickSteps());
        assertEquals(PacketType.Play.Server.TICKING_STEP_STATE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.TICKING_STEP_STATE);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 7);

        WrappedClientboundTickingStepPacket w = new WrappedClientboundTickingStepPacket(raw);
        assertEquals(7, w.getTickSteps());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundTickingStepPacket w = new WrappedClientboundTickingStepPacket();
        w.setTickSteps(1);

        new WrappedClientboundTickingStepPacket(w.getHandle()).setTickSteps(10);

        assertEquals(10, w.getTickSteps());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTickingStepPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
