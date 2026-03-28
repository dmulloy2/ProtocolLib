package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundPlayerCombatKillTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundPlayerCombatKill w = new WrapperGameClientboundPlayerCombatKill();
        w.setPlayerId(5);
        w.setMessage(WrappedChatComponent.fromText("Player was slain"));
        assertEquals(5, w.getPlayerId());
        assertTrue(w.getMessage().getJson().contains("Player was slain"));
        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_KILL, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.PLAYER_COMBAT_KILL);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 3);
        raw.getChatComponents().write(0, WrappedChatComponent.fromText("Died"));

        WrapperGameClientboundPlayerCombatKill w = new WrapperGameClientboundPlayerCombatKill(raw);
        assertEquals(3, w.getPlayerId());
        assertTrue(w.getMessage().getJson().contains("Died"));
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundPlayerCombatKill w = new WrapperGameClientboundPlayerCombatKill();
        w.setPlayerId(1);

        new WrapperGameClientboundPlayerCombatKill(w.getHandle()).setPlayerId(99);

        assertEquals(99, w.getPlayerId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundPlayerCombatKill(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
