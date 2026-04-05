package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDebugEntityValuePacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        // No all-args constructor: update field has no ProtocolLib accessor
        assertEquals(PacketType.Play.Server.DEBUG_ENTITY_VALUE, new WrappedClientboundDebugEntityValuePacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDebugEntityValuePacket w = new WrappedClientboundDebugEntityValuePacket();
        assertEquals(PacketType.Play.Server.DEBUG_ENTITY_VALUE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.DEBUG_ENTITY_VALUE);
        WrappedClientboundDebugEntityValuePacket wrapper = new WrappedClientboundDebugEntityValuePacket(container);
        wrapper.setEntityId(42);
        assertEquals(42, wrapper.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDebugEntityValuePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
