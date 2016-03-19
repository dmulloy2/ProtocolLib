/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import net.minecraft.server.v1_9_R1.DataWatcher;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityLightning;
import net.minecraft.server.v1_9_R1.ItemStack;

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
		WrappedDataWatcher wrapper = create();
		WrappedWatchableObject watchable = wrapper.getWatchableObject(0);
		WrappedDataWatcherObject object = watchable.getWatcherObject();

		// Make sure the serializers work
		assertEquals(object.getSerializer(), Registry.get(Byte.class));

		// Make sure we can set existing objects
		wrapper.setObject(0, (byte) 1);
		assertTrue(wrapper.getByte(0) == 1);
	}

	@Test
	public void testStrings() {
		WrappedDataWatcher wrapper = create();

		// Make sure we can create watcher objects
		Serializer serializer = Registry.get(String.class);
		WrappedDataWatcherObject object = new WrappedDataWatcherObject(3, serializer);
		wrapper.setObject(object, "Test");

		assertEquals(wrapper.getString(3), "Test");
	}

	@Test
	public void testFloats() {
		WrappedDataWatcher wrapper = create();

		// Make sure we can add new entries
		Serializer serializer = Registry.get(Float.class);
		WrappedDataWatcherObject object = new WrappedDataWatcherObject(10, serializer);
		wrapper.setObject(object, 1.0F);

		assertTrue(wrapper.hasIndex(10));
	}

	private WrappedDataWatcher create() {
		Entity entity = new EntityLightning(null, 0, 0, 0, true);
		DataWatcher handle = entity.getDataWatcher();
		return new WrappedDataWatcher(handle);
	}

	@Test
	public void testSerializers() {
		Serializer blockPos = Registry.get(net.minecraft.server.v1_9_R1.BlockPosition.class, false);
		Serializer optionalBlockPos = Registry.get(net.minecraft.server.v1_9_R1.BlockPosition.class, true);
		assertNotSame(blockPos, optionalBlockPos);

		assertNull(Registry.get(ItemStack.class, false));
		assertNotNull(Registry.get(UUID.class, true));
	}
}