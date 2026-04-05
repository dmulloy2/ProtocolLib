package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTestInstanceBlockStatusPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        // No full all-args constructor: size field has no ProtocolLib accessor
        assertEquals(PacketType.Play.Server.TEST_INSTANCE_BLOCK_STATUS, new WrappedClientboundTestInstanceBlockStatusPacket().getHandle().getType());
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
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTestInstanceBlockStatusPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
