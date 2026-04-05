package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerCombatKillPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerCombatKillPacket w = new WrappedClientboundPlayerCombatKillPacket(3, WrappedChatComponent.fromText("Testing"));

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_KILL, w.getHandle().getType());

        assertEquals(3, w.getPlayerId());
        assertEquals(WrappedChatComponent.fromText("Testing"), w.getMessage());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerCombatKillPacket w = new WrappedClientboundPlayerCombatKillPacket();

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_KILL, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerCombatKillPacket source = new WrappedClientboundPlayerCombatKillPacket(3, WrappedChatComponent.fromText("Testing"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerCombatKillPacket wrapper = new WrappedClientboundPlayerCombatKillPacket(container);

        assertEquals(3, wrapper.getPlayerId());
        assertEquals(WrappedChatComponent.fromText("Testing"), wrapper.getMessage());

        wrapper.setPlayerId(9);
        wrapper.setMessage(WrappedChatComponent.fromText("Modified"));

        assertEquals(9, wrapper.getPlayerId());
        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getMessage());

        assertEquals(9, source.getPlayerId());
        assertEquals(WrappedChatComponent.fromText("Modified"), source.getMessage());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerCombatKillPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
