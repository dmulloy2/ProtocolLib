package com.comphenix.protocol.utility;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachedPackageTest {
	private CachedPackage pack;

	@BeforeEach
	public void prepare() {
		ClassSource source = ClassSource.fromClassLoader();
		this.pack = new CachedPackage("java.lang", source);
	}

	@Test
	public void testGetPackageClass() {
		Optional<Class<?>> result = pack.getPackageClass("Object");
		assertTrue(result.isPresent());
		assertEquals(result.get(), Object.class);
	}

	@Test
	public void testUsingAliases() {
		Optional<Class<?>> result = pack.getPackageClass("NOT_A_CLASS", "Object");
		assertTrue(result.isPresent());
		assertEquals(result.get(), Object.class);

		result = pack.getPackageClass("NOT_A_CLASS", "STILL_NOT_A_CLASS", "Object");
		assertTrue(result.isPresent());
		assertEquals(result.get(), Object.class);
	}
}
