package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
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

        assertEquals(PacketType.Play.Server.SYSTEM_CHAT, w.getHandle().getType());

        ClientboundSystemChatPacket p = (ClientboundSystemChatPacket) w.getHandle().getHandle();

        assertNotNull(p.content());
        assertFalse(p.overlay());
        assertTrue(w.getContent().getJson().contains("System message"));
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSystemChatPacket nmsPacket = new ClientboundSystemChatPacket(
                Component.literal("Test"), true
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSystemChatPacket wrapper = new WrappedClientboundSystemChatPacket(container);

        assertTrue(wrapper.getContent().getJson().contains("Test"));
        assertTrue(wrapper.isOverlay());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSystemChatPacket nmsPacket = new ClientboundSystemChatPacket(
                Component.literal("original"), false
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSystemChatPacket wrapper = new WrappedClientboundSystemChatPacket(container);

        wrapper.setOverlay(true);

        assertTrue(wrapper.getContent().getJson().contains("original"));
        assertTrue(wrapper.isOverlay());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSystemChatPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
