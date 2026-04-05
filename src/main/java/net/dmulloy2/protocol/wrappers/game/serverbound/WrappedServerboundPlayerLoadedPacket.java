package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPlayerLoadedPacket} (game phase, serverbound).
 * This is an empty packet with no fields sent when the player is loaded.
 */
public class WrappedServerboundPlayerLoadedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.PLAYER_LOADED;

    public WrappedServerboundPlayerLoadedPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPlayerLoadedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
