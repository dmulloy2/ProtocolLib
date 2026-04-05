package net.dmulloy2.protocol.wrappers.handshake.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientIntentionPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        // No all-args constructor: ClientIntent has no EnumWrappers entry
        assertEquals(PacketType.Handshake.Client.SET_PROTOCOL, new WrappedClientIntentionPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientIntentionPacket w = new WrappedClientIntentionPacket();
        assertEquals(PacketType.Handshake.Client.SET_PROTOCOL, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Handshake.Client.SET_PROTOCOL);
        WrappedClientIntentionPacket wrapper = new WrappedClientIntentionPacket(container);
        wrapper.setProtocolVersion(769);
        wrapper.setHostName("localhost");
        wrapper.setPort(25565);
        assertEquals(769, wrapper.getProtocolVersion());
        assertEquals("localhost", wrapper.getHostName());
        assertEquals(25565, wrapper.getPort());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientIntentionPacket(
                        new PacketContainer(PacketType.Status.Client.PING)));
    }
}
