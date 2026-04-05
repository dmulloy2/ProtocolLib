package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSelectKnownPacksPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Configuration.Client.SELECT_KNOWN_PACKS, new WrappedServerboundSelectKnownPacksPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSelectKnownPacksPacket w = new WrappedServerboundSelectKnownPacksPacket();
        assertEquals(PacketType.Configuration.Client.SELECT_KNOWN_PACKS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Client.SELECT_KNOWN_PACKS);
        WrappedServerboundSelectKnownPacksPacket wrapper = new WrappedServerboundSelectKnownPacksPacket(container);
        assertEquals(PacketType.Configuration.Client.SELECT_KNOWN_PACKS, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSelectKnownPacksPacket(new PacketContainer(PacketType.Configuration.Client.KEEP_ALIVE)));
    }
}
