package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.EquivalentConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BukkitConvertersTest {

	@BeforeAll
	public static void beforeClass() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void testItemStacks() {
		ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 16);
		item.addEnchantment(Enchantment.DAMAGE_ALL, 4);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Diamond Sword");
		item.setItemMeta(meta);

		EquivalentConverter<ItemStack> converter = BukkitConverters.getItemStackConverter();
		Object nmsStack = converter.getGeneric(item);
		ItemStack back = converter.getSpecific(nmsStack);

		assertEquals(item.getType(), back.getType());
		assertEquals(item.getDurability(), back.getDurability());
		assertEquals(item.hasItemMeta(), back.hasItemMeta());
		assertTrue(Bukkit.getItemFactory().equals(item.getItemMeta(), back.getItemMeta()));
	}
}
