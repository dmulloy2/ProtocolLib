package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundStartConfigurationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Server.START_CONFIGURATION, new WrappedClientboundStartConfigurationPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundStartConfigurationPacket w = new WrappedClientboundStartConfigurationPacket();

        assertEquals(PacketType.Play.Server.START_CONFIGURATION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.START_CONFIGURATION);
        WrappedClientboundStartConfigurationPacket wrapper = new WrappedClientboundStartConfigurationPacket(container);

        assertEquals(PacketType.Play.Server.START_CONFIGURATION, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundStartConfigurationPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
