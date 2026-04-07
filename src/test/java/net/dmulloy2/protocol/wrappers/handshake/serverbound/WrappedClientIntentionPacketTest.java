package net.dmulloy2.protocol.wrappers.handshake.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientIntentionPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedClientIntentionPacket w = new WrappedClientIntentionPacket(769, "localhost", 25565, EnumWrappers.ClientIntent.LOGIN);

        assertEquals(PacketType.Handshake.Client.SET_PROTOCOL, w.getHandle().getType());

        assertEquals(769, w.getProtocolVersion());
        assertEquals("localhost", w.getHostName());
        assertEquals(25565, w.getPort());
        assertEquals(EnumWrappers.ClientIntent.LOGIN, w.getIntention());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientIntentionPacket w = new WrappedClientIntentionPacket();
        assertEquals(PacketType.Handshake.Client.SET_PROTOCOL, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientIntentionPacket source = new WrappedClientIntentionPacket(769, "localhost", 25565, EnumWrappers.ClientIntent.LOGIN);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientIntentionPacket wrapper = new WrappedClientIntentionPacket(container);

        assertEquals(769, wrapper.getProtocolVersion());
        assertEquals("localhost", wrapper.getHostName());
        assertEquals(25565, wrapper.getPort());
        assertEquals(EnumWrappers.ClientIntent.LOGIN, wrapper.getIntention());

        wrapper.setProtocolVersion(765);
        wrapper.setHostName("example.com");
        wrapper.setPort(19132);
        wrapper.setIntention(EnumWrappers.ClientIntent.STATUS);

        assertEquals(765, wrapper.getProtocolVersion());
        assertEquals("example.com", wrapper.getHostName());
        assertEquals(19132, wrapper.getPort());
        assertEquals(EnumWrappers.ClientIntent.STATUS, wrapper.getIntention());

        assertEquals(765, source.getProtocolVersion());
        assertEquals("example.com", source.getHostName());
        assertEquals(19132, source.getPort());
        assertEquals(EnumWrappers.ClientIntent.STATUS, source.getIntention());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientIntentionPacket(
                        new PacketContainer(PacketType.Status.Client.PING)));
    }
}
