package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTestInstanceBlockStatusPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundTestInstanceBlockStatusPacket w = new WrappedClientboundTestInstanceBlockStatusPacket(
                WrappedChatComponent.fromText("Running"),
                Optional.of(new BlockPosition(3, 4, 5)));

        assertEquals(PacketType.Play.Server.TEST_INSTANCE_BLOCK_STATUS, w.getHandle().getType());
        assertEquals(WrappedChatComponent.fromText("Running"), w.getStatus());
        assertTrue(w.getSize().isPresent());
        assertEquals(new BlockPosition(3, 4, 5), w.getSize().get());
    }

    @Test
    void testAllArgsCreateWithEmptySize() {
        WrappedClientboundTestInstanceBlockStatusPacket w = new WrappedClientboundTestInstanceBlockStatusPacket(
                WrappedChatComponent.fromText("Idle"), Optional.empty());

        assertEquals(WrappedChatComponent.fromText("Idle"), w.getStatus());
        assertFalse(w.getSize().isPresent());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTestInstanceBlockStatusPacket w = new WrappedClientboundTestInstanceBlockStatusPacket();
        assertEquals(PacketType.Play.Server.TEST_INSTANCE_BLOCK_STATUS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.TEST_INSTANCE_BLOCK_STATUS);
        WrappedClientboundTestInstanceBlockStatusPacket wrapper = new WrappedClientboundTestInstanceBlockStatusPacket(container);
        wrapper.setStatus(WrappedChatComponent.fromText("Hello, world!"));
        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getStatus());
    }

    @Test
    void testSetAndGetStatus() {
        WrappedClientboundTestInstanceBlockStatusPacket w = new WrappedClientboundTestInstanceBlockStatusPacket();
        w.setStatus(WrappedChatComponent.fromText("Test passed!"));
        assertEquals(WrappedChatComponent.fromText("Test passed!"), w.getStatus());
    }

    @Test
    void testSetAndGetSize() {
        WrappedClientboundTestInstanceBlockStatusPacket w = new WrappedClientboundTestInstanceBlockStatusPacket();
        w.setSize(Optional.of(new BlockPosition(5, 5, 5)));

        Optional<BlockPosition> size = w.getSize();
        assertTrue(size.isPresent());
        assertEquals(new BlockPosition(5, 5, 5), size.get());
    }

    @Test
    void testSetEmptySize() {
        WrappedClientboundTestInstanceBlockStatusPacket w = new WrappedClientboundTestInstanceBlockStatusPacket();
        w.setSize(Optional.empty());
        Optional<BlockPosition> size = w.getSize();
        assertFalse(size.isPresent());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTestInstanceBlockStatusPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
