package com.comphenix.protocol.wrappers;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

public class WrappedChunkCoordinateTest {
	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}
	
	@Test
	public void test() {
		WrappedChunkCoordinate coordinate = new WrappedChunkCoordinate(1, 2, 3);
		
		assertEquals(1, coordinate.getX());
		assertEquals(2, coordinate.getY());
		assertEquals(3, coordinate.getZ());
	}
}
