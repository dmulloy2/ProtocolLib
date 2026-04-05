package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.util.Optional;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundResourcePackPushPacket} (game phase, clientbound).
 */
public class WrappedClientboundResourcePackPushPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ADD_RESOURCE_PACK;

    public WrappedClientboundResourcePackPushPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundResourcePackPushPacket(String url, String hash, boolean required, UUID id, Optional<WrappedChatComponent> prompt) {
        this();
        setUrl(url);
        setHash(hash);
        setRequired(required);
        setId(id);
        setPrompt(prompt);
    }

    public WrappedClientboundResourcePackPushPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getUrl() {
        return handle.getStrings().read(0);
    }

    public void setUrl(String url) {
        handle.getStrings().write(0, url);
    }

    public String getHash() {
        return handle.getStrings().read(1);
    }

    public void setHash(String hash) {
        handle.getStrings().write(1, hash);
    }

    public boolean isRequired() {
        return handle.getBooleans().read(0);
    }

    public void setRequired(boolean required) {
        handle.getBooleans().write(0, required);
    }

    public UUID getId() {
        return handle.getUUIDs().read(0);
    }

    public void setId(UUID id) {
        handle.getUUIDs().write(0, id);
    }

    public Optional<WrappedChatComponent> getPrompt() {
        return handle.getOptionals(BukkitConverters.getWrappedChatComponentConverter()).read(0);
    }

    public void setPrompt(Optional<WrappedChatComponent> prompt) {
        handle.getOptionals(BukkitConverters.getWrappedChatComponentConverter()).write(0, prompt);
    }
}
