package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Optional;
import java.util.UUID;
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
        UUID identifier = UUID.fromString("00000000-0000-0000-0000-000000000001");
        WrappedClientboundTrackedWaypointPacket.WaypointIcon icon =
                new WrappedClientboundTrackedWaypointPacket.WaypointIcon(
                        new MinecraftKey("bowtie"),
                        Optional.of(0x336699));
        WrappedClientboundTrackedWaypointPacket.TrackedWaypoint waypoint =
                WrappedClientboundTrackedWaypointPacket.TrackedWaypoint.position(
                        identifier,
                        icon,
                        new WrappedClientboundTrackedWaypointPacket.WaypointPosition(1, 2, 3));
        WrappedClientboundTrackedWaypointPacket w = new WrappedClientboundTrackedWaypointPacket(
                WrappedClientboundTrackedWaypointPacket.Operation.TRACK,
                waypoint);

        assertEquals(PacketType.Play.Server.TRACKED_WAYPOINT, w.getHandle().getType());
        assertEquals(WrappedClientboundTrackedWaypointPacket.Operation.TRACK, w.getOperation());
        assertEquals(waypoint, w.getWaypoint());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTrackedWaypointPacket w = new WrappedClientboundTrackedWaypointPacket();
        assertEquals(PacketType.Play.Server.TRACKED_WAYPOINT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        UUID identifier = UUID.fromString("00000000-0000-0000-0000-000000000001");
        WrappedClientboundTrackedWaypointPacket source = new WrappedClientboundTrackedWaypointPacket(
                WrappedClientboundTrackedWaypointPacket.Operation.TRACK,
                WrappedClientboundTrackedWaypointPacket.TrackedWaypoint.empty(identifier));
        PacketContainer container = PacketContainer.fromPacket(source.getHandle().getHandle());
        WrappedClientboundTrackedWaypointPacket wrapper = new WrappedClientboundTrackedWaypointPacket(container);
        assertEquals(PacketType.Play.Server.TRACKED_WAYPOINT, wrapper.getHandle().getType());
        assertEquals(WrappedClientboundTrackedWaypointPacket.Operation.TRACK, wrapper.getOperation());
        assertEquals(WrappedClientboundTrackedWaypointPacket.TrackedWaypoint.empty(identifier), wrapper.getWaypoint());

        WrappedClientboundTrackedWaypointPacket.TrackedWaypoint chunkWaypoint =
                WrappedClientboundTrackedWaypointPacket.TrackedWaypoint.chunk(
                        UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"),
                        WrappedClientboundTrackedWaypointPacket.WaypointIcon.defaultIcon(),
                        new ChunkCoordIntPair(4, 5));
        wrapper.setOperation(WrappedClientboundTrackedWaypointPacket.Operation.UPDATE);
        wrapper.setWaypoint(chunkWaypoint);

        assertEquals(WrappedClientboundTrackedWaypointPacket.Operation.UPDATE, wrapper.getOperation());
        assertEquals(chunkWaypoint, wrapper.getWaypoint());
        assertEquals(WrappedClientboundTrackedWaypointPacket.Operation.UPDATE, source.getOperation());
        assertEquals(chunkWaypoint, source.getWaypoint());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTrackedWaypointPacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
