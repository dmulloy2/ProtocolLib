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
    void testCreate() {
        WrappedClientboundSystemChatPacket w = new WrappedClientboundSystemChatPacket();
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

        WrappedClientboundSystemChatPacket w = new WrappedClientboundSystemChatPacket(raw);
        assertTrue(w.getContent().getJson().contains("Test"));
        assertTrue(w.isOverlay());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSystemChatPacket w = new WrappedClientboundSystemChatPacket();
        w.setOverlay(false);

        new WrappedClientboundSystemChatPacket(w.getHandle()).setOverlay(true);

        assertTrue(w.isOverlay());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSystemChatPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
