package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.AutoWrapper;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Wrapper for {@code ClientboundSetDefaultSpawnPositionPacket} (Play phase, clientbound).
 *
 * <p>NMS structure (1.21+):
 * <pre>
 * record ClientboundSetDefaultSpawnPositionPacket(LevelData.RespawnData respawnData)
 * record LevelData.RespawnData(GlobalPos globalPos, float yaw, float pitch)
 * record GlobalPos(ResourceKey&lt;Level&gt; dimension, BlockPos pos)
 * </pre>
 *
 * <p>The nested structure maps 1:1 to a Bukkit {@link Location}:
 * <ul>
 *   <li>{@code GlobalPos.dimension} ↔ {@link Location#getWorld()}
 *       (via {@link BukkitConverters#getWorldKeyConverter()})
 *   <li>{@code GlobalPos.pos} ↔ {@link Location#getX()}/{@link Location#getY()}/{@link Location#getZ()}
 *   <li>{@code RespawnData.yaw} ↔ {@link Location#getYaw()}
 *   <li>{@code RespawnData.pitch} ↔ {@link Location#getPitch()}
 * </ul>
 *
 * <p>The {@link #LOCATION_CONVERTER} encapsulates this mapping entirely; the public API
 * is just {@link #getLocation()} and {@link #setLocation(Location)}.
 */
public class WrappedClientboundSetDefaultSpawnPositionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SPAWN_POSITION;

    /**
     * Converts between {@code LevelData.RespawnData} (NMS) and {@link Location} (Bukkit).
     *
     * <p>The nested NMS structure is:
     * <pre>
     *   RespawnData { globalPos: GlobalPos { dimension: ResourceKey&lt;Level&gt;, pos: BlockPos }, yaw: float, pitch: float }
     * </pre>
     * This converter maps those fields to/from a Bukkit {@link Location}.
     * A {@code null} input produces a {@code null} output in both directions.
     */
    private static final EquivalentConverter<Location> LOCATION_CONVERTER = new EquivalentConverter<Location>() {
        @Override
        public Location getSpecific(Object generic) {
            if (generic == null) return null;
            try {
                // RespawnData.globalPos()
                java.lang.reflect.Method globalPosM = generic.getClass().getMethod("globalPos");
                Object globalPos = globalPosM.invoke(generic);
                // RespawnData.yaw()
                float yaw   = (float) generic.getClass().getMethod("yaw").invoke(generic);
                // RespawnData.pitch()
                float pitch = (float) generic.getClass().getMethod("pitch").invoke(generic);

                // GlobalPos.pos() → BlockPos
                Object blockPos = globalPos.getClass().getMethod("pos").invoke(globalPos);
                double x = ((Number) blockPos.getClass().getMethod("getX").invoke(blockPos)).doubleValue();
                double y = ((Number) blockPos.getClass().getMethod("getY").invoke(blockPos)).doubleValue();
                double z = ((Number) blockPos.getClass().getMethod("getZ").invoke(blockPos)).doubleValue();

                return new Location(null, x, y, z, yaw, pitch);
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }

        @Override
        public Object getGeneric(Location specific) {
            if (specific == null) return null;
            try {
                // Build BlockPos
                Class<?> blockPosClass = com.comphenix.protocol.utility.MinecraftReflection.getBlockPositionClass();
                Object blockPos = blockPosClass.getConstructor(int.class, int.class, int.class)
                        .newInstance(specific.getBlockX(), specific.getBlockY(), specific.getBlockZ());

                // Build GlobalPos using OVERWORLD dimension key as fallback
                Class<?> registriesClass = Class.forName("net.minecraft.core.registries.Registries");
                Object overworldKey = Class.forName("net.minecraft.world.level.Level")
                        .getDeclaredField("OVERWORLD").get(null);
                Class<?> globalPosClass = Class.forName("net.minecraft.core.GlobalPos");
                Object globalPos = globalPosClass.getMethod("of",
                                Class.forName("net.minecraft.resources.ResourceKey"), blockPosClass)
                        .invoke(null, overworldKey, blockPos);

                // Build LevelData.RespawnData
                Class<?> respawnDataClass = Class.forName("net.minecraft.world.level.storage.LevelData$RespawnData");
                return respawnDataClass.getConstructor(globalPosClass, float.class, float.class)
                        .newInstance(globalPos, specific.getYaw(), specific.getPitch());
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }

        @Override
        public Class<Location> getSpecificType() {
            return Location.class;
        }
    };

    public WrappedClientboundSetDefaultSpawnPositionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetDefaultSpawnPositionPacket(Location location) {
        this();
        setLocation(location);
    }

    public WrappedClientboundSetDefaultSpawnPositionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // ---- API ----------------------------------------------------------------

    /**
     * Returns the spawn location.
     *
     * <p>{@link Location#getWorld()} is resolved via {@link BukkitConverters#getWorldKeyConverter()}.
     * If the dimension is absent or the world is not loaded, it will be {@code null} — all
     * coordinate and angle fields are still populated. Returns {@code null} only if the packet
     * carries no {@code RespawnData} (should not occur in practice).
     */
    public Location getLocation() {
        Object rawRd = handle.getModifier().read(0);
        return rawRd != null ? LOCATION_CONVERTER.getSpecific(rawRd) : null;
    }

    /**
     * Sets the spawn location. Block coordinates become the spawn position;
     * {@link Location#getYaw()} and {@link Location#getPitch()} set the angles;
     * a non-{@code null} {@link Location#getWorld()} is stored as the dimension key.
     */
    public void setLocation(Location location) {
        handle.getModifier().write(0, LOCATION_CONVERTER.getGeneric(location));
    }
}
