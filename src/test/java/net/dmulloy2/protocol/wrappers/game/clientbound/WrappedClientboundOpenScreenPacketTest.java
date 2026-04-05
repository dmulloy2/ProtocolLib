package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundOpenScreenPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundOpenScreenPacket w = new WrappedClientboundOpenScreenPacket(3, WrappedChatComponent.fromText("Testing"));

        assertEquals(PacketType.Play.Server.OPEN_WINDOW, w.getHandle().getType());

        assertEquals(3, w.getContainerId());
        assertEquals(WrappedChatComponent.fromText("Testing"), w.getTitle());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundOpenScreenPacket w = new WrappedClientboundOpenScreenPacket();

        assertEquals(PacketType.Play.Server.OPEN_WINDOW, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundOpenScreenPacket source = new WrappedClientboundOpenScreenPacket(3, WrappedChatComponent.fromText("Testing"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundOpenScreenPacket wrapper = new WrappedClientboundOpenScreenPacket(container);

        assertEquals(3, wrapper.getContainerId());
        assertEquals(WrappedChatComponent.fromText("Testing"), wrapper.getTitle());

        wrapper.setContainerId(9);
        wrapper.setTitle(WrappedChatComponent.fromText("Modified"));

        assertEquals(9, wrapper.getContainerId());
        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getTitle());

        assertEquals(9, source.getContainerId());
        assertEquals(WrappedChatComponent.fromText("Modified"), source.getTitle());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundOpenScreenPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
