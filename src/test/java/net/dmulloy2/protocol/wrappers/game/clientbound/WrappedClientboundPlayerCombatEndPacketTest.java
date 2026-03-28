package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerCombatEndPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundPlayerCombatEndPacket w = new WrappedClientboundPlayerCombatEndPacket();
        w.setDuration(40);

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_END, w.getHandle().getType());

        ClientboundPlayerCombatEndPacket p = (ClientboundPlayerCombatEndPacket) w.getHandle().getHandle();

        assertNotNull(p);
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundPlayerCombatEndPacket nmsPacket = new ClientboundPlayerCombatEndPacket(100);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerCombatEndPacket wrapper = new WrappedClientboundPlayerCombatEndPacket(container);

        assertEquals(100, wrapper.getDuration());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundPlayerCombatEndPacket nmsPacket = new ClientboundPlayerCombatEndPacket(100);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerCombatEndPacket wrapper = new WrappedClientboundPlayerCombatEndPacket(container);

        wrapper.setDuration(200);

        assertEquals(200, wrapper.getDuration());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerCombatEndPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
