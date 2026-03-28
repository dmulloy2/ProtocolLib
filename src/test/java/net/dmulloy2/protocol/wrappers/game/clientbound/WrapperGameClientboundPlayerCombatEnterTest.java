package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundPlayerCombatEnterTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundPlayerCombatEnter w = new WrapperGameClientboundPlayerCombatEnter();
        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_ENTER, w.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundPlayerCombatEnter(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
