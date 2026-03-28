package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerCombatKillPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundPlayerCombatKillPacket w = new WrappedClientboundPlayerCombatKillPacket();
        w.setPlayerId(5);
        w.setMessage(WrappedChatComponent.fromText("Player was slain"));

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_KILL, w.getHandle().getType());

        ClientboundPlayerCombatKillPacket p = (ClientboundPlayerCombatKillPacket) w.getHandle().getHandle();

        assertEquals(5, p.playerId());
        assertNotNull(p.message());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundPlayerCombatKillPacket nmsPacket = new ClientboundPlayerCombatKillPacket(
                3, Component.literal("Died")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerCombatKillPacket wrapper = new WrappedClientboundPlayerCombatKillPacket(container);

        assertEquals(3, wrapper.getPlayerId());
        assertTrue(wrapper.getMessage().getJson().contains("Died"));
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundPlayerCombatKillPacket nmsPacket = new ClientboundPlayerCombatKillPacket(
                3, Component.literal("Died")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerCombatKillPacket wrapper = new WrappedClientboundPlayerCombatKillPacket(container);

        wrapper.setPlayerId(99);

        assertEquals(99, wrapper.getPlayerId());
        assertTrue(wrapper.getMessage().getJson().contains("Died"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerCombatKillPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
