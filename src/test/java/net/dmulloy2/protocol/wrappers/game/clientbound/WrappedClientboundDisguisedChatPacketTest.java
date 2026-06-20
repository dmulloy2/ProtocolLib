package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedChatTypeBound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDisguisedChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedChatTypeBound chatType = chatType("Sender");
        WrappedClientboundDisguisedChatPacket w = new WrappedClientboundDisguisedChatPacket(
                WrappedChatComponent.fromText("Hello, world!"), chatType);

        assertEquals(PacketType.Play.Server.DISGUISED_CHAT, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getMessage());
        assertEquals(chatType, w.getChatType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDisguisedChatPacket w = new WrappedClientboundDisguisedChatPacket();

        assertEquals(PacketType.Play.Server.DISGUISED_CHAT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedChatTypeBound chatType = chatType("Sender");
        WrappedClientboundDisguisedChatPacket source = new WrappedClientboundDisguisedChatPacket(
                WrappedChatComponent.fromText("Hello, world!"), chatType);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundDisguisedChatPacket wrapper = new WrappedClientboundDisguisedChatPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getMessage());
        assertEquals(chatType, wrapper.getChatType());

        wrapper.setMessage(WrappedChatComponent.fromText("Modified"));
        WrappedChatTypeBound modifiedChatType = chatType("Modified Sender");
        wrapper.setChatType(modifiedChatType);

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getMessage());
        assertEquals(modifiedChatType, wrapper.getChatType());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getMessage());
        assertEquals(modifiedChatType, source.getChatType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDisguisedChatPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }

    private static WrappedChatTypeBound chatType(String name) {
        return new WrappedChatTypeBound(
                new MinecraftKey("chat"),
                WrappedChatComponent.fromText(name),
                Optional.empty());
    }
}
