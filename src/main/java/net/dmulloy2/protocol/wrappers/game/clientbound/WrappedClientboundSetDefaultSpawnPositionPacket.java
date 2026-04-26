package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.Location;

/**
 * Wrapper for {@code ClientboundSetDefaultSpawnPositionPacket} (Play phase, clientbound).
 *
 * <p>NMS structure:
 * <pre>
 * record ClientboundSetDefaultSpawnPositionPacket(LevelData.RespawnData respawnData)
 * record LevelData.RespawnData(GlobalPos globalPos, float yaw, float pitch)
 * record GlobalPos(ResourceKey&lt;Level&gt; dimension, BlockPos pos)
 * </pre>
 *
 * <p>The nested structure maps 1:1 to a Bukkit {@link Location}:
 * <ul>
 *   <li>{@code GlobalPos.dimension} ↔ {@link Location#getWorld()}
 *   <li>{@code GlobalPos.pos} ↔ {@link Location#getX()}/{@link Location#getY()}/{@link Location#getZ()}
 *   <li>{@code RespawnData.yaw} ↔ {@link Location#getYaw()}
 *   <li>{@code RespawnData.pitch} ↔ {@link Location#getPitch()}
 * </ul>
 */
public class WrappedClientboundSetDefaultSpawnPositionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SPAWN_POSITION;

    // ---- Cached NMS classes -------------------------------------------------

    private static final Class<?> RESPAWN_DATA_CLASS;
    private static final Class<?> GLOBAL_POS_CLASS;
    private static final Class<?> BLOCK_POS_CLASS;
    private static final Class<?> RESOURCE_KEY_CLASS;

    // ---- Cached accessors ---------------------------------------------------

    /** RespawnData → GlobalPos */
    private static final MethodAccessor RESPAWN_DATA_GLOBAL_POS;
    /** RespawnData → float yaw */
    private static final MethodAccessor RESPAWN_DATA_YAW;
    /** RespawnData → float pitch */
    private static final MethodAccessor RESPAWN_DATA_PITCH;
    /** GlobalPos → BlockPos */
    private static final MethodAccessor GLOBAL_POS_POS;
    /** BlockPos → int x */
    private static final MethodAccessor BLOCK_POS_GET_X;
    /** BlockPos → int y */
    private static final MethodAccessor BLOCK_POS_GET_Y;
    /** BlockPos → int z */
    private static final MethodAccessor BLOCK_POS_GET_Z;
    /** BlockPos(int, int, int) */
    private static final ConstructorAccessor BLOCK_POS_CTOR;
    /** static GlobalPos.of(ResourceKey, BlockPos) */
    private static final MethodAccessor GLOBAL_POS_OF;
    /** RespawnData(GlobalPos, float, float) */
    private static final ConstructorAccessor RESPAWN_DATA_CTOR;
    /** Cached Level.OVERWORLD ResourceKey value */
    private static final Object OVERWORLD_KEY;

    static {
        try {
            RESPAWN_DATA_CLASS  = Class.forName("net.minecraft.world.level.storage.LevelData$RespawnData");
            GLOBAL_POS_CLASS    = Class.forName("net.minecraft.core.GlobalPos");
            BLOCK_POS_CLASS     = MinecraftReflection.getBlockPositionClass();
            RESOURCE_KEY_CLASS  = Class.forName("net.minecraft.resources.ResourceKey");

            RESPAWN_DATA_GLOBAL_POS = Accessors.getMethodAccessor(RESPAWN_DATA_CLASS, "globalPos");
            RESPAWN_DATA_YAW        = Accessors.getMethodAccessor(RESPAWN_DATA_CLASS, "yaw");
            RESPAWN_DATA_PITCH      = Accessors.getMethodAccessor(RESPAWN_DATA_CLASS, "pitch");
            GLOBAL_POS_POS          = Accessors.getMethodAccessor(GLOBAL_POS_CLASS, "pos");
            BLOCK_POS_GET_X         = Accessors.getMethodAccessor(BLOCK_POS_CLASS, "getX");
            BLOCK_POS_GET_Y         = Accessors.getMethodAccessor(BLOCK_POS_CLASS, "getY");
            BLOCK_POS_GET_Z         = Accessors.getMethodAccessor(BLOCK_POS_CLASS, "getZ");
            BLOCK_POS_CTOR          = Accessors.getConstructorAccessor(BLOCK_POS_CLASS, int.class, int.class, int.class);
            GLOBAL_POS_OF           = Accessors.getMethodAccessor(GLOBAL_POS_CLASS, "of", RESOURCE_KEY_CLASS, BLOCK_POS_CLASS);
            RESPAWN_DATA_CTOR       = Accessors.getConstructorAccessor(RESPAWN_DATA_CLASS, GLOBAL_POS_CLASS, float.class, float.class);

            java.lang.reflect.Field owField = Class.forName("net.minecraft.world.level.Level")
                    .getDeclaredField("OVERWORLD");
            owField.setAccessible(true);
            OVERWORLD_KEY = owField.get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // ---- Converter ----------------------------------------------------------

    private static final EquivalentConverter<Location> LOCATION_CONVERTER = new EquivalentConverter<>() {
        @Override
        public Location getSpecific(Object generic) {
            if (generic == null) return null;
            Object globalPos = RESPAWN_DATA_GLOBAL_POS.invoke(generic);
            float yaw   = (float) RESPAWN_DATA_YAW.invoke(generic);
            float pitch = (float) RESPAWN_DATA_PITCH.invoke(generic);
            Object blockPos = GLOBAL_POS_POS.invoke(globalPos);
            double x = ((Number) BLOCK_POS_GET_X.invoke(blockPos)).doubleValue();
            double y = ((Number) BLOCK_POS_GET_Y.invoke(blockPos)).doubleValue();
            double z = ((Number) BLOCK_POS_GET_Z.invoke(blockPos)).doubleValue();
            return new Location(null, x, y, z, yaw, pitch);
        }

        @Override
        public Object getGeneric(Location specific) {
            if (specific == null) return null;
            Object blockPos  = BLOCK_POS_CTOR.invoke(specific.getBlockX(), specific.getBlockY(), specific.getBlockZ());
            Object globalPos = GLOBAL_POS_OF.invoke(null, OVERWORLD_KEY, blockPos);
            return RESPAWN_DATA_CTOR.invoke(globalPos, specific.getYaw(), specific.getPitch());
        }

        @Override
        public Class<Location> getSpecificType() {
            return Location.class;
        }
    };

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(RESPAWN_DATA_CLASS, LOCATION_CONVERTER);

    // ---- Constructors -------------------------------------------------------

    public WrappedClientboundSetDefaultSpawnPositionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetDefaultSpawnPositionPacket(Location location) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(location)));
    }

    public WrappedClientboundSetDefaultSpawnPositionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // ---- API ----------------------------------------------------------------

    /**
     * Returns the spawn location. {@link Location#getWorld()} is always {@code null} since
     * the dimension key is not resolved to a loaded world. Returns {@code null} only if the
     * packet carries no {@code RespawnData}.
     */
    public Location getLocation() {
        Object rawRd = handle.getModifier().read(0);
        return rawRd != null ? LOCATION_CONVERTER.getSpecific(rawRd) : null;
    }

    /**
     * Sets the spawn location. Block coordinates become the spawn position;
     * yaw/pitch are stored as-is. The dimension is always written as {@code Level.OVERWORLD}.
     */
    public void setLocation(Location location) {
        handle.getModifier().write(0, LOCATION_CONVERTER.getGeneric(location));
    }
}
