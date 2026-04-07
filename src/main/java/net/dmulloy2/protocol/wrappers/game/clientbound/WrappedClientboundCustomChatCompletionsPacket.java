package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.Arrays;
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

    private static final Class<?> ACTION_NMS_CLASS = Arrays.stream(TYPE.getPacketClass().getDeclaredClasses())
            .filter(Class::isEnum)
            .findFirst()
            .orElse(null);

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(ACTION_NMS_CLASS, new EnumWrappers.EnumConverter<>(ACTION_NMS_CLASS, Action.class))
            .withParam(List.class);

    public WrappedClientboundCustomChatCompletionsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCustomChatCompletionsPacket(Action action, List<String> entries) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(action, entries)));
    }

    public WrappedClientboundCustomChatCompletionsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the completion action (ADD, REMOVE, or SET). Global field index 0. */
    public Action getAction() {
        return handle.getEnumModifier(Action.class, 0).readSafely(0);
    }

    public void setAction(Action action) {
        handle.getEnumModifier(Action.class, 0).writeSafely(0, action);
    }

    public List<String> getEntries() {
        return handle.getLists(Converters.passthrough(String.class)).readSafely(0);
    }

    public void setEntries(List<String> entries) {
        handle.getLists(Converters.passthrough(String.class)).writeSafely(0, entries);
    }
}
