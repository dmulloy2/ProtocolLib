package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundFinishConfigurationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Configuration.Server.FINISH_CONFIGURATION, new WrappedClientboundFinishConfigurationPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundFinishConfigurationPacket w = new WrappedClientboundFinishConfigurationPacket();

        assertEquals(PacketType.Configuration.Server.FINISH_CONFIGURATION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.FINISH_CONFIGURATION);
        WrappedClientboundFinishConfigurationPacket wrapper = new WrappedClientboundFinishConfigurationPacket(container);

        assertEquals(PacketType.Configuration.Server.FINISH_CONFIGURATION, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundFinishConfigurationPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
