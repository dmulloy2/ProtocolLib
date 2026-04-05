package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetActionBarTextPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetActionBarTextPacket w = new WrappedClientboundSetActionBarTextPacket(WrappedChatComponent.fromText("Hello, world!"));

        assertEquals(PacketType.Play.Server.SET_ACTION_BAR_TEXT, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getText());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetActionBarTextPacket w = new WrappedClientboundSetActionBarTextPacket();

        assertEquals(PacketType.Play.Server.SET_ACTION_BAR_TEXT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetActionBarTextPacket source = new WrappedClientboundSetActionBarTextPacket(WrappedChatComponent.fromText("Hello, world!"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetActionBarTextPacket wrapper = new WrappedClientboundSetActionBarTextPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getText());

        wrapper.setText(WrappedChatComponent.fromText("Modified"));

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getText());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getText());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetActionBarTextPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
