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
        WrappedClientboundCustomChatCompletionsPacket w = new WrappedClientboundCustomChatCompletionsPacket(
                WrappedClientboundCustomChatCompletionsPacket.Action.ADD, List.of("hello"));

        assertEquals(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS, w.getHandle().getType());

        assertEquals(WrappedClientboundCustomChatCompletionsPacket.Action.ADD, w.getAction());
        assertEquals(List.of("hello"), w.getEntries());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCustomChatCompletionsPacket w = new WrappedClientboundCustomChatCompletionsPacket();

        assertEquals(PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCustomChatCompletionsPacket source = new WrappedClientboundCustomChatCompletionsPacket(
                WrappedClientboundCustomChatCompletionsPacket.Action.ADD, List.of("hello"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundCustomChatCompletionsPacket wrapper = new WrappedClientboundCustomChatCompletionsPacket(container);

        assertEquals(WrappedClientboundCustomChatCompletionsPacket.Action.ADD, wrapper.getAction());
        assertEquals(List.of("hello"), wrapper.getEntries());

        wrapper.setAction(WrappedClientboundCustomChatCompletionsPacket.Action.REMOVE);
        wrapper.setEntries(List.of("modified"));

        assertEquals(WrappedClientboundCustomChatCompletionsPacket.Action.REMOVE, wrapper.getAction());
        assertEquals(List.of("modified"), wrapper.getEntries());

        assertEquals(WrappedClientboundCustomChatCompletionsPacket.Action.REMOVE, source.getAction());
        assertEquals(List.of("modified"), source.getEntries());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCustomChatCompletionsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
