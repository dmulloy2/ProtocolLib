package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundLowDiskSpaceWarningPacket} (game phase, clientbound).
 *
 * <p>This is an empty packet with no fields. It warns the client that the server is low on disk space.
 */
public class WrappedClientboundLowDiskSpaceWarningPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.LOW_DISK_SPACE_WARNING;

    public WrappedClientboundLowDiskSpaceWarningPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundLowDiskSpaceWarningPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}

