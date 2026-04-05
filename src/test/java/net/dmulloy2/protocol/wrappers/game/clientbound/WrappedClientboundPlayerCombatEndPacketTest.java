package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerCombatEndPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerCombatEndPacket w = new WrappedClientboundPlayerCombatEndPacket(3);

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_END, w.getHandle().getType());

        assertEquals(3, w.getDuration());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerCombatEndPacket w = new WrappedClientboundPlayerCombatEndPacket();

        assertEquals(PacketType.Play.Server.PLAYER_COMBAT_END, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerCombatEndPacket source = new WrappedClientboundPlayerCombatEndPacket(3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerCombatEndPacket wrapper = new WrappedClientboundPlayerCombatEndPacket(container);

        assertEquals(3, wrapper.getDuration());

        wrapper.setDuration(9);

        assertEquals(9, wrapper.getDuration());

        assertEquals(9, source.getDuration());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerCombatEndPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
