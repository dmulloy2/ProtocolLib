package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTabListPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundTabListPacket w = new WrappedClientboundTabListPacket();
        w.setHeader(WrappedChatComponent.fromText("Header text"));
        w.setFooter(WrappedChatComponent.fromText("Footer text"));

        assertEquals(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER, w.getHandle().getType());

        ClientboundTabListPacket p = (ClientboundTabListPacket) w.getHandle().getHandle();

        assertNotNull(p.header());
        assertNotNull(p.footer());
        assertTrue(w.getHeader().getJson().contains("Header text"));
        assertTrue(w.getFooter().getJson().contains("Footer text"));
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundTabListPacket nmsPacket = new ClientboundTabListPacket(
                Component.literal("Top"), Component.literal("Bottom")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTabListPacket wrapper = new WrappedClientboundTabListPacket(container);

        assertTrue(wrapper.getHeader().getJson().contains("Top"));
        assertTrue(wrapper.getFooter().getJson().contains("Bottom"));
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTabListPacket nmsPacket = new ClientboundTabListPacket(
                Component.literal("old header"), Component.literal("old footer")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTabListPacket wrapper = new WrappedClientboundTabListPacket(container);

        wrapper.setHeader(WrappedChatComponent.fromText("new header"));

        assertTrue(wrapper.getHeader().getJson().contains("new header"));
        assertTrue(wrapper.getFooter().getJson().contains("old footer"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTabListPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
