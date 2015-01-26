/**
 * (c) 2015 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import org.bukkit.Material;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a wrapper around IBlockData.
 *
 * @author dmulloy2
 */

public class WrappedBlockData extends AbstractWrapper {
	private static final Class<?> MAGIC_NUMBERS = MinecraftReflection.getCraftBukkitClass("util.CraftMagicNumbers");
	private static final Class<?> IBLOCK_DATA = MinecraftReflection.getIBlockDataClass();
	private static final Class<?> BLOCK = MinecraftReflection.getBlockClass();

	private static MethodAccessor FROM_LEGACY_DATA = null;
	private static MethodAccessor GET_NMS_BLOCK = null;
	private static MethodAccessor GET_BLOCK = null;

	static {
		FuzzyReflection fuzzy = FuzzyReflection.fromClass(BLOCK);
		FROM_LEGACY_DATA = Accessors.getMethodAccessor(fuzzy.getMethodByParameters("fromLegacyData", IBLOCK_DATA,
				new Class<?>[] { int.class }));

		fuzzy = FuzzyReflection.fromClass(MAGIC_NUMBERS);
		GET_NMS_BLOCK = Accessors.getMethodAccessor(fuzzy.getMethodByParameters("getBlock", BLOCK,
				new Class<?>[] { int.class }));

		fuzzy = FuzzyReflection.fromClass(IBLOCK_DATA);
		GET_BLOCK = Accessors.getMethodAccessor(fuzzy.getMethodByParameters("getBlock", BLOCK,
				new Class<?>[0]));
	}

	public WrappedBlockData(Object handle) {
		super(IBLOCK_DATA);
		setHandle(handle);
	}

	/**
	 * Retrieves the type of this BlockData.
	 * @return The type of this BlockData.
	 */
	public Material getType() {
		Object block = GET_BLOCK.invoke(handle);
		return BukkitConverters.getBlockConverter().getSpecific(block);
	}

	/**
	 * Sets the type of this BlockData.
	 * @param type New type
	 */
	public void setType(Material type) {
		setTypeAndData(type, 0);
	}

	/**
	 * Sets the type and data of this BlockData.
	 * @param type New type
	 * @param data New data
	 */
	public void setTypeAndData(Material type, int data) {
		Object nmsBlock = GET_NMS_BLOCK.invoke(null, type.getId());
		Object blockData = FROM_LEGACY_DATA.invoke(nmsBlock, data);
		setHandle(blockData);
	}

	/**
	 * Creates a new BlockData instance with the given type and no data.
	 * @param type Block type
	 * @return New BlockData
	 */
	public static WrappedBlockData createData(Material type) {
		return createData(type, 0);
	}

	/**
	 * Creates a new BlockData instance with the given type and data.
	 * @param type Block type
	 * @param data Block data
	 * @return New BlockData
	 */
	public static WrappedBlockData createData(Material type, int data) {
		Object nmsBlock = GET_NMS_BLOCK.invoke(null, type.getId());
		Object blockData = FROM_LEGACY_DATA.invoke(nmsBlock, data);
		return new WrappedBlockData(blockData);
	}
}