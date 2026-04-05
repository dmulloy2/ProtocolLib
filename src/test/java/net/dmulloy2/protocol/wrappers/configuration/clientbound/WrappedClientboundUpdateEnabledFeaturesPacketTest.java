package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundUpdateEnabledFeaturesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Configuration.Server.UPDATE_ENABLED_FEATURES, new WrappedClientboundUpdateEnabledFeaturesPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundUpdateEnabledFeaturesPacket w = new WrappedClientboundUpdateEnabledFeaturesPacket();

        assertEquals(PacketType.Configuration.Server.UPDATE_ENABLED_FEATURES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.UPDATE_ENABLED_FEATURES);
        WrappedClientboundUpdateEnabledFeaturesPacket wrapper = new WrappedClientboundUpdateEnabledFeaturesPacket(container);




    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateEnabledFeaturesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
