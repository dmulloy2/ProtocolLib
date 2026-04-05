package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Converters;
import java.util.List;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCustomChatCompletionsPacket} (game phase, clientbound).
 *
 * <p>NMS field order: {@code action (global 0), entries (global 1)}
 */
public class WrappedClientboundCustomChatCompletionsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CUSTOM_CHAT_COMPLETIONS;

    /**
     * Mirrors {@code ClientboundCustomChatCompletionsPacket.Action}.
     * Global field index 0 — constants must match NMS names exactly.
     */
    public enum Action { ADD, REMOVE, SET }

    public WrappedClientboundCustomChatCompletionsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCustomChatCompletionsPacket(Action action, List<String> entries) {
        this();
        setAction(action);
        setEntries(entries);
    }

    public WrappedClientboundCustomChatCompletionsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the completion action (ADD, REMOVE, or SET). Global field index 0. */
    public Action getAction() {
        return handle.getEnumModifier(Action.class, 0).read(0);
    }

    public void setAction(Action action) {
        handle.getEnumModifier(Action.class, 0).write(0, action);
    }

    public List<String> getEntries() {
        return handle.getLists(Converters.passthrough(String.class)).read(0);
    }

    public void setEntries(List<String> entries) {
        handle.getLists(Converters.passthrough(String.class)).write(0, entries);
    }
}
