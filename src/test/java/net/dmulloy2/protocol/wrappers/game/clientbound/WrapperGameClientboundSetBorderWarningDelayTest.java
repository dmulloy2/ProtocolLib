package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetBorderWarningDelayTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetBorderWarningDelay w = new WrapperGameClientboundSetBorderWarningDelay();
        w.setWarningDelay(15);
        assertEquals(15, w.getWarningDelay());
        assertEquals(PacketType.Play.Server.SET_BORDER_WARNING_DELAY, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_BORDER_WARNING_DELAY);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 30);

        WrapperGameClientboundSetBorderWarningDelay w = new WrapperGameClientboundSetBorderWarningDelay(raw);
        assertEquals(30, w.getWarningDelay());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetBorderWarningDelay w = new WrapperGameClientboundSetBorderWarningDelay();
        w.setWarningDelay(5);

        new WrapperGameClientboundSetBorderWarningDelay(w.getHandle()).setWarningDelay(60);

        assertEquals(60, w.getWarningDelay());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetBorderWarningDelay(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
