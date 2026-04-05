package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundTeleportToEntityPacket} (game phase, serverbound).
 *
 * <p>NMS: {@code ServerboundTeleportToEntityPacket(UUID uuid)}
 */
public class WrappedServerboundTeleportToEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SPECTATE;

    public WrappedServerboundTeleportToEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundTeleportToEntityPacket(UUID uuid) {
        this();
        setUuid(uuid);
    }

    public WrappedServerboundTeleportToEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public UUID getUuid() {
        return handle.getUUIDs().read(0);
    }

    public void setUuid(UUID uuid) {
        handle.getUUIDs().write(0, uuid);
    }
}
