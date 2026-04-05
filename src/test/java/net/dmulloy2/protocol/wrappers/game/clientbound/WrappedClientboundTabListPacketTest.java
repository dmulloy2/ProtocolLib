package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTabListPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundTabListPacket w = new WrappedClientboundTabListPacket(WrappedChatComponent.fromText("Hello, world!"), WrappedChatComponent.fromText("Testing"));

        assertEquals(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getHeader());
        assertEquals(WrappedChatComponent.fromText("Testing"), w.getFooter());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTabListPacket w = new WrappedClientboundTabListPacket();

        assertEquals(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundTabListPacket source = new WrappedClientboundTabListPacket(WrappedChatComponent.fromText("Hello, world!"), WrappedChatComponent.fromText("Testing"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTabListPacket wrapper = new WrappedClientboundTabListPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getHeader());
        assertEquals(WrappedChatComponent.fromText("Testing"), wrapper.getFooter());

        wrapper.setHeader(WrappedChatComponent.fromText("Modified"));
        wrapper.setFooter(WrappedChatComponent.fromText("Modified"));

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getHeader());
        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getFooter());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getHeader());
        assertEquals(WrappedChatComponent.fromText("Modified"), source.getFooter());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTabListPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
