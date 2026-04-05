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
    void testAllArgsCreate() {
        WrappedClientboundSetSubtitleTextPacket w = new WrappedClientboundSetSubtitleTextPacket(WrappedChatComponent.fromText("Hello, world!"));

        assertEquals(PacketType.Play.Server.SET_SUBTITLE_TEXT, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getSubtitle());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetSubtitleTextPacket w = new WrappedClientboundSetSubtitleTextPacket();

        assertEquals(PacketType.Play.Server.SET_SUBTITLE_TEXT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetSubtitleTextPacket source = new WrappedClientboundSetSubtitleTextPacket(WrappedChatComponent.fromText("Hello, world!"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetSubtitleTextPacket wrapper = new WrappedClientboundSetSubtitleTextPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getSubtitle());

        wrapper.setSubtitle(WrappedChatComponent.fromText("Modified"));

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getSubtitle());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getSubtitle());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetSubtitleTextPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
