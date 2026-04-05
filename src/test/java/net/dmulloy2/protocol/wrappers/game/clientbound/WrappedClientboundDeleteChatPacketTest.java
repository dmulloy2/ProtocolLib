package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDeleteChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundDeleteChatPacket w = new WrappedClientboundDeleteChatPacket((WrappedMessageSignature) null);

        assertEquals(PacketType.Play.Server.DELETE_CHAT_MESSAGE, w.getHandle().getType());

        assertEquals(null, w.getMessageSignature());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDeleteChatPacket w = new WrappedClientboundDeleteChatPacket();

        assertEquals(PacketType.Play.Server.DELETE_CHAT_MESSAGE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundDeleteChatPacket source = new WrappedClientboundDeleteChatPacket((WrappedMessageSignature) null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundDeleteChatPacket wrapper = new WrappedClientboundDeleteChatPacket(container);

        assertEquals(null, wrapper.getMessageSignature());

        wrapper.setMessageSignature(null);

        assertEquals(null, wrapper.getMessageSignature());

        assertEquals(null, source.getMessageSignature());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDeleteChatPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
