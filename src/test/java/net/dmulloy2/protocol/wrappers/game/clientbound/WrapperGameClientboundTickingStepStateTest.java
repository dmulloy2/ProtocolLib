package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundTickingStepStateTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundTickingStepState w = new WrapperGameClientboundTickingStepState();
        w.setTickSteps(3);
        assertEquals(3, w.getTickSteps());
        assertEquals(PacketType.Play.Server.TICKING_STEP_STATE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.TICKING_STEP_STATE);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 7);

        WrapperGameClientboundTickingStepState w = new WrapperGameClientboundTickingStepState(raw);
        assertEquals(7, w.getTickSteps());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundTickingStepState w = new WrapperGameClientboundTickingStepState();
        w.setTickSteps(1);

        new WrapperGameClientboundTickingStepState(w.getHandle()).setTickSteps(10);

        assertEquals(10, w.getTickSteps());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundTickingStepState(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
