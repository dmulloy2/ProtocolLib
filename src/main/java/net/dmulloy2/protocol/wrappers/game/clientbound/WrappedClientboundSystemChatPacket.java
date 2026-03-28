package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSystemChatPacket} (Play phase, clientbound).
 *
 * <p>Sends a system-level chat message to the client.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Component content} – the message text</li>
 *   <li>{@code boolean overlay} – if {@code true}, display as action bar instead of chat</li>
 * </ul>
 */
public class WrappedClientboundSystemChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SYSTEM_CHAT;

    public WrappedClientboundSystemChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundSystemChatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getContent() {
        return handle.getChatComponents().read(0);
    }

    public void setContent(WrappedChatComponent content) {
        handle.getChatComponents().write(0, content);
    }

    /** Returns {@code true} if the message is shown in the action bar rather than chat. */
    public boolean isOverlay() {
        return handle.getBooleans().read(0);
    }

    public void setOverlay(boolean overlay) {
        handle.getBooleans().write(0, overlay);
    }
}
