package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetTitleTextPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetTitleTextPacket w = new WrappedClientboundSetTitleTextPacket();
        w.setTitle(WrappedChatComponent.fromText("My Title"));
        assertTrue(w.getTitle().getJson().contains("My Title"));
        assertEquals(PacketType.Play.Server.SET_TITLE_TEXT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_TITLE_TEXT);
        raw.getModifier().writeDefaults();
        raw.getChatComponents().write(0, WrappedChatComponent.fromText("Hello Title"));

        WrappedClientboundSetTitleTextPacket w = new WrappedClientboundSetTitleTextPacket(raw);
        assertTrue(w.getTitle().getJson().contains("Hello Title"));
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetTitleTextPacket w = new WrappedClientboundSetTitleTextPacket();
        w.setTitle(WrappedChatComponent.fromText("old"));

        new WrappedClientboundSetTitleTextPacket(w.getHandle())
                .setTitle(WrappedChatComponent.fromText("updated title"));

        assertTrue(w.getTitle().getJson().contains("updated title"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetTitleTextPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
