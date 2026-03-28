package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundServerDifficultyTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundServerDifficulty w = new WrapperGameClientboundServerDifficulty();
        w.setDifficulty(EnumWrappers.Difficulty.HARD);
        w.setLocked(true);
        assertEquals(EnumWrappers.Difficulty.HARD, w.getDifficulty());
        assertTrue(w.isLocked());
        assertEquals(PacketType.Play.Server.SERVER_DIFFICULTY, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SERVER_DIFFICULTY);
        raw.getModifier().writeDefaults();
        raw.getDifficulties().write(0, EnumWrappers.Difficulty.NORMAL);
        raw.getBooleans().write(0, false);

        WrapperGameClientboundServerDifficulty w = new WrapperGameClientboundServerDifficulty(raw);
        assertEquals(EnumWrappers.Difficulty.NORMAL, w.getDifficulty());
        assertFalse(w.isLocked());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundServerDifficulty w = new WrapperGameClientboundServerDifficulty();
        w.setDifficulty(EnumWrappers.Difficulty.EASY);

        new WrapperGameClientboundServerDifficulty(w.getHandle()).setDifficulty(EnumWrappers.Difficulty.PEACEFUL);

        assertEquals(EnumWrappers.Difficulty.PEACEFUL, w.getDifficulty());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundServerDifficulty(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
