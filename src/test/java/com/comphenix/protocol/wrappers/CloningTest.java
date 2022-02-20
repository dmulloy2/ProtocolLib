package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.cloning.AggregateCloner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CloningTest {

	@BeforeAll
	public static void initializeBukkit() {
		BukkitInitialization.initializeAll();
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
