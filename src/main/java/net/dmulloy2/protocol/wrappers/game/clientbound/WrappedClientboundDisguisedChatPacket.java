package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDisguisedChatPacket} (game phase, clientbound).
 */
public class WrappedClientboundDisguisedChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DISGUISED_CHAT;

    public WrappedClientboundDisguisedChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDisguisedChatPacket(WrappedChatComponent message) {
        this();
        setMessage(message);
    }

    public WrappedClientboundDisguisedChatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'chatType' (NMS type: ChatType.Bound — holds ResourceKey<ChatType> + Component name + optional Component targetName)
    //   No dedicated ProtocolLib accessor exists for ChatType.Bound.
    //   Use handle.getModifier().read(1) for the raw ChatType.Bound object.

    public WrappedChatComponent getMessage() {
        return handle.getChatComponents().read(0);
    }

    public void setMessage(WrappedChatComponent message) {
        handle.getChatComponents().write(0, message);
    }
}
