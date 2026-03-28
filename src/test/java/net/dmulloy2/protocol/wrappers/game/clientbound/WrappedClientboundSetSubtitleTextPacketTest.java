package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetSubtitleTextPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetSubtitleTextPacket w = new WrappedClientboundSetSubtitleTextPacket();
        w.setSubtitle(WrappedChatComponent.fromText("My Subtitle"));
        assertTrue(w.getSubtitle().getJson().contains("My Subtitle"));
        assertEquals(PacketType.Play.Server.SET_SUBTITLE_TEXT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_SUBTITLE_TEXT);
        raw.getModifier().writeDefaults();
        raw.getChatComponents().write(0, WrappedChatComponent.fromText("Hello Sub"));

        WrappedClientboundSetSubtitleTextPacket w = new WrappedClientboundSetSubtitleTextPacket(raw);
        assertTrue(w.getSubtitle().getJson().contains("Hello Sub"));
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetSubtitleTextPacket w = new WrappedClientboundSetSubtitleTextPacket();
        w.setSubtitle(WrappedChatComponent.fromText("old"));

        new WrappedClientboundSetSubtitleTextPacket(w.getHandle())
                .setSubtitle(WrappedChatComponent.fromText("updated sub"));

        assertTrue(w.getSubtitle().getJson().contains("updated sub"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetSubtitleTextPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
