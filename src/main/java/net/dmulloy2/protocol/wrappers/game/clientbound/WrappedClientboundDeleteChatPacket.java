package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDeleteChatPacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code MessageSignature.Packed messageSignature} – identifies the message to delete</li>
 * </ul>
 *
 * <p>Note: The NMS field is {@code MessageSignature.Packed} (an inner record), which differs from
 * the plain {@code MessageSignature} class used by {@code getMessageSignatures()}. As a result,
 * the {@code messageSignature} field is not accessible via the standard modifier; getter/setter
 * operate as no-ops and always return {@code null}.
 */
public class WrappedClientboundDeleteChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DELETE_CHAT_MESSAGE;

    public WrappedClientboundDeleteChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDeleteChatPacket(WrappedMessageSignature messageSignature) {
        this();
        setMessageSignature(messageSignature);
    }

    public WrappedClientboundDeleteChatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedMessageSignature getMessageSignature() {
        return handle.getMessageSignatures().readSafely(0);
    }

    public void setMessageSignature(WrappedMessageSignature messageSignature) {
        handle.getMessageSignatures().writeSafely(0, messageSignature);
    }
}
