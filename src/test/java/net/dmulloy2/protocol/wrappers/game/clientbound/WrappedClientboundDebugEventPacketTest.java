package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDebugEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Server.DEBUG_EVENT, new WrappedClientboundDebugEventPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDebugEventPacket w = new WrappedClientboundDebugEventPacket();
        assertEquals(PacketType.Play.Server.DEBUG_EVENT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.DEBUG_EVENT);
        WrappedClientboundDebugEventPacket wrapper = new WrappedClientboundDebugEventPacket(container);
        assertEquals(PacketType.Play.Server.DEBUG_EVENT, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDebugEventPacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
