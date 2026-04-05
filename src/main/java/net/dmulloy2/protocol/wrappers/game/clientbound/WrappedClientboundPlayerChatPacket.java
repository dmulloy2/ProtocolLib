package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerChatPacket} (game phase, clientbound).
 */
public class WrappedClientboundPlayerChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CHAT;

    public WrappedClientboundPlayerChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPlayerChatPacket(int globalIndex, int index, UUID sender, WrappedChatComponent unsignedContent, WrappedMessageSignature signature) {
        this();
        setGlobalIndex(globalIndex);
        setIndex(index);
        setSender(sender);
        setUnsignedContent(unsignedContent);
        setSignature(signature);
    }

    public WrappedClientboundPlayerChatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getGlobalIndex() {
        return handle.getIntegers().read(0);
    }

    public void setGlobalIndex(int globalIndex) {
        handle.getIntegers().write(0, globalIndex);
    }

    public int getIndex() {
        return handle.getIntegers().read(1);
    }

    public void setIndex(int index) {
        handle.getIntegers().write(1, index);
    }

    public UUID getSender() {
        return handle.getUUIDs().read(0);
    }

    public void setSender(UUID sender) {
        handle.getUUIDs().write(0, sender);
    }

    public WrappedChatComponent getUnsignedContent() {
        return handle.getChatComponents().read(0);
    }

    public void setUnsignedContent(WrappedChatComponent unsignedContent) {
        handle.getChatComponents().write(0, unsignedContent);
    }

    public WrappedMessageSignature getSignature() {
        return handle.getMessageSignatures().read(0);
    }

    public void setSignature(WrappedMessageSignature signature) {
        handle.getMessageSignatures().write(0, signature);
    }

    // TODO: missing field 'body' (NMS type: SignedMessageBody.Packed)
    //   Holds the plain text content, timestamp, salt, and a lastSeen acknowledgement array.
    //   Use handle.getSignatures() for the lastSeen portion, or handle.getModifier().read(N) for the raw Packed body.
    // TODO: missing field 'filterMask' (NMS type: FilterMask)
    //   Encodes which message characters are filtered. No dedicated ProtocolLib accessor exists.
    //   Use handle.getModifier().read(N) for the raw FilterMask object.
    // TODO: missing field 'chatType' (NMS type: ChatType.Bound)
    //   Holds a ResourceKey<ChatType> and two Component parameters (name, targetName).
    //   No dedicated ProtocolLib accessor exists; use handle.getModifier().read(N).
}
