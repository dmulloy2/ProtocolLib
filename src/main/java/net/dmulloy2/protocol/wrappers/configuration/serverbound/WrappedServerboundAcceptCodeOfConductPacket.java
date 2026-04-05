package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundAcceptCodeOfConductPacket} (configuration phase, serverbound).
 * This is an empty packet accepting the code of conduct.
 */
public class WrappedServerboundAcceptCodeOfConductPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.ACCEPT_CODE_OF_CONDUCT;

    public WrappedServerboundAcceptCodeOfConductPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundAcceptCodeOfConductPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
