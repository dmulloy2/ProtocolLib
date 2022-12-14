package com.comphenix.protocol.wrappers;

import java.lang.reflect.Modifier;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an immutable wrapped ParticleParam in 1.13
 */
public class WrappedParticle<T> {
	private static Class<?> VECTOR_3FA;

	private static MethodAccessor toBukkit;
	private static MethodAccessor toNMS;
	private static MethodAccessor toCraftData;

	private static void ensureMethods() {
		if (toBukkit != null && toNMS != null) {
			return;
		}

		FuzzyReflection fuzzy = FuzzyReflection.fromClass(MinecraftReflection.getCraftBukkitClass("CraftParticle"));
		FuzzyMethodContract contract = FuzzyMethodContract
				.newBuilder()
				.requireModifier(Modifier.STATIC)
				.returnTypeExact(Particle.class)
				.parameterExactType(MinecraftReflection.getParticleParam())
				.build();
		toBukkit = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

		contract = FuzzyMethodContract
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
	 * {@link #getParticle() Particle type}. For Block particles it will be {@link WrappedBlockData},
	 * for Item crack particles, it will be an {@link ItemStack}, and for redstone particles it will
	 * be {@link Particle.DustOptions}
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

		Particle bukkit = (Particle) toBukkit.invoke(null, handle);
		Object data = null;

		switch (bukkit) {
			case BLOCK_CRACK:
			case BLOCK_DUST:
			case FALLING_DUST:
				data = getBlockData(handle);
				break;
			case ITEM_CRACK:
				data = getItem(handle);
				break;
			case REDSTONE:
				data = getRedstone(handle);
				break;
			default:
				break;
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
		float alpha;

		if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
			StructureModifier<Object> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle);
			org.joml.Vector3f rgb = (org.joml.Vector3f) modifier.withType(org.joml.Vector3f.class).read(0);

			r = (int) (rgb.x() * 255);
			g = (int) (rgb.y() * 255);
			b = (int) (rgb.z() * 255);
			alpha = (float) modifier.withType(float.class).read(0);
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
			alpha = (float) modifier.withType(float.class).read(0);
		} else {
			StructureModifier<Float> modifier = new StructureModifier<>(handle.getClass()).withTarget(handle).withType(float.class);
			r = (int) (modifier.read(0) * 255);
			g = (int) (modifier.read(1) * 255);
			b = (int) (modifier.read(2) * 255);
			alpha = modifier.read(3);
		}

		return new Particle.DustOptions(Color.fromRGB(r, g, b), alpha);
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
