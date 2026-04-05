package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedRemoteChatSessionData;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChatSessionUpdatePacket} (game phase, serverbound).
 *
 * <p>NMS record: {@code ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession)}
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code RemoteChatSession.Data chatSession} – the player's active chat session data
 *       (session UUID and public key profile)</li>
 * </ul>
 */
public class WrappedServerboundChatSessionUpdatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHAT_SESSION_UPDATE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getRemoteChatSessionDataClass(),
                    BukkitConverters.getWrappedRemoteChatSessionDataConverter());

    public WrappedServerboundChatSessionUpdatePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundChatSessionUpdatePacket(WrappedRemoteChatSessionData chatSession) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(chatSession)));
    }

    public WrappedServerboundChatSessionUpdatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedRemoteChatSessionData getChatSession() {
        Object raw = handle.getModifier().read(0);
        return raw != null ? BukkitConverters.getWrappedRemoteChatSessionDataConverter().getSpecific(raw) : null;
    }

    public void setChatSession(WrappedRemoteChatSessionData chatSession) {
        handle.getModifier().write(0,
                chatSession != null ? BukkitConverters.getWrappedRemoteChatSessionDataConverter().getGeneric(chatSession) : null);
    }
}
