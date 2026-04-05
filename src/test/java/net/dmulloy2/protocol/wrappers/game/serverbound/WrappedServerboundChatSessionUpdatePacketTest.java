package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedRemoteChatSessionData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChatSessionUpdatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundChatSessionUpdatePacket w = new WrappedServerboundChatSessionUpdatePacket((WrappedRemoteChatSessionData) null);

        assertEquals(PacketType.Play.Client.CHAT_SESSION_UPDATE, w.getHandle().getType());

        assertEquals(null, w.getChatSession());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChatSessionUpdatePacket w = new WrappedServerboundChatSessionUpdatePacket();

        assertEquals(PacketType.Play.Client.CHAT_SESSION_UPDATE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundChatSessionUpdatePacket source = new WrappedServerboundChatSessionUpdatePacket((WrappedRemoteChatSessionData) null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatSessionUpdatePacket wrapper = new WrappedServerboundChatSessionUpdatePacket(container);

        assertEquals(null, wrapper.getChatSession());

        wrapper.setChatSession(null);

        assertEquals(null, wrapper.getChatSession());

        assertEquals(null, source.getChatSession());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatSessionUpdatePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
