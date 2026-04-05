package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPlayerLoadedPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Client.PLAYER_LOADED, new WrappedServerboundPlayerLoadedPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerLoadedPacket w = new WrappedServerboundPlayerLoadedPacket();
        assertEquals(PacketType.Play.Client.PLAYER_LOADED, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.PLAYER_LOADED);
        WrappedServerboundPlayerLoadedPacket wrapper = new WrappedServerboundPlayerLoadedPacket(container);
        assertEquals(PacketType.Play.Client.PLAYER_LOADED, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerLoadedPacket(new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
