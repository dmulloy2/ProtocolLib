package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedLastSeenMessagesUpdate;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import java.time.Instant;
import java.util.BitSet;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChatPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code String message} – the chat message text</li>
 *   <li>{@code Instant timeStamp} – timestamp at which the message was sent</li>
 *   <li>{@code long salt} – random salt used for message signing</li>
 *   <li>{@code MessageSignature signature} – optional message signature</li>
 *   <li>{@code LastSeenMessages.Update lastSeenMessages} – acknowledgement state</li>
 * </ul>
 */
public class WrappedServerboundChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHAT;

    private static final Class<?> LAST_SEEN_MESSAGES_UPDATE_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.LastSeenMessages$Update");

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(String.class)
            .withParam(Instant.class)
            .withParam(long.class)
            .withParam(MinecraftReflection.getMessageSignatureClass(), BukkitConverters.getWrappedMessageSignatureConverter())
            .withParam(LAST_SEEN_MESSAGES_UPDATE_CLASS, WrappedLastSeenMessagesUpdate.CONVERTER);

    public WrappedServerboundChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundChatPacket(String message, Instant timeStamp, long salt, WrappedMessageSignature signature) {
        this(message, timeStamp, salt, signature, new WrappedLastSeenMessagesUpdate(0, new BitSet(), (byte) 0));
    }

    public WrappedServerboundChatPacket(String message, Instant timeStamp, long salt,
            WrappedMessageSignature signature, WrappedLastSeenMessagesUpdate lastSeenMessages) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(message, timeStamp, salt, signature, lastSeenMessages)));
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

    /**
     * Returns the optional message signature bytes, or {@code null} if unsigned.
     */
    public WrappedMessageSignature getSignature() {
        return handle.getMessageSignatures().read(0);
    }

    /**
     * Sets the message signature.
     */
    public void setSignature(WrappedMessageSignature signature) {
        handle.getMessageSignatures().write(0, signature);
    }

    public WrappedLastSeenMessagesUpdate getLastSeenMessages() {
        return handle.getModifier()
                .withType(LAST_SEEN_MESSAGES_UPDATE_CLASS, WrappedLastSeenMessagesUpdate.CONVERTER)
                .read(0);
    }

    public void setLastSeenMessages(WrappedLastSeenMessagesUpdate lastSeenMessages) {
        handle.getModifier()
                .withType(LAST_SEEN_MESSAGES_UPDATE_CLASS, WrappedLastSeenMessagesUpdate.CONVERTER)
                .write(0, lastSeenMessages);
    }
}
