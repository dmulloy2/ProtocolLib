package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
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

        assertEquals(PacketType.Play.Server.SET_TITLE_TEXT, w.getHandle().getType());

        ClientboundSetTitleTextPacket p = (ClientboundSetTitleTextPacket) w.getHandle().getHandle();

        assertNotNull(p.text());
        assertTrue(w.getTitle().getJson().contains("My Title"));
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetTitleTextPacket nmsPacket = new ClientboundSetTitleTextPacket(
                Component.literal("Hello Title")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTitleTextPacket wrapper = new WrappedClientboundSetTitleTextPacket(container);

        assertTrue(wrapper.getTitle().getJson().contains("Hello Title"));
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetTitleTextPacket nmsPacket = new ClientboundSetTitleTextPacket(
                Component.literal("original")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTitleTextPacket wrapper = new WrappedClientboundSetTitleTextPacket(container);

        wrapper.setTitle(WrappedChatComponent.fromText("updated title"));

        assertTrue(wrapper.getTitle().getJson().contains("updated title"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetTitleTextPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
