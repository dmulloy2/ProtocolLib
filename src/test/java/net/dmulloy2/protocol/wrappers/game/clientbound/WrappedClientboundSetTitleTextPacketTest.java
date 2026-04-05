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
    void testAllArgsCreate() {
        WrappedClientboundSetTitleTextPacket w = new WrappedClientboundSetTitleTextPacket(WrappedChatComponent.fromText("Hello, world!"));

        assertEquals(PacketType.Play.Server.SET_TITLE_TEXT, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getTitle());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetTitleTextPacket w = new WrappedClientboundSetTitleTextPacket();

        assertEquals(PacketType.Play.Server.SET_TITLE_TEXT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetTitleTextPacket source = new WrappedClientboundSetTitleTextPacket(WrappedChatComponent.fromText("Hello, world!"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTitleTextPacket wrapper = new WrappedClientboundSetTitleTextPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getTitle());

        wrapper.setTitle(WrappedChatComponent.fromText("Modified"));

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getTitle());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getTitle());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetTitleTextPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
