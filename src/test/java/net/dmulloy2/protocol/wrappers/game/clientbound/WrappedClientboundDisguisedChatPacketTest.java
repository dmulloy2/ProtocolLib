package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDisguisedChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundDisguisedChatPacket w = new WrappedClientboundDisguisedChatPacket(WrappedChatComponent.fromText("Hello, world!"));

        assertEquals(PacketType.Play.Server.DISGUISED_CHAT, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getMessage());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDisguisedChatPacket w = new WrappedClientboundDisguisedChatPacket();

        assertEquals(PacketType.Play.Server.DISGUISED_CHAT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundDisguisedChatPacket source = new WrappedClientboundDisguisedChatPacket(WrappedChatComponent.fromText("Hello, world!"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundDisguisedChatPacket wrapper = new WrappedClientboundDisguisedChatPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getMessage());

        wrapper.setMessage(WrappedChatComponent.fromText("Modified"));

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getMessage());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getMessage());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDisguisedChatPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
