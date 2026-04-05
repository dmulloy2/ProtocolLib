package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundFinishConfigurationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Configuration.Client.FINISH_CONFIGURATION, new WrappedServerboundFinishConfigurationPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundFinishConfigurationPacket w = new WrappedServerboundFinishConfigurationPacket();

        assertEquals(PacketType.Configuration.Client.FINISH_CONFIGURATION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Client.FINISH_CONFIGURATION);
        WrappedServerboundFinishConfigurationPacket wrapper = new WrappedServerboundFinishConfigurationPacket(container);

        assertEquals(PacketType.Configuration.Client.FINISH_CONFIGURATION, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundFinishConfigurationPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
