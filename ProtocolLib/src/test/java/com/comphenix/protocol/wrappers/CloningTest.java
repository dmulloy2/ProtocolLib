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
		WrappedGameProfile profile = new WrappedGameProfile("id", "name");
		WrappedGameProfile copy = WrappedGameProfile.fromHandle(
			AggregateCloner.DEFAULT.clone(profile.getHandle())
		);
		
		assertEquals(profile, copy);
	}
}
