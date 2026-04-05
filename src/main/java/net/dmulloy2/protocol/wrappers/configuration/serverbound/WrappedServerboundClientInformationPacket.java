package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundClientInformationPacket} (configuration phase, serverbound).
 */
public class WrappedServerboundClientInformationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.CLIENT_INFORMATION;

    public WrappedServerboundClientInformationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundClientInformationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'information' (NMS type: ClientInformation — record with locale, viewDistance, chatMode, etc.)
    //   ClientInformation holds: String locale, int viewDistance, ChatVisiblity chatVisibility,
    //   boolean chatColors, int modelCustomisation, HumanoidArm mainHand, boolean textFilteringEnabled,
    //   boolean allowsListing, ParticleStatus particleStatus.
    //   Use AutoWrapper with a dedicated WrappedClientInformation POJO, or expose individual fields
    //   via getStrings().read(0), getIntegers().read(0), etc. in declaration order.
}
