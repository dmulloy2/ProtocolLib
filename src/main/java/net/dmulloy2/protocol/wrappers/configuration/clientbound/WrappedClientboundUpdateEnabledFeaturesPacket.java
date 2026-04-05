package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Set;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundUpdateEnabledFeaturesPacket} (configuration phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Set<ResourceLocation> features} – set of enabled feature flag identifiers</li>
 * </ul>
 */
public class WrappedClientboundUpdateEnabledFeaturesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.UPDATE_ENABLED_FEATURES;

    public WrappedClientboundUpdateEnabledFeaturesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundUpdateEnabledFeaturesPacket(Set<MinecraftKey> features) {
        this();
        setFeatures(features);
    }

    public WrappedClientboundUpdateEnabledFeaturesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Set<MinecraftKey> getFeatures() {
        return handle.getSets(MinecraftKey.getConverter()).read(0);
    }

    public void setFeatures(Set<MinecraftKey> features) {
        handle.getSets(MinecraftKey.getConverter()).write(0, features);
    }
}
