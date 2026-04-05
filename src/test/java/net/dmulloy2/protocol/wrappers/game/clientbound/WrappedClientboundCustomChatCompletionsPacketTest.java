package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCustomChatCompletionsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundCustomChatCompletionsPacket w = new WrappedClientboundCustomChatCompletionsPacket(List.of("hello"));

        assertEquals(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS, w.getHandle().getType());

        assertEquals(List.of("hello"), w.getEntries());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCustomChatCompletionsPacket w = new WrappedClientboundCustomChatCompletionsPacket();

        assertEquals(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCustomChatCompletionsPacket source = new WrappedClientboundCustomChatCompletionsPacket(List.of("hello"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundCustomChatCompletionsPacket wrapper = new WrappedClientboundCustomChatCompletionsPacket(container);

        assertEquals(List.of("hello"), wrapper.getEntries());

        wrapper.setEntries(List.of("modified"));

        assertEquals(List.of("modified"), wrapper.getEntries());

        assertEquals(List.of("modified"), source.getEntries());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCustomChatCompletionsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
