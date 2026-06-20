package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedTagPayload;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundUpdateTagsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        Map<MinecraftKey, WrappedTagPayload> tags = tags();
        WrappedClientboundUpdateTagsPacket wrapper = new WrappedClientboundUpdateTagsPacket(tags);

        assertEquals(PacketType.Play.Server.TAGS, wrapper.getHandle().getType());
        assertEquals(tags, wrapper.getTags());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundUpdateTagsPacket w = new WrappedClientboundUpdateTagsPacket();

        assertEquals(PacketType.Play.Server.TAGS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new WrappedClientboundUpdateTagsPacket(Map.of()).getHandle();
        WrappedClientboundUpdateTagsPacket wrapper = new WrappedClientboundUpdateTagsPacket(container);
        Map<MinecraftKey, WrappedTagPayload> tags = tags();

        wrapper.setTags(tags);
        assertEquals(PacketType.Play.Server.TAGS, wrapper.getHandle().getType());
        assertEquals(tags, wrapper.getTags());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateTagsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }

    private static Map<MinecraftKey, WrappedTagPayload> tags() {
        return Map.of(
                new MinecraftKey("minecraft", "item"),
                new WrappedTagPayload(Map.of(new MinecraftKey("minecraft", "test_tag"), List.of(1, 2, 3))));
    }
}
