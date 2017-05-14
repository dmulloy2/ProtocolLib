package com.comphenix.protocol.reflect.cloning;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NonNullList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class AggregateClonerTest {

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializePackage();
	}
	
	@Test
	public void testArrays() {		
		List<Integer> input = Arrays.asList(1, 2, 3);
		assertEquals(input, AggregateCloner.DEFAULT.clone(input));
	}

	@Test
	public void testNonNullList() {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);

		NonNullList<ItemStack> list = NonNullList.a(16, ItemStack.a);
		packet.getModifier().write(1, list);

		PacketContainer cloned = packet.deepClone();

		@SuppressWarnings("unchecked")
		NonNullList<ItemStack> list1 = (NonNullList<ItemStack>) cloned.getModifier().read(1);

		assertEquals(list.size(), list1.size());
		assertArrayEquals(list.toArray(), list1.toArray());
	}
}
