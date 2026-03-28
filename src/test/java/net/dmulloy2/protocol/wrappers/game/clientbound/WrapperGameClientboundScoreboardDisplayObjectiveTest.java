package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundScoreboardDisplayObjectiveTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundScoreboardDisplayObjective w = new WrapperGameClientboundScoreboardDisplayObjective();
        w.setSlot(EnumWrappers.DisplaySlot.BELOW_NAME);
        w.setObjectiveName("myObjective");
        assertEquals(EnumWrappers.DisplaySlot.BELOW_NAME, w.getSlot());
        assertEquals("myObjective", w.getObjectiveName());
        assertEquals(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        raw.getModifier().writeDefaults();
        raw.getDisplaySlots().write(0, EnumWrappers.DisplaySlot.SIDEBAR);
        raw.getStrings().write(0, "testObj");

        WrapperGameClientboundScoreboardDisplayObjective w = new WrapperGameClientboundScoreboardDisplayObjective(raw);
        assertEquals(EnumWrappers.DisplaySlot.SIDEBAR, w.getSlot());
        assertEquals("testObj", w.getObjectiveName());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundScoreboardDisplayObjective w = new WrapperGameClientboundScoreboardDisplayObjective();
        w.setObjectiveName("old");

        new WrapperGameClientboundScoreboardDisplayObjective(w.getHandle()).setObjectiveName("new");

        assertEquals("new", w.getObjectiveName());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundScoreboardDisplayObjective(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
