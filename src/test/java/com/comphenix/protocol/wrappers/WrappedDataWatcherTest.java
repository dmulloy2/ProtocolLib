/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2016 dmulloy2
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

import java.util.UUID;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

import net.minecraft.server.v1_16_R3.EntityEgg;

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEgg;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author dmulloy2
 */

public class WrappedDataWatcherTest {
	
	@BeforeClass
	public static void prepare() {
		BukkitInitialization.initializeItemMeta();
	}

	@Test
	public void testBytes() {
		// Create a fake lightning strike and get its watcher
		EntityEgg nmsEgg = new EntityEgg(null, 0, 0, 0);
		CraftEntity craftEgg = new CraftEgg(null, nmsEgg);
		WrappedDataWatcher wrapper = WrappedDataWatcher.getEntityWatcher(craftEgg);

		WrappedWatchableObject watchable = wrapper.getWatchableObject(0);
		WrappedDataWatcherObject object = watchable.getWatcherObject();

		// Make sure the serializers work
		assertEquals(object.getSerializer(), Registry.get(Byte.class));

		// Make sure we can set existing objects
		wrapper.setObject(0, (byte) 21);
		assertTrue(wrapper.getByte(0) == 21);
	}

	@Test
	public void testStrings() {
		WrappedDataWatcher wrapper = new WrappedDataWatcher();

		// Make sure we can create watcher objects
		Serializer serializer = Registry.get(String.class);
		WrappedDataWatcherObject object = new WrappedDataWatcherObject(3, serializer);
		wrapper.setObject(object, "Test");

		assertEquals(wrapper.getString(3), "Test");
	}

	@Test
	public void testFloats() {
		WrappedDataWatcher wrapper = new WrappedDataWatcher();

		// Make sure we can add new entries
		Serializer serializer = Registry.get(Float.class);
		WrappedDataWatcherObject object = new WrappedDataWatcherObject(10, serializer);
		wrapper.setObject(object, 21.0F);

		assertTrue(wrapper.hasIndex(10));
	}

	@Test
	public void testSerializers() {
		Serializer blockPos = Registry.get(net.minecraft.server.v1_16_R3.BlockPosition.class, false);
		Serializer optionalBlockPos = Registry.get(net.minecraft.server.v1_16_R3.BlockPosition.class, true);
		assertNotSame(blockPos, optionalBlockPos);

		// assertNull(Registry.get(ItemStack.class, false));
		assertNotNull(Registry.get(UUID.class, true));
	}

	@Test
	public void testHasIndex() {
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		Serializer serializer = Registry.get(Integer.class);

		assertFalse(watcher.hasIndex(0));
		watcher.setObject(0, serializer, 1);
		assertTrue(watcher.hasIndex(0));
	}

	@Test
	public void testDeepClone() {
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(0, Registry.get(Integer.class), 1);

		WrappedDataWatcher cloned = watcher.deepClone();
		assertEquals(1, cloned.asMap().size());
		assertEquals(1, (Object) cloned.getInteger(0));
	}
}
