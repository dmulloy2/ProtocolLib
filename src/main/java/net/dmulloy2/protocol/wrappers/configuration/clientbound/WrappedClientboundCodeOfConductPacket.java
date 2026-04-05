package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCodeOfConductPacket} (configuration phase, clientbound).
 */
public class WrappedClientboundCodeOfConductPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.CODE_OF_CONDUCT;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(String.class);

    public WrappedClientboundCodeOfConductPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCodeOfConductPacket(String codeOfConduct) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(codeOfConduct)));
    }

    public WrappedClientboundCodeOfConductPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getCodeOfConduct() {
        return handle.getStrings().read(0);
    }

    public void setCodeOfConduct(String codeOfConduct) {
        handle.getStrings().write(0, codeOfConduct);
    }
}
