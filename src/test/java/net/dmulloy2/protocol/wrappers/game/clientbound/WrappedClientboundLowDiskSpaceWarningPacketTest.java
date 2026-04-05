package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLowDiskSpaceWarningPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Server.LOW_DISK_SPACE_WARNING, new WrappedClientboundLowDiskSpaceWarningPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLowDiskSpaceWarningPacket w = new WrappedClientboundLowDiskSpaceWarningPacket();
        assertEquals(PacketType.Play.Server.LOW_DISK_SPACE_WARNING, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.LOW_DISK_SPACE_WARNING);
        WrappedClientboundLowDiskSpaceWarningPacket wrapper = new WrappedClientboundLowDiskSpaceWarningPacket(container);
        assertEquals(PacketType.Play.Server.LOW_DISK_SPACE_WARNING, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLowDiskSpaceWarningPacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
