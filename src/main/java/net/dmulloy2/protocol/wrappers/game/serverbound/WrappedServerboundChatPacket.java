package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

import java.time.Instant;

/**
 * Wrapper for {@code ServerboundChatPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code String message} – the chat message text</li>
 *   <li>{@code Instant timeStamp} – timestamp at which the message was sent</li>
 *   <li>{@code long salt} – random salt used for message signing</li>
 *   <li>{@code MessageSignature signature} – optional message signature (not exposed)</li>
 *   <li>{@code LastSeenMessages lastSeenMessages} – acknowledgement state (not exposed)</li>
 * </ul>
 */
public class WrappedServerboundChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHAT;

    public WrappedServerboundChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundChatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the chat message text.
     */
    public String getMessage() {
        return handle.getStrings().read(0);
    }

    /**
     * Sets the chat message text.
     */
    public void setMessage(String message) {
        handle.getStrings().write(0, message);
    }

    /**
     * Returns the timestamp at which the message was sent.
     */
    public Instant getTimeStamp() {
        return handle.getInstants().read(0);
    }

    /**
     * Sets the timestamp at which the message was sent.
     */
    public void setTimeStamp(Instant timeStamp) {
        handle.getInstants().write(0, timeStamp);
    }

    /**
     * Returns the random salt used for message signing.
     */
    public long getSalt() {
        return handle.getLongs().read(0);
    }

    /**
     * Sets the random salt used for message signing.
     */
    public void setSalt(long salt) {
        handle.getLongs().write(0, salt);
    }
}
