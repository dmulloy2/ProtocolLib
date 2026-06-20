package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedChatTypeBound;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.Optional;

/**
 * Wrapper for {@code ClientboundDisguisedChatPacket} (game phase, clientbound).
 */
public class WrappedClientboundDisguisedChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DISGUISED_CHAT;

    private static final Class<?> CHAT_TYPE_BOUND_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.ChatType$Bound");

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter())
            .withParam(CHAT_TYPE_BOUND_CLASS, WrappedChatTypeBound.CONVERTER);

    public WrappedClientboundDisguisedChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDisguisedChatPacket(WrappedChatComponent message) {
        this(message, new WrappedChatTypeBound(
                new com.comphenix.protocol.wrappers.MinecraftKey("chat"),
                WrappedChatComponent.fromText(""),
                Optional.empty()));
    }

    public WrappedClientboundDisguisedChatPacket(WrappedChatComponent message, WrappedChatTypeBound chatType) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(message, chatType)));
    }

    public WrappedClientboundDisguisedChatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getMessage() {
        return handle.getChatComponents().read(0);
    }

    public void setMessage(WrappedChatComponent message) {
        handle.getChatComponents().write(0, message);
    }

    public WrappedChatTypeBound getChatType() {
        return handle.getModifier().withType(CHAT_TYPE_BOUND_CLASS, WrappedChatTypeBound.CONVERTER).read(0);
    }

    public void setChatType(WrappedChatTypeBound chatType) {
        handle.getModifier().withType(CHAT_TYPE_BOUND_CLASS, WrappedChatTypeBound.CONVERTER).write(0, chatType);
    }
}
