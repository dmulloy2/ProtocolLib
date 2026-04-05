package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCustomPayloadPacket} (configuration phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code CustomPacketPayload payload} – channel-specific payload</li>
 * </ul>
 */
public class WrappedClientboundCustomPayloadPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.CUSTOM_PAYLOAD;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(CustomPacketPayloadWrapper.getCustomPacketPayloadClass(), CustomPacketPayloadWrapper.getConverter());

    public WrappedClientboundCustomPayloadPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCustomPayloadPacket(CustomPacketPayloadWrapper payload) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(payload)));
    }

    public WrappedClientboundCustomPayloadPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public CustomPacketPayloadWrapper getPayload() {
        return handle.getCustomPacketPayloads().read(0);
    }

    public void setPayload(CustomPacketPayloadWrapper payload) {
        handle.getCustomPacketPayloads().write(0, payload);
    }
}
