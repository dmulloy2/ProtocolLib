package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTrackedWaypointPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Server.TRACKED_WAYPOINT, new WrappedClientboundTrackedWaypointPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTrackedWaypointPacket w = new WrappedClientboundTrackedWaypointPacket();
        assertEquals(PacketType.Play.Server.TRACKED_WAYPOINT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.TRACKED_WAYPOINT);
        WrappedClientboundTrackedWaypointPacket wrapper = new WrappedClientboundTrackedWaypointPacket(container);
        assertEquals(PacketType.Play.Server.TRACKED_WAYPOINT, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTrackedWaypointPacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
