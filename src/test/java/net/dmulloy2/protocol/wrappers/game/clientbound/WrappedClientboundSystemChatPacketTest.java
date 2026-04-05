package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSystemChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSystemChatPacket w = new WrappedClientboundSystemChatPacket(WrappedChatComponent.fromText("Hello, world!"), false);

        assertEquals(PacketType.Play.Server.SYSTEM_CHAT, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getContent());
        assertFalse(w.isOverlay());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSystemChatPacket w = new WrappedClientboundSystemChatPacket();

        assertEquals(PacketType.Play.Server.SYSTEM_CHAT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSystemChatPacket source = new WrappedClientboundSystemChatPacket(WrappedChatComponent.fromText("Hello, world!"), false);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSystemChatPacket wrapper = new WrappedClientboundSystemChatPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getContent());
        assertFalse(wrapper.isOverlay());

        wrapper.setContent(WrappedChatComponent.fromText("Modified"));
        wrapper.setOverlay(true);

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getContent());
        assertTrue(wrapper.isOverlay());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getContent());
        assertTrue(source.isOverlay());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSystemChatPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
