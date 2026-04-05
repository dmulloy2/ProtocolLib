package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundTrackedWaypointPacket} (game phase, clientbound).
 * Tracks a waypoint on the client. Fields have no ProtocolLib accessor.
 */
public class WrappedClientboundTrackedWaypointPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TRACKED_WAYPOINT;

    public WrappedClientboundTrackedWaypointPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTrackedWaypointPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
