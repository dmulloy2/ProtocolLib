package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDisconnectPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundDisconnectPacket w = new WrappedClientboundDisconnectPacket(WrappedChatComponent.fromText("Hello, world!"));

        assertEquals(PacketType.Play.Server.KICK_DISCONNECT, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getReason());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDisconnectPacket w = new WrappedClientboundDisconnectPacket();

        assertEquals(PacketType.Play.Server.KICK_DISCONNECT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundDisconnectPacket source = new WrappedClientboundDisconnectPacket(WrappedChatComponent.fromText("Hello, world!"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedClientboundDisconnectPacket.TYPE, nmsPacket);
        WrappedClientboundDisconnectPacket wrapper = new WrappedClientboundDisconnectPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getReason());

        wrapper.setReason(WrappedChatComponent.fromText("Modified"));

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getReason());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getReason());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDisconnectPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
