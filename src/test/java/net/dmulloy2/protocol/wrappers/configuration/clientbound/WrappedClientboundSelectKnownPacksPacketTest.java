package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSelectKnownPacksPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Configuration.Server.SELECT_KNOWN_PACKS, new WrappedClientboundSelectKnownPacksPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSelectKnownPacksPacket w = new WrappedClientboundSelectKnownPacksPacket();
        assertEquals(PacketType.Configuration.Server.SELECT_KNOWN_PACKS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.SELECT_KNOWN_PACKS);
        WrappedClientboundSelectKnownPacksPacket wrapper = new WrappedClientboundSelectKnownPacksPacket(container);
        assertEquals(PacketType.Configuration.Server.SELECT_KNOWN_PACKS, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSelectKnownPacksPacket(new PacketContainer(PacketType.Configuration.Server.KEEP_ALIVE)));
    }
}
