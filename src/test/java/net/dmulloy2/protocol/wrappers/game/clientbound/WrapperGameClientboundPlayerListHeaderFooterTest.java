package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundPlayerListHeaderFooterTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundPlayerListHeaderFooter w = new WrapperGameClientboundPlayerListHeaderFooter();
        w.setHeader(WrappedChatComponent.fromText("Header text"));
        w.setFooter(WrappedChatComponent.fromText("Footer text"));
        assertTrue(w.getHeader().getJson().contains("Header text"));
        assertTrue(w.getFooter().getJson().contains("Footer text"));
        assertEquals(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        raw.getModifier().writeDefaults();
        raw.getChatComponents().write(0, WrappedChatComponent.fromText("Top"));
        raw.getChatComponents().write(1, WrappedChatComponent.fromText("Bottom"));

        WrapperGameClientboundPlayerListHeaderFooter w = new WrapperGameClientboundPlayerListHeaderFooter(raw);
        assertTrue(w.getHeader().getJson().contains("Top"));
        assertTrue(w.getFooter().getJson().contains("Bottom"));
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundPlayerListHeaderFooter w = new WrapperGameClientboundPlayerListHeaderFooter();
        w.setHeader(WrappedChatComponent.fromText("old"));

        new WrapperGameClientboundPlayerListHeaderFooter(w.getHandle())
                .setHeader(WrappedChatComponent.fromText("new header"));

        assertTrue(w.getHeader().getJson().contains("new header"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundPlayerListHeaderFooter(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
