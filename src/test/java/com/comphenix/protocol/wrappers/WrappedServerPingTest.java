package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.WrappedServerPing.CompressedImage;
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class WrappedServerPingTest {

	@BeforeAll
	public static void initializeBukkit() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void test() {
		try {
			CompressedImage tux = CompressedImage.fromPng(Resources.getResource("tux.png").openStream());
			byte[] original = tux.getDataCopy();

			WrappedServerPing serverPing = new WrappedServerPing();
			serverPing.setMotD("Hello, this is a test.");
			serverPing.setPlayersOnline(5);
			serverPing.setPlayersMaximum(10);
			serverPing.setVersionName("Minecraft 123");
			serverPing.setVersionProtocol(4);
			serverPing.setFavicon(tux);
			serverPing.setEnforceSecureChat(true);

			assertEquals(5, serverPing.getPlayersOnline());
			assertEquals(10, serverPing.getPlayersMaximum());
			assertEquals("Minecraft 123", serverPing.getVersionName());
			assertEquals(4, serverPing.getVersionProtocol());
			assertTrue(serverPing.isEnforceSecureChat());

			assertArrayEquals(original, serverPing.getFavicon().getData());

			CompressedImage copy = CompressedImage.fromBase64Png(Base64Coder.encodeLines(tux.getData()));
			assertArrayEquals(copy.getData(), serverPing.getFavicon().getData());
		} catch (Throwable ex) {
			fail("Encountered an exception testing ServerPing", ex);
		}
	}
}
