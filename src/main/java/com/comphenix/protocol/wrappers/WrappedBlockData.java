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

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * Represents a wrapper around IBlockData.
 *
 * @author dmulloy2
 */

public abstract class WrappedBlockData extends AbstractWrapper implements ClonableWrapper {
	private static final Class<?> MAGIC_NUMBERS = MinecraftReflection.getCraftBukkitClass("util.CraftMagicNumbers");
	private static final Class<?> IBLOCK_DATA = MinecraftReflection.getIBlockDataClass();
	private static final Class<?> BLOCK = MinecraftReflection.getBlockClass();

	private static class NewBlockData extends WrappedBlockData {
		private static MethodAccessor MATERIAL_FROM_BLOCK;
		private static MethodAccessor TO_LEGACY_DATA;
		private static MethodAccessor GET_BLOCK;
		private static MethodAccessor BLOCK_FROM_MATERIAL;
		private static MethodAccessor GET_BLOCK_DATA;
		private static MethodAccessor FROM_LEGACY_DATA;
		private static MethodAccessor GET_HANDLE;

		static {
			if (MinecraftVersion.atOrAbove(MinecraftVersion.AQUATIC_UPDATE)) {
				FuzzyReflection fuzzy = FuzzyReflection.fromClass(MAGIC_NUMBERS);
				FuzzyMethodContract contract = FuzzyMethodContract
						.newBuilder()
						.requireModifier(Modifier.STATIC)
						.returnTypeExact(Material.class)
						.parameterExactArray(BLOCK)
						.build();
				MATERIAL_FROM_BLOCK = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

				contract = FuzzyMethodContract
						.newBuilder()
						.requireModifier(Modifier.STATIC)
						.parameterExactArray(Material.class)
						.returnTypeExact(BLOCK)
						.build();
				BLOCK_FROM_MATERIAL = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

				contract = FuzzyMethodContract
						.newBuilder()
						.requireModifier(Modifier.STATIC)
						.parameterExactArray(IBLOCK_DATA)
						.returnTypeExact(byte.class)
						.build();
				TO_LEGACY_DATA = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

				contract = FuzzyMethodContract
						.newBuilder()
						.requireModifier(Modifier.STATIC)
						.parameterExactArray(Material.class, byte.class)
						.returnTypeExact(IBLOCK_DATA)
						.build();
				FROM_LEGACY_DATA = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

				fuzzy = FuzzyReflection.fromClass(IBLOCK_DATA);
				contract = FuzzyMethodContract
						.newBuilder()
						.banModifier(Modifier.STATIC)
						.returnTypeExact(BLOCK)
						.parameterCount(0)
						.build();
				GET_BLOCK = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

				fuzzy = FuzzyReflection.fromClass(BLOCK);
				contract = FuzzyMethodContract
						.newBuilder()
						.banModifier(Modifier.STATIC)
						.parameterCount(0)
						.returnTypeExact(IBLOCK_DATA)
						.build();
				GET_BLOCK_DATA = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

				fuzzy = FuzzyReflection.fromClass(MinecraftReflection.getCraftBukkitClass("block.data.CraftBlockData"));
				contract = FuzzyMethodContract
						.newBuilder()
						.banModifier(Modifier.STATIC)
						.parameterCount(0)
						.returnTypeExact(IBLOCK_DATA)
						.build();
				GET_HANDLE = Accessors.getMethodAccessor(fuzzy.getMethod(contract));
			}
		}

		private NewBlockData(Object handle) {
			super(handle);
		}

		@Override
		public Material getType() {
			Object block = GET_BLOCK.invoke(handle);
			return (Material) MATERIAL_FROM_BLOCK.invoke(null, block);
		}

		@Override
		public int getData() {
			return ((Number) TO_LEGACY_DATA.invoke(null, handle)).intValue();
		}

		@Override
		public void setType(Material material) {
			Object block = BLOCK_FROM_MATERIAL.invoke(null, material);
			setHandle(GET_BLOCK_DATA.invoke(block));
		}

		@Override
		public void setData(int data) {
			setTypeAndData(getType(), data);
		}

		@Override
		public void setTypeAndData(Material material, int data) {
			setHandle(TO_LEGACY_DATA.invoke(null, material, (byte) data));
		}

		@Override
		public WrappedBlockData deepClone() {
			return NewBlockData.createNewData(getType(), getData());
		}

		private static WrappedBlockData createNewData(Material material) {
			Object block = BLOCK_FROM_MATERIAL.invoke(null, material);
			return new NewBlockData(GET_BLOCK_DATA.invoke(block));
		}

		private static WrappedBlockData createNewData(Material material, int data) {
			return new NewBlockData(FROM_LEGACY_DATA.invoke(null, material, (byte) data));
		}

		private static WrappedBlockData createNewData(BlockData data) {
			return new NewBlockData(GET_HANDLE.invoke(data));
		}
	}

	private static class OldBlockData extends WrappedBlockData {
		private static MethodAccessor FROM_LEGACY_DATA;
		private static MethodAccessor TO_LEGACY_DATA;
		private static MethodAccessor GET_NMS_BLOCK;
		private static MethodAccessor GET_BLOCK;

		static {
			if (!MinecraftVersion.atOrAbove(MinecraftVersion.AQUATIC_UPDATE)) {
				FuzzyReflection fuzzy = FuzzyReflection.fromClass(BLOCK);
				FuzzyMethodContract contract = FuzzyMethodContract
						.newBuilder()
						.banModifier(Modifier.STATIC)
						.parameterExactArray(int.class)
						.returnTypeExact(IBLOCK_DATA)
						.build();
				FROM_LEGACY_DATA = Accessors.getMethodAccessor(fuzzy.getMethod(contract));

				contract = FuzzyMethodContract
						.newBuilder()
						.banModifier(Modifier.STATIC)
						.parameterExactArray(IBLOCK_DATA)
						.returnTypeExact(int.class)
						.build();
				TO_LEGACY_DATA = Accessors.getMethodAccessor(fuzzy.getMethod(contract, "toLegacyData"));

				fuzzy = FuzzyReflection.fromClass(MAGIC_NUMBERS);
				GET_NMS_BLOCK = Accessors.getMethodAccessor(fuzzy.getMethodByParameters("getBlock", BLOCK,
						new Class<?>[]{Material.class}));

				fuzzy = FuzzyReflection.fromClass(IBLOCK_DATA);
				GET_BLOCK = Accessors.getMethodAccessor(fuzzy.getMethodByParameters("getBlock", BLOCK,
						new Class<?>[0]));
			}
		}

		private OldBlockData(Object handle) {
			super(handle);
		}

		@Override
		public Material getType() {
			Object block = GET_BLOCK.invoke(handle);
			return BukkitConverters.getBlockConverter().getSpecific(block);
		}

		@Override
		public int getData() {
			Object block = GET_BLOCK.invoke(handle);
			return (Integer) TO_LEGACY_DATA.invoke(block, handle);
		}

		@Override
		public void setType(Material type) {
			setTypeAndData(type, 0);
		}

		@Override
		public void setData(int data) {
			setTypeAndData(getType(), data);
		}

		@Override
		public void setTypeAndData(Material type, int data) {
			Object nmsBlock = GET_NMS_BLOCK.invoke(null, type);
			Object blockData = FROM_LEGACY_DATA.invoke(nmsBlock, data);
			setHandle(blockData);
		}

		@Override
		public WrappedBlockData deepClone() {
			return WrappedBlockData.createData(getType(), getData());
		}

		private static WrappedBlockData createOldData(Material type) {
			Object blockData = GET_BLOCK.invoke(null, type);
			return new OldBlockData(blockData);
		}

		private static WrappedBlockData createOldData(Material type, int data) {
			Object nmsBlock = GET_NMS_BLOCK.invoke(null, type);
			Object blockData = FROM_LEGACY_DATA.invoke(nmsBlock, data);
			return new OldBlockData(blockData);
		}
	}

	public WrappedBlockData(Object handle) {
		super(IBLOCK_DATA);
		setHandle(handle);
	}

	/**
	 * Gets this BlockData's Bukkit material
	 * @return The Bukkit material
	 */
	public abstract Material getType();

	/**
	 * Gets this BlockData's legacy data
	 * @return The legacy data
	 * @deprecated By the flattening in 1.13. Fine on lower versions.
	 */
	@Deprecated
	public abstract int getData();

	/**
	 * Sets this BlockData's type
	 * @param material Bukkit material
	 */
	public abstract void setType(Material material);

	/**
	 * Sets this BlockData's legacy data
	 * @param data The new legacy data
	 * @deprecated By the flattening in 1.13. Fine on lower versions.
	 */
	@Deprecated
	public abstract void setData(int data);

	/**
	 * Sets this BlockData's type and legacy data
	 * @param material The new Bukkit material
	 * @param data The new legacy data
	 * @deprecated By the flattening in 1.13. Fine on lower versions.
	 */
	@Deprecated
	public abstract void setTypeAndData(Material material, int data);

	public abstract WrappedBlockData deepClone();

	/**
	 * Creates a new BlockData instance with the given type and no data.
	 * @param type Block type
	 * @return New BlockData
	 */
	public static WrappedBlockData createData(Material type) {
		return MinecraftVersion.atOrAbove(MinecraftVersion.AQUATIC_UPDATE) ? NewBlockData.createNewData(type)
		                                                                   : OldBlockData.createOldData(type);
	}

	/**
	 * Creates a new BlockData instance with the given type and data.
	 * @param type Block type
	 * @param data Block data
	 * @return New BlockData
	 * @deprecated The flattening
	 */
	@Deprecated
	public static WrappedBlockData createData(Material type, int data) {
		return MinecraftVersion.atOrAbove(MinecraftVersion.AQUATIC_UPDATE) ? NewBlockData.createNewData(type, data)
		                                                                   : OldBlockData.createOldData(type, data);
	}

	public static WrappedBlockData fromHandle(Object handle) {
		return MinecraftVersion.atOrAbove(MinecraftVersion.AQUATIC_UPDATE) ? new NewBlockData(handle)
		                                                                   : new OldBlockData(handle);
	}

	/**
	 * Creates a new Wrapped Block Data instance from a given Spigot Block Data
	 * @param data Spigot block data
	 * @return The new Wrapped Block Data
	 */
	public static WrappedBlockData createData(BlockData data) {
		return NewBlockData.createNewData(data);
	}

	@Override
	public String toString() {
		return "WrappedBlockData[handle=" + handle + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getType().hashCode();
		result = prime * result + getData();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (o instanceof WrappedBlockData) {
			WrappedBlockData that = (WrappedBlockData) o;
			return this.handle.equals(that.handle)
			       || (this.getType() == that.getType() && this.getData() == that.getData());
		}

		return false;
	}
}
