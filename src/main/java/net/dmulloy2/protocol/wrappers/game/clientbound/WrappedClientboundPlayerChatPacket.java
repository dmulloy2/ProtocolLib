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
}
