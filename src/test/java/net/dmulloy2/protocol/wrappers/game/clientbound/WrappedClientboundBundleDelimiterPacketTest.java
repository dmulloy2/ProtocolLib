package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBundleDelimiterPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Server.BUNDLE, new WrappedClientboundBundleDelimiterPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundBundleDelimiterPacket w = new WrappedClientboundBundleDelimiterPacket();

        assertEquals(PacketType.Play.Server.BUNDLE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.BUNDLE);
        WrappedClientboundBundleDelimiterPacket wrapper = new WrappedClientboundBundleDelimiterPacket(container);

        assertEquals(PacketType.Play.Server.BUNDLE, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBundleDelimiterPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
