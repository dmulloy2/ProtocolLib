package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundPlayerCombatEndTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundPlayerCombatEnd w = new WrapperGameClientboundPlayerCombatEnd();
        w.setDuration(40);
        assertEquals(40, w.getDuration());
        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_END, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.PLAYER_COMBAT_END);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 100);

        WrapperGameClientboundPlayerCombatEnd w = new WrapperGameClientboundPlayerCombatEnd(raw);
        assertEquals(100, w.getDuration());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundPlayerCombatEnd w = new WrapperGameClientboundPlayerCombatEnd();
        w.setDuration(10);

        new WrapperGameClientboundPlayerCombatEnd(w.getHandle()).setDuration(200);

        assertEquals(200, w.getDuration());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundPlayerCombatEnd(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
