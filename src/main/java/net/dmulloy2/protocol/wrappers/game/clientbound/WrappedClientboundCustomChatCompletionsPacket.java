package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Converters;
import java.util.List;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCustomChatCompletionsPacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Action action} – ADD, REMOVE, or SET</li>
 *   <li>{@code List<String> entries} – completion strings to add/remove/set</li>
 * </ul>
 */
public class WrappedClientboundCustomChatCompletionsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS;

    public WrappedClientboundCustomChatCompletionsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCustomChatCompletionsPacket(List<String> entries) {
        this();
        setEntries(entries);
    }

    public WrappedClientboundCustomChatCompletionsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public List<String> getEntries() {
        return handle.getLists(Converters.passthrough(String.class)).read(0);
    }

    public void setEntries(List<String> entries) {
        handle.getLists(Converters.passthrough(String.class)).write(0, entries);
    }
}
