package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerCombatEnterPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundPlayerCombatEnterPacket w = new WrappedClientboundPlayerCombatEnterPacket();

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_ENTER, w.getHandle().getType());

        ClientboundPlayerCombatEnterPacket p = (ClientboundPlayerCombatEnterPacket) w.getHandle().getHandle();

        assertNotNull(p);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.PLAYER_COMBAT_ENTER);
        container.getModifier().writeDefaults();

        WrappedClientboundPlayerCombatEnterPacket wrapper = new WrappedClientboundPlayerCombatEnterPacket(container);

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_ENTER, wrapper.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.PLAYER_COMBAT_ENTER);
        container.getModifier().writeDefaults();

        WrappedClientboundPlayerCombatEnterPacket wrapper = new WrappedClientboundPlayerCombatEnterPacket(container);

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_ENTER, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerCombatEnterPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
