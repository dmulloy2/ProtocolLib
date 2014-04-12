package com.comphenix.protocol.wrappers;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.cloning.AggregateCloner;

public class CloningTest {
	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}
	
	@Test
	public void cloneGameProfile() {
		WrappedGameProfile profile = new WrappedGameProfile("8817d9ec-72e6-4abe-a496-cda667c3efe1", "name");
		WrappedGameProfile copy = WrappedGameProfile.fromHandle(
			AggregateCloner.DEFAULT.clone(profile.getHandle())
		);
		
		assertEquals(profile, copy);
	}
}
