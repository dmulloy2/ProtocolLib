package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSystemChatTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSystemChat w = new WrapperGameClientboundSystemChat();
        w.setContent(WrappedChatComponent.fromText("System message"));
        w.setOverlay(false);

        assertTrue(w.getContent().getJson().contains("System message"));
        assertFalse(w.isOverlay());
        assertEquals(PacketType.Play.Server.SYSTEM_CHAT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        raw.getModifier().writeDefaults();
        raw.getChatComponents().write(0, WrappedChatComponent.fromText("Test"));
        raw.getBooleans().write(0, true);

        WrapperGameClientboundSystemChat w = new WrapperGameClientboundSystemChat(raw);
        assertTrue(w.getContent().getJson().contains("Test"));
        assertTrue(w.isOverlay());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSystemChat w = new WrapperGameClientboundSystemChat();
        w.setOverlay(false);

        new WrapperGameClientboundSystemChat(w.getHandle()).setOverlay(true);

        assertTrue(w.isOverlay());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSystemChat(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
