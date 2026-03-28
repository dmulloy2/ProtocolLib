package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetActionBarTextTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetActionBarText w = new WrapperGameClientboundSetActionBarText();
        w.setText(WrappedChatComponent.fromText("Hello, action bar!"));

        assertTrue(w.getText().getJson().contains("Hello, action bar!"));
        assertEquals(PacketType.Play.Server.SET_ACTION_BAR_TEXT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_ACTION_BAR_TEXT);
        raw.getModifier().writeDefaults();
        raw.getChatComponents().write(0, WrappedChatComponent.fromText("Bar text"));

        WrapperGameClientboundSetActionBarText w = new WrapperGameClientboundSetActionBarText(raw);
        assertTrue(w.getText().getJson().contains("Bar text"));
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetActionBarText w = new WrapperGameClientboundSetActionBarText();
        w.setText(WrappedChatComponent.fromText("original"));

        new WrapperGameClientboundSetActionBarText(w.getHandle())
                .setText(WrappedChatComponent.fromText("updated"));

        assertTrue(w.getText().getJson().contains("updated"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetActionBarText(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
