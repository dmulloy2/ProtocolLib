package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundResetChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Configuration.Server.RESET_CHAT, new WrappedClientboundResetChatPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundResetChatPacket w = new WrappedClientboundResetChatPacket();

        assertEquals(PacketType.Configuration.Server.RESET_CHAT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.RESET_CHAT);
        WrappedClientboundResetChatPacket wrapper = new WrappedClientboundResetChatPacket(container);

        assertEquals(PacketType.Configuration.Server.RESET_CHAT, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundResetChatPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
