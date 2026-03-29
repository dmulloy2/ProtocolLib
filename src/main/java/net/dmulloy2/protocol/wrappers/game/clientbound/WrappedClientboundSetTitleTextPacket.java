package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetTitleTextPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code WrappedChatComponent title} – title text to display</li>
 * </ul>
 */
public class WrappedClientboundSetTitleTextPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_TITLE_TEXT;

    public WrappedClientboundSetTitleTextPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundSetTitleTextPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getTitle() {
        return handle.getChatComponents().read(0);
    }

    public void setTitle(WrappedChatComponent title) {
        handle.getChatComponents().write(0, title);
    }
}
