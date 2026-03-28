package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetActionBarTextPacket} (Play phase, clientbound).
 *
 * <p>Displays text in the action bar (above the hotbar).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Component text} – the message to display</li>
 * </ul>
 */
public class WrapperGameClientboundSetActionBarText extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_ACTION_BAR_TEXT;

    public WrapperGameClientboundSetActionBarText() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundSetActionBarText(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getText() {
        return handle.getChatComponents().read(0);
    }

    public void setText(WrappedChatComponent text) {
        handle.getChatComponents().write(0, text);
    }
}
