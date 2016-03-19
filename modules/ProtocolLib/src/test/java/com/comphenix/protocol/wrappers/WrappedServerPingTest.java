package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.WrappedServerPing.CompressedImage;
import com.google.common.io.Resources;

public class WrappedServerPingTest {

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializePackage();
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

			assertEquals(5, serverPing.getPlayersOnline());
			assertEquals(10, serverPing.getPlayersMaximum());
			assertEquals("Minecraft 123", serverPing.getVersionName());
			assertEquals(4, serverPing.getVersionProtocol());

			assertArrayEquals(original, serverPing.getFavicon().getData());

			CompressedImage copy = CompressedImage.fromBase64Png(Base64Coder.encodeLines(tux.getData()));
			assertArrayEquals(copy.getData(), serverPing.getFavicon().getData());
		} catch (Throwable ex) {
			if (ex.getCause() instanceof SecurityException) {
				// There was a global package seal for a while, but not anymore
				System.err.println("Encountered a SecurityException, update your Spigot jar!");
			} else {
				ex.printStackTrace();
				fail("Encountered an exception testing ServerPing");
			}
		}
	}
}