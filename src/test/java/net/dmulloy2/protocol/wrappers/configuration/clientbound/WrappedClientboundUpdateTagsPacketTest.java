package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundUpdateTagsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Configuration.Server.UPDATE_TAGS, new WrappedClientboundUpdateTagsPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundUpdateTagsPacket w = new WrappedClientboundUpdateTagsPacket();

        assertEquals(PacketType.Configuration.Server.UPDATE_TAGS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.UPDATE_TAGS);
        WrappedClientboundUpdateTagsPacket wrapper = new WrappedClientboundUpdateTagsPacket(container);

        assertEquals(PacketType.Configuration.Server.UPDATE_TAGS, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateTagsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
