package com.comphenix.protocol;

import static org.junit.Assert.*;

import org.junit.Test;

public class MinecraftVersionTest {

	@Test
	public void testComparision() {
		MinecraftVersion within = new MinecraftVersion(1, 2, 5);
		MinecraftVersion outside = new MinecraftVersion(1, 7, 0);
		
		MinecraftVersion lower = new MinecraftVersion(1, 0, 0);
		MinecraftVersion highest = new MinecraftVersion(1, 4, 5);
		
		// Make sure this is valid
		assertTrue(lower.compareTo(within) < 0 && within.compareTo(highest) < 0);
		assertFalse(outside.compareTo(within) < 0 && outside.compareTo(highest) < 0);
	}
	
	public void testParsing() {
		assertEquals(MinecraftVersion.extractVersion("CraftBukkit R3.0 (MC: 1.4.3)"), "1.4.3");
		assertEquals(MinecraftVersion.extractVersion("CraftBukkit Test Beta 1 (MC: 1.10.01 )"), "1.10.01");
		assertEquals(MinecraftVersion.extractVersion("Hello (MC:   2.3.4 ) "), "2.3.4");
	}
}
