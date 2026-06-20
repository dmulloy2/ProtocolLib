package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedKnownPack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSelectKnownPacksPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        List<WrappedKnownPack> knownPacks = List.of(
                new WrappedKnownPack("minecraft", "core", "1.21"),
                new WrappedKnownPack("example", "extras", "2"));

        WrappedClientboundSelectKnownPacksPacket wrapper = new WrappedClientboundSelectKnownPacksPacket(knownPacks);

        assertEquals(PacketType.Configuration.Server.SELECT_KNOWN_PACKS, wrapper.getHandle().getType());
        assertEquals(knownPacks, wrapper.getKnownPacks());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSelectKnownPacksPacket w = new WrappedClientboundSelectKnownPacksPacket();
        assertEquals(PacketType.Configuration.Server.SELECT_KNOWN_PACKS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new WrappedClientboundSelectKnownPacksPacket(List.of()).getHandle();
        WrappedClientboundSelectKnownPacksPacket wrapper = new WrappedClientboundSelectKnownPacksPacket(container);
        List<WrappedKnownPack> knownPacks = List.of(new WrappedKnownPack("minecraft", "vanilla", "1.21.6"));

        wrapper.setKnownPacks(knownPacks);

        assertEquals(PacketType.Configuration.Server.SELECT_KNOWN_PACKS, wrapper.getHandle().getType());
        assertEquals(knownPacks, wrapper.getKnownPacks());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSelectKnownPacksPacket(new PacketContainer(PacketType.Configuration.Server.KEEP_ALIVE)));
    }
}
