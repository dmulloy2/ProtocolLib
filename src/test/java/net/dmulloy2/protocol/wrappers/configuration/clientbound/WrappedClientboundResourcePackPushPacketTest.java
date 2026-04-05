package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundResourcePackPushPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundResourcePackPushPacket w = new WrappedClientboundResourcePackPushPacket("hello", "world", true, UUID.fromString("12345678-1234-1234-1234-123456789abc"), Optional.empty());

        assertEquals(PacketType.Configuration.Server.ADD_RESOURCE_PACK, w.getHandle().getType());

        assertEquals("hello", w.getUrl());
        assertEquals("world", w.getHash());
        assertTrue(w.isRequired());
        assertEquals(UUID.fromString("12345678-1234-1234-1234-123456789abc"), w.getId());
        assertEquals(Optional.empty(), w.getPrompt());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundResourcePackPushPacket w = new WrappedClientboundResourcePackPushPacket();

        assertEquals(PacketType.Configuration.Server.ADD_RESOURCE_PACK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundResourcePackPushPacket source = new WrappedClientboundResourcePackPushPacket("hello", "world", true, UUID.fromString("12345678-1234-1234-1234-123456789abc"), Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedClientboundResourcePackPushPacket.TYPE, nmsPacket);
        WrappedClientboundResourcePackPushPacket wrapper = new WrappedClientboundResourcePackPushPacket(container);

        assertEquals("hello", wrapper.getUrl());
        assertEquals("world", wrapper.getHash());
        assertTrue(wrapper.isRequired());
        assertEquals(UUID.fromString("12345678-1234-1234-1234-123456789abc"), wrapper.getId());
        assertEquals(Optional.empty(), wrapper.getPrompt());

        wrapper.setUrl("modified");
        wrapper.setHash("hello");
        wrapper.setRequired(false);
        wrapper.setId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        wrapper.setPrompt(Optional.empty());

        assertEquals("modified", wrapper.getUrl());
        assertEquals("hello", wrapper.getHash());
        assertFalse(wrapper.isRequired());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), wrapper.getId());
        assertEquals(Optional.empty(), wrapper.getPrompt());

        assertEquals("modified", source.getUrl());
        assertEquals("hello", source.getHash());
        assertFalse(source.isRequired());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), source.getId());
        assertEquals(Optional.empty(), source.getPrompt());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundResourcePackPushPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
