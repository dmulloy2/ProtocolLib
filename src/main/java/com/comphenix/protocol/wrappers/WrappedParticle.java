package com.comphenix.protocol.wrappers;

import java.lang.reflect.Modifier;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

import com.google.common.base.Preconditions;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an immutable wrapped ParticleParam in 1.13
 */
public class WrappedParticle<T> {
    private static Class<?> VECTOR_3FA;

    private static MethodAccessor toBukkit;
    private static MethodAccessor getType;
    private static MethodAccessor toNMS;
    private static MethodAccessor toCraftData;

    private static void ensureMethods() {
        if (toBukkit != null && toNMS != null) {
            return;
        }

        Class<?> particleType = MinecraftReflection.isMojangMapped()
            ? MinecraftReflection.getParticleTypeClass()
            : MinecraftReflection.getParticleClass();

        Preconditions.checkNotNull(particleType, "Cannot find ParticleType class (MojMap: " + MinecraftReflection.isMojangMapped() + ")");

        FuzzyReflection fuzzy = FuzzyReflection.fromClass(MinecraftReflection.getCraftBukkitClass("CraftParticle"));
        if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove()) {
            FuzzyMethodContract contract = FuzzyMethodContract
                    .newBuilder()
                    .requireModifier(Modifier.STATIC)
                    .returnTypeExact(Particle.class)
                    .parameterExactArray(particleType)
                    .build();
            toBukkit = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

            FuzzyReflection particleParam = FuzzyReflection.fromClass(MinecraftReflection.getParticleParam(), false);
            contract = FuzzyMethodContract
                    .newBuilder()
                    .returnTypeExact(particleType)
                    .parameterCount(0)
                    .build();
            getType = Accessors.getMethodAccessor(particleParam.getMethod(contract));
        } else {
            FuzzyMethodContract contract = FuzzyMethodContract
                    .newBuilder()
                    .requireModifier(Modifier.STATIC)
                    .returnTypeExact(Particle.class)
                    .parameterExactType(MinecraftReflection.getParticleParam())
                    .build();
            toBukkit = Accessors.getMethodAccessor(fuzzy.getMethod(contract));
        }

        FuzzyMethodContract contract = FuzzyMethodContract
                .newBuilder()
                .requireModifier(Modifier.STATIC)
                .returnTypeExact(MinecraftReflection.getParticleParam())
                .parameterCount(2)
                .build();
        toNMS = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

