package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundServerLinksPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Configuration.Server.SERVER_LINKS, new WrappedClientboundServerLinksPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundServerLinksPacket w = new WrappedClientboundServerLinksPacket();

        assertEquals(PacketType.Configuration.Server.SERVER_LINKS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.SERVER_LINKS);
        WrappedClientboundServerLinksPacket wrapper = new WrappedClientboundServerLinksPacket(container);

        assertEquals(PacketType.Configuration.Server.SERVER_LINKS, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundServerLinksPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
