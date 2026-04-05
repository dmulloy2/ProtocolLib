package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundClientInformationPacket} (game phase, serverbound).
 */
public class WrappedServerboundClientInformationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SETTINGS;

    public WrappedServerboundClientInformationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundClientInformationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'information' (NMS type: ClientInformation — see configuration-phase wrapper for field list)
    //   Same as the configuration-phase ClientInformation packet. See WrappedServerboundClientInformationPacket
    //   in configuration/serverbound for implementation guidance.
}
