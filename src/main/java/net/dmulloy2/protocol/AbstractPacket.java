package net.dmulloy2.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

/**
 * Base class for all packet wrappers. Provides access to the underlying
 * {@link PacketContainer} and convenience methods for sending/receiving.
 */
public abstract class AbstractPacket {

    protected final PacketContainer handle;

    protected AbstractPacket(PacketContainer handle, PacketType type) {
        if (handle == null) {
            throw new IllegalArgumentException("Packet handle cannot be null");
        }
        if (!handle.getType().equals(type)) {
            throw new IllegalArgumentException(
                    "Invalid packet type: expected " + type + ", got " + handle.getType());
        }
        this.handle = handle;
    }

    /**
     * Returns the underlying {@link PacketContainer}.
     */
    public PacketContainer getHandle() {
        return handle;
    }

    /**
     * Sends this packet to a player (server → client).
     */
    public void sendPacket(Player receiver) {
        ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, getHandle());
    }

    /**
     * Simulates receiving this packet from a player (client → server).
     */
    public void receivePacket(Player player) {
        ProtocolLibrary.getProtocolManager().receiveClientPacket(player, getHandle());
    }
}
