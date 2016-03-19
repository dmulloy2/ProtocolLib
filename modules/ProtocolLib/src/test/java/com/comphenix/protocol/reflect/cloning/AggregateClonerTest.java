package com.comphenix.protocol.reflect.cloning;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

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
}
