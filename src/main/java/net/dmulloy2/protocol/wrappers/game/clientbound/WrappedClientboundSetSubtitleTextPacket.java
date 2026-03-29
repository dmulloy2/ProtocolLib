package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetSubtitleTextPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code WrappedChatComponent subtitle} – subtitle text to display</li>
 * </ul>
 */
public class WrappedClientboundSetSubtitleTextPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_SUBTITLE_TEXT;

    public WrappedClientboundSetSubtitleTextPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundSetSubtitleTextPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getSubtitle() {
        return handle.getChatComponents().read(0);
    }

    public void setSubtitle(WrappedChatComponent subtitle) {
        handle.getChatComponents().write(0, subtitle);
    }
}
