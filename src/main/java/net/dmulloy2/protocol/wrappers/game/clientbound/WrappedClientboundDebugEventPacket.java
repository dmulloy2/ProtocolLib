package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDebugEventPacket} (game phase, clientbound).
 * Sends a debug event. The event field has no ProtocolLib accessor.
 */
public class WrappedClientboundDebugEventPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DEBUG_EVENT;

    public WrappedClientboundDebugEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDebugEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'event' (NMS type: DebugSubscription.Event<?> — registry-backed debug event)
    //   No ProtocolLib accessor exists. Use handle.getModifier().read(0) for the raw event object.
}
