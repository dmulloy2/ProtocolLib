package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDisconnectPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code WrappedChatComponent reason} – disconnect reason shown to the player</li>
 * </ul>
 */
public class WrappedClientboundDisconnectPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.KICK_DISCONNECT;

    public WrappedClientboundDisconnectPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundDisconnectPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getReason() {
        return handle.getChatComponents().read(0);
    }

    public void setReason(WrappedChatComponent reason) {
        handle.getChatComponents().write(0, reason);
    }
}
