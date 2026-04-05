package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLoginDisconnectPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundLoginDisconnectPacket w = new WrappedClientboundLoginDisconnectPacket(WrappedChatComponent.fromText("Hello, world!"));

        assertEquals(PacketType.Login.Server.DISCONNECT, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getReason());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLoginDisconnectPacket w = new WrappedClientboundLoginDisconnectPacket();

        assertEquals(PacketType.Login.Server.DISCONNECT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundLoginDisconnectPacket source = new WrappedClientboundLoginDisconnectPacket(WrappedChatComponent.fromText("Hello, world!"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLoginDisconnectPacket wrapper = new WrappedClientboundLoginDisconnectPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getReason());

        wrapper.setReason(WrappedChatComponent.fromText("Modified"));

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getReason());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getReason());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLoginDisconnectPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
