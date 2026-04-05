package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundConfigurationAcknowledgedPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Client.CONFIGURATION_ACK, new WrappedServerboundConfigurationAcknowledgedPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundConfigurationAcknowledgedPacket w = new WrappedServerboundConfigurationAcknowledgedPacket();

        assertEquals(PacketType.Play.Client.CONFIGURATION_ACK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.CONFIGURATION_ACK);
        WrappedServerboundConfigurationAcknowledgedPacket wrapper = new WrappedServerboundConfigurationAcknowledgedPacket(container);

        assertEquals(PacketType.Play.Client.CONFIGURATION_ACK, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundConfigurationAcknowledgedPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
