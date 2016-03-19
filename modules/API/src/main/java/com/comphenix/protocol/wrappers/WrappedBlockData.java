/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Modifier;

import org.bukkit.Material;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
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
	private static MethodAccessor TO_LEGACY_DATA = null;
	private static MethodAccessor GET_NMS_BLOCK = null;
	private static MethodAccessor GET_BLOCK = null;

	static {
		FuzzyReflection fuzzy = FuzzyReflection.fromClass(BLOCK);
		FuzzyMethodContract contract = FuzzyMethodContract.newBuilder()
				.banModifier(Modifier.STATIC)
				.parameterExactArray(int.class)
				.returnTypeExact(IBLOCK_DATA)
				.build();
		FROM_LEGACY_DATA = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

		contract = FuzzyMethodContract.newBuilder()
				.banModifier(Modifier.STATIC)
				.parameterExactArray(IBLOCK_DATA)
				.returnTypeExact(int.class)
				.build();
		TO_LEGACY_DATA = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

		fuzzy = FuzzyReflection.fromClass(MAGIC_NUMBERS);
		GET_NMS_BLOCK = Accessors.getMethodAccessor(fuzzy.getMethodByParameters("getBlock", BLOCK,
				new Class<?>[] { Material.class }));

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
	 * Retrieves the type id of this BlockData.
	 * @return The type id of this BlockData.
	 * @deprecated ID's are deprecated
	 */
	@Deprecated
	public int getTypeId() {
		Object block = GET_BLOCK.invoke(handle);
		return BukkitConverters.getBlockIDConverter().getSpecific(block);
	}

	/**
	 * Retrieves the data of this BlockData.
	 * @return The data of this BlockData.
	 */
	public int getData() {
		Object block = GET_BLOCK.invoke(handle);
		return (Integer) TO_LEGACY_DATA.invoke(block, handle);
	}

	/**
	 * Sets the type of this BlockData.
	 * @param type New type
	 */
	public void setType(Material type) {
		setTypeAndData(type, 0);
	}

	/**
	 * Sets the data of this BlockData.
	 * @param data New data
	 */
	public void setData(int data) {
		setTypeAndData(getType(), data);
	}

	/**
	 * Sets the type and data of this BlockData.
	 * @param type New type
	 * @param data New data
	 */
	public void setTypeAndData(Material type, int data) {
		Object nmsBlock = GET_NMS_BLOCK.invoke(null, type);
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
		Object nmsBlock = GET_NMS_BLOCK.invoke(null, type);
		Object blockData = FROM_LEGACY_DATA.invoke(nmsBlock, data);
		return new WrappedBlockData(blockData);
	}

	@Override
	public String toString() {
		return "WrappedBlockData[handle=" + handle + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WrappedBlockData) {
			WrappedBlockData that = (WrappedBlockData) o;
			return this.getType() == that.getType();
		}

		return false;
	}
}