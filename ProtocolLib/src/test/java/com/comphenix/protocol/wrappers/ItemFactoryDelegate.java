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

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemFactory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author dmulloy2
 */

public class ItemFactoryDelegate implements ItemFactory {
	private final CraftItemFactory factory;
	private final ItemMeta mocked;

	public ItemFactoryDelegate(ItemMeta mocked) {
		this.factory = CraftItemFactory.instance();
		this.mocked = mocked;
	}

	@Override
	public ItemMeta asMetaFor(ItemMeta meta, ItemStack stack) throws IllegalArgumentException {
		return factory.asMetaFor(meta, stack);
	}

	@Override
	public ItemMeta asMetaFor(ItemMeta meta, Material material) throws IllegalArgumentException {
		return factory.asMetaFor(meta, material);
	}

	@Override
	public boolean equals(ItemMeta meta1, ItemMeta meta2) throws IllegalArgumentException {
		return factory.equals(meta1, meta2);
	}

	@Override
	public Color getDefaultLeatherColor() {
		return factory.getDefaultLeatherColor();
	}

	@Override
	public ItemMeta getItemMeta(Material arg0) {
		return mocked;
	}

	@Override
	public boolean isApplicable(ItemMeta meta, ItemStack itemstack) throws IllegalArgumentException {
		return factory.isApplicable(meta, itemstack);
	}

	@Override
	public boolean isApplicable(ItemMeta meta, Material material) throws IllegalArgumentException {
		return factory.isApplicable(meta, material);
	}
}