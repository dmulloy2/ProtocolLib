package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

public class WrappedIntHashMapTest {

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializePackage();
	}
	
	@Test
	public void testIntMap() {
		WrappedIntHashMap test = WrappedIntHashMap.newMap();
		test.put(1, "hello");
		
		assertNull(test.get(0));
		assertEquals(test.get(1), "hello");
	}
}
