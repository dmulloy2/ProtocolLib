package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundClearTitlesTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundClearTitles w = new WrapperGameClientboundClearTitles();
        w.setResetTimes(true);
        assertTrue(w.isResetTimes());
        assertEquals(PacketType.Play.Server.CLEAR_TITLES, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.CLEAR_TITLES);
        raw.getModifier().writeDefaults();
        raw.getBooleans().write(0, false);

        WrapperGameClientboundClearTitles w = new WrapperGameClientboundClearTitles(raw);
        assertFalse(w.isResetTimes());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundClearTitles w = new WrapperGameClientboundClearTitles();
        w.setResetTimes(false);

        new WrapperGameClientboundClearTitles(w.getHandle()).setResetTimes(true);

        assertTrue(w.isResetTimes());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundClearTitles(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
