package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedKnownPack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSelectKnownPacksPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        List<WrappedKnownPack> knownPacks = List.of(
                new WrappedKnownPack("minecraft", "core", "1.21"),
                new WrappedKnownPack("example", "extras", "2"));

        WrappedServerboundSelectKnownPacksPacket wrapper = new WrappedServerboundSelectKnownPacksPacket(knownPacks);

        assertEquals(PacketType.Configuration.Client.SELECT_KNOWN_PACKS, wrapper.getHandle().getType());
        assertEquals(knownPacks, wrapper.getKnownPacks());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSelectKnownPacksPacket w = new WrappedServerboundSelectKnownPacksPacket();
        assertEquals(PacketType.Configuration.Client.SELECT_KNOWN_PACKS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new WrappedServerboundSelectKnownPacksPacket(List.of()).getHandle();
        WrappedServerboundSelectKnownPacksPacket wrapper = new WrappedServerboundSelectKnownPacksPacket(container);
        List<WrappedKnownPack> knownPacks = List.of(new WrappedKnownPack("minecraft", "vanilla", "1.21.6"));

        wrapper.setKnownPacks(knownPacks);

        assertEquals(PacketType.Configuration.Client.SELECT_KNOWN_PACKS, wrapper.getHandle().getType());
        assertEquals(knownPacks, wrapper.getKnownPacks());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSelectKnownPacksPacket(new PacketContainer(PacketType.Configuration.Client.KEEP_ALIVE)));
    }
}
