package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData;
import net.dmulloy2.protocol.AbstractPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;

/**
 * Wrapper for {@code ClientboundLightUpdatePacket} (game phase, clientbound).
 *
 * <p>NMS record: {@code ClientboundLightUpdatePacket(int x, int z, ClientboundLightUpdatePacketData lightData)}
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int x} – chunk section X coordinate</li>
 *   <li>{@code int z} – chunk section Z coordinate</li>
 *   <li>{@code WrappedLevelChunkData.LightData lightData} – light engine data for the section</li>
 * </ul>
 *
 * <p>Note: the NMS canonical constructor is not used because {@code ClientboundLightUpdatePacketData}
 * may not be present in all supported server versions; all fields are set via ProtocolLib's
 * {@code StructureModifier} after creating a default packet container.
 */
public class WrappedClientboundLightUpdatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.LIGHT_UPDATE;

    public WrappedClientboundLightUpdatePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundLightUpdatePacket(int x, int z, WrappedLevelChunkData.LightData lightData) {
        this();
        setX(x);
        setZ(z);
        setLightData(lightData);
    }

    public WrappedClientboundLightUpdatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getX() {
        return handle.getIntegers().read(0);
    }

    public void setX(int x) {
        handle.getIntegers().write(0, x);
    }

    public int getZ() {
        return handle.getIntegers().read(1);
    }

    public void setZ(int z) {
        handle.getIntegers().write(1, z);
    }

    /** Returns the light engine data for the section, or {@code null} if not set. */
    public WrappedLevelChunkData.LightData getLightData() {
        Object raw = handle.getModifier().read(2);
        return raw != null ? BukkitConverters.getWrappedLightDataConverter().getSpecific(raw) : null;
    }

    public void setLightData(WrappedLevelChunkData.LightData lightData) {
        handle.getModifier().write(2,
                lightData != null ? BukkitConverters.getWrappedLightDataConverter().getGeneric(lightData) : null);
    }
}