        Class<?> cbData = MinecraftReflection.getCraftBukkitClass("block.data.CraftBlockData");
        fuzzy = FuzzyReflection.fromClass(cbData);
        contract = FuzzyMethodContract
                .newBuilder()
                .requireModifier(Modifier.STATIC)
                .returnTypeExact(cbData)
                .parameterExactArray(MinecraftReflection.getIBlockDataClass())
                .build();
        toCraftData = Accessors.getMethodAccessor(fuzzy.getMethod(contract));
    }

    private final Particle particle;
    private final T data;
    private final Object handle;

    private WrappedParticle(Object handle, Particle particle, T data) {
        this.handle = handle;
        this.particle = particle;
        this.data = data;
    }

    /**
     * @return This particle's Bukkit type
     */
    public Particle getParticle() {
        return particle;
    }

    /**
     * Gets this Particle's Bukkit/ProtocolLib data. The type of this data depends on the
     * {@link #getParticle() Particle type}. Refer to the table below for the corresponding data types.
     * <p>
     * <table border="1">
     * <caption>Particle Data Types</caption>
     * <tr>
     * <td><b>Particle Type</b></td>
     * <td><b>Particle Data Type</b></td>
     * </tr>
     * <tr>
     * <td>Block particles (BLOCK_CRACK, BLOCK_DUST, FALLING_DUST)</td>
     * <td>{@link WrappedBlockData}</td>
     * </tr>
     * <tr>
     * <td>Item crack particles</td>
     * <td>{@link ItemStack}</td>
     * </tr>
     * <tr>
     * <td>Redstone particles</td>
     * <td>{@link Particle.DustOptions}</td>
     * </tr>
     * <tr>
     * <td>Dust color transition particles</td>
     * <td>{@link Particle.DustTransition}</td>
     * </tr>
     * </table>
     *
     * @return The particle data
     */
    public T getData() {
        return data;
    }

    /**
     * @return NMS handle
     */
    public Object getHandle() {
        return handle;
    }

    public static WrappedParticle fromHandle(Object handle) {
        ensureMethods();

        Particle bukkit;
        if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove()) {
            Object particle = getType.invoke(handle);
            bukkit = (Particle) toBukkit.invoke(null, particle);
        } else {
            bukkit = (Particle) toBukkit.invoke(null, handle);
        }

		Object data = null;
		Class<?> dataType = bukkit.getDataType();
		if (dataType == BlockData.class) {
			data = getBlockData(handle);
		} else if (dataType == Particle.DustTransition.class) {
			data = getDustTransition(handle);
		} else if (dataType == ItemStack.class) {
			data = getItem(handle);
		} else if (dataType == Particle.DustOptions.class) {
			data = getRedstone(handle);
		}

        return new WrappedParticle<>(handle, bukkit, data);
    }

    private static WrappedBlockData getBlockData(Object handle) {
        return new StructureModifier<>(handle.getClass())
                .withTarget(handle)
                .withType(MinecraftReflection.getIBlockDataClass(), BukkitConverters.getWrappedBlockDataConverter())
                .read(0);
    }

    private static Object getItem(Object handle) {
        return new StructureModifier<>(handle.getClass())
                .withTarget(handle)
                .withType(MinecraftReflection.getItemStackClass(), BukkitConverters.getItemStackConverter())
                .read(0);
    }

    private static Object getRedstone(Object handle) {
        int r, g, b;
        float size;

        if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
            StructureModifier<Object> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle);
            org.joml.Vector3f rgb = (org.joml.Vector3f) modifier.withType(org.joml.Vector3f.class).read(0);

            r = (int) (rgb.x() * 255);
            g = (int) (rgb.y() * 255);
            b = (int) (rgb.z() * 255);
            size = (float) modifier.withType(float.class).read(0);
        } else if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
            if (VECTOR_3FA == null) {
                VECTOR_3FA = MinecraftReflection.getLibraryClass("com.mojang.math.Vector3fa");
            }

            StructureModifier<Object> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle);

            Object rgb = modifier.withType(VECTOR_3FA).read(0);
            StructureModifier<Object> rgbModifier = new StructureModifier<>(VECTOR_3FA).withTarget(rgb);

            r = (int) (rgbModifier.<Float>withType(float.class).read(0) * 255);
            g = (int) (rgbModifier.<Float>withType(float.class).read(1) * 255);
            b = (int) (rgbModifier.<Float>withType(float.class).read(2) * 255);
            size = (float) modifier.withType(float.class).read(0);
        } else {
            StructureModifier<Float> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle).withType(float.class);
            r = (int) (modifier.read(0) * 255);
            g = (int) (modifier.read(1) * 255);
            b = (int) (modifier.read(2) * 255);
            size = modifier.read(3);
        }

        return new Particle.DustOptions(Color.fromRGB(r, g, b), size);
    }

    private static Object getDustTransition(Object handle) {
        int fromR, fromG, fromB, toR, toG, toB;
        float size;

        if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
            StructureModifier<Object> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle);
            org.joml.Vector3f toRGB = (org.joml.Vector3f) modifier.withType(org.joml.Vector3f.class).read(1);
            org.joml.Vector3f fromRGB = (org.joml.Vector3f) modifier.withType(org.joml.Vector3f.class).read(0);
            size = (float) modifier.withType(float.class).read(0);

            fromR = (int) (fromRGB.x() * 255);
            fromG = (int) (fromRGB.y() * 255);
            fromB = (int) (fromRGB.z() * 255);

            toR = (int) (toRGB.x() * 255);
            toG = (int) (toRGB.y() * 255);
            toB = (int) (toRGB.z() * 255);
        } else if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
            if (VECTOR_3FA == null) {
                VECTOR_3FA = MinecraftReflection.getLibraryClass("com.mojang.math.Vector3fa");
            }

            StructureModifier<Object> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle);

            Object toRGB = modifier.withType(VECTOR_3FA).read(0);
            Object fromRGB = modifier.withType(VECTOR_3FA).read(1);
            size = (float) modifier.withType(float.class).read(0);
            StructureModifier<Object> rgbModifier = new StructureModifier<>(VECTOR_3FA).withTarget(fromRGB);
            StructureModifier<Object> rgbModifier2 = new StructureModifier<>(VECTOR_3FA).withTarget(toRGB);

            fromR = (int) (rgbModifier.<Float>withType(float.class).read(0) * 255);
            fromG = (int) (rgbModifier.<Float>withType(float.class).read(1) * 255);
            fromB = (int) (rgbModifier.<Float>withType(float.class).read(2) * 255);

            toR = (int) (rgbModifier2.<Float>withType(float.class).read(0) * 255);
            toG = (int) (rgbModifier2.<Float>withType(float.class).read(1) * 255);
            toB = (int) (rgbModifier2.<Float>withType(float.class).read(2) * 255);
        } else {
            StructureModifier<Float> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle).withType(float.class);
            toR = (int) (modifier.read(0) * 255);
            toG = (int) (modifier.read(1) * 255);
            toB = (int) (modifier.read(2) * 255);
            size = modifier.read(3);
            fromR = (int) (modifier.read(4) * 255);
            fromG = (int) (modifier.read(5) * 255);
            fromB = (int) (modifier.read(6) * 255);
        }

        return new Particle.DustTransition(Color.fromRGB(fromR, fromG, fromB), Color.fromRGB(toR, toG, toB), size);
    }

    public static <T> WrappedParticle<T> create(Particle particle, T data) {
        ensureMethods();

        Object bukkitData = data;
        if (data instanceof WrappedBlockData) {
            WrappedBlockData blockData = (WrappedBlockData) data;
            bukkitData = toCraftData.invoke(null, blockData.getHandle());
        }

        Object handle = toNMS.invoke(null, particle, bukkitData);
        return new WrappedParticle<>(handle, particle, data);
    }
}
