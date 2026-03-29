package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
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

        assertEquals(PacketType.Play.Server.SET_SUBTITLE_TEXT, w.getHandle().getType());

        ClientboundSetSubtitleTextPacket p = (ClientboundSetSubtitleTextPacket) w.getHandle().getHandle();

        assertNotNull(p.text());
        assertTrue(w.getSubtitle().getJson().contains("My Subtitle"));
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetSubtitleTextPacket nmsPacket = new ClientboundSetSubtitleTextPacket(
                Component.literal("Hello Sub")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetSubtitleTextPacket wrapper = new WrappedClientboundSetSubtitleTextPacket(container);

        assertTrue(wrapper.getSubtitle().getJson().contains("Hello Sub"));
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetSubtitleTextPacket nmsPacket = new ClientboundSetSubtitleTextPacket(
                Component.literal("original")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetSubtitleTextPacket wrapper = new WrappedClientboundSetSubtitleTextPacket(container);

        wrapper.setSubtitle(WrappedChatComponent.fromText("updated sub"));

        assertTrue(wrapper.getSubtitle().getJson().contains("updated sub"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetSubtitleTextPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
