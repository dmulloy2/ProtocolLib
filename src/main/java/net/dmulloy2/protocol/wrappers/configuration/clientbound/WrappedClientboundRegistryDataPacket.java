package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundRegistryDataPacket} (configuration phase, clientbound).
 */
public class WrappedClientboundRegistryDataPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.REGISTRY_DATA;

    public WrappedClientboundRegistryDataPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRegistryDataPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'registry' (NMS type: ResourceKey<? extends Registry<?>>)
    //   Use getMinecraftKeys().read(0) for the registry key identifier, or getSpecificModifier(ResourceKey.class).read(0).
    // TODO: missing field 'entries' (NMS type: List<RegistrySynchronization.PackedRegistryEntry>)
    //   Each entry has an id (Identifier) and an optional NBT tag. Use handle.getModifier().read(1)
    //   for the raw List, or add a dedicated WrappedPackedRegistryEntry class.
}
