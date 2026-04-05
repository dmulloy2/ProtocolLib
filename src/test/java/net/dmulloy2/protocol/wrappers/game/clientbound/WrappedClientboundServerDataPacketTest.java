package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundServerDataPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundServerDataPacket w = new WrappedClientboundServerDataPacket(WrappedChatComponent.fromText("Hello, world!"), Optional.empty());

        assertEquals(PacketType.Play.Server.SERVER_DATA, w.getHandle().getType());

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getMotd());
        assertEquals(Optional.empty(), w.getIconBytes());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundServerDataPacket w = new WrappedClientboundServerDataPacket();

        assertEquals(PacketType.Play.Server.SERVER_DATA, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundServerDataPacket source = new WrappedClientboundServerDataPacket(WrappedChatComponent.fromText("Hello, world!"), Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundServerDataPacket wrapper = new WrappedClientboundServerDataPacket(container);

        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getMotd());
        assertEquals(Optional.empty(), wrapper.getIconBytes());

        wrapper.setMotd(WrappedChatComponent.fromText("Modified"));
        wrapper.setIconBytes(Optional.empty());

        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getMotd());
        assertEquals(Optional.empty(), wrapper.getIconBytes());

        assertEquals(WrappedChatComponent.fromText("Modified"), source.getMotd());
        assertEquals(Optional.empty(), source.getIconBytes());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundServerDataPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
