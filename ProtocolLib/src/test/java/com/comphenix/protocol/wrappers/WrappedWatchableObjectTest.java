package com.comphenix.protocol.wrappers;

import static org.junit.Assert.*;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemFactory;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.comphenix.protocol.BukkitInitialization;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PowerMockIgnore({ "org.apache.log4j.*", "org.apache.logging.*", "org.bukkit.craftbukkit.libs.jline.*" })
@PrepareForTest(CraftItemFactory.class)
public class WrappedWatchableObjectTest {
	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializeItemMeta();
	}
	
	@Test
	public void testItemStack() {
		final ItemStack stack = new ItemStack(Material.GOLD_AXE);
		final WrappedWatchableObject test = new WrappedWatchableObject(0, stack);
	
		ItemStack value = (ItemStack) test.getValue();
		assertEquals(value.getType(), stack.getType());
	}
}
