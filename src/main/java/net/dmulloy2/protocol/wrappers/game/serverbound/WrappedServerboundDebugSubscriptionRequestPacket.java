package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundDebugSubscriptionRequestPacket} (game phase, serverbound).
 * Requests debug subscriptions. The subscriptions field has no ProtocolLib accessor.
 */
public class WrappedServerboundDebugSubscriptionRequestPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.DEBUG_SUBSCRIPTION_REQUEST;

    public WrappedServerboundDebugSubscriptionRequestPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundDebugSubscriptionRequestPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'subscriptions' (NMS type: Set<DebugSubscription<?>> — registry-backed set)
    //   No standard ProtocolLib accessor exists. Use handle.getModifier().read(0) for the raw Set,
    //   or add a getSets() call once a DebugSubscription converter is registered.
}
