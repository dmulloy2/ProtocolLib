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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import net.minecraft.server.v1_11_R1.EntityLightning;

import org.bukkit.craftbukkit.v1_11_R1.entity.CraftLightningStrike;
import org.bukkit.entity.Entity;
import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

/**
 * @author dmulloy2
 */

public class WrappedDataWatcherTest {
	
	@BeforeClass
	public static void prepare() {
		BukkitInitialization.initializePackage();
	}

	@Test
	public void testBytes() {
		// Create a fake lightning strike and get its watcher
		EntityLightning lightning = new EntityLightning(null, 0, 0, 0, true);
		Entity entity = new CraftLightningStrike(null, lightning);
		WrappedDataWatcher wrapper = WrappedDataWatcher.getEntityWatcher(entity);

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
		Serializer blockPos = Registry.get(net.minecraft.server.v1_11_R1.BlockPosition.class, false);
		Serializer optionalBlockPos = Registry.get(net.minecraft.server.v1_11_R1.BlockPosition.class, true);
		assertNotSame(blockPos, optionalBlockPos);

		// assertNull(Registry.get(ItemStack.class, false));
		assertNotNull(Registry.get(UUID.class, true));
	}
}
