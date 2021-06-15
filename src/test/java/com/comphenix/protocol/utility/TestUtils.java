package com.comphenix.protocol.utility;

import java.lang.reflect.Field;
import java.util.List;

import sun.misc.Unsafe;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class TestUtils {

	public static void assertItemCollectionsEqual(List<ItemStack> first, List<ItemStack> second) {
		assertEquals(first.size(), second.size());
		for (int i = 0; i < first.size(); i++) {
			assertItemsEqual(first.get(i), second.get(i));
		}
	}

	public static void assertItemsEqual(ItemStack first, ItemStack second) {
		if (first == null) {
			assertNull(second);
		} else {
			assertNotNull(first);

			// The legacy check in ItemStack#isSimilar causes a null pointer
			assertEquals(first.getType(), second.getType());
			assertEquals(first.getDurability(), second.getDurability());
			assertEquals(first.hasItemMeta(), second.hasItemMeta());
			if (first.hasItemMeta()) {
				assertTrue(Bukkit.getItemFactory().equals(first.getItemMeta(), second.getItemMeta()));
			}
		}
	}

	public static boolean equivalentItem(ItemStack first, ItemStack second) {
		if (first == null) {
			return second == null;
		} else if (second == null) {
			return false;
		} else {
			return first.getType().equals(second.getType());
		}
	}

	public static void setFinalField(Object obj, Field field, Object newValue) throws ReflectiveOperationException {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		Unsafe unsafe = (Unsafe) unsafeField.get(null);

		long offset = unsafe.objectFieldOffset(field);
		unsafe.putObject(obj, offset, newValue);
	}
}
