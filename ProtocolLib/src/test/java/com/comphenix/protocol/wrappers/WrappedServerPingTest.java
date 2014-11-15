package com.comphenix.protocol.wrappers;

import org.junit.BeforeClass;

import com.comphenix.protocol.BukkitInitialization;

public class WrappedServerPingTest {
	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}

//	@Test
//	public void test() throws IOException {
//		CompressedImage tux = CompressedImage.fromPng(Resources.getResource("tux.png").openStream());
//		byte[] original = tux.getDataCopy();
//
//		WrappedServerPing serverPing = new WrappedServerPing();
//		serverPing.setMotD("Hello, this is a test.");
//		serverPing.setPlayersOnline(5);
//		serverPing.setPlayersMaximum(10);
//		serverPing.setVersionName("Minecraft 123");
//		serverPing.setVersionProtocol(4);
//		serverPing.setFavicon(tux);
//
//		assertEquals(5, serverPing.getPlayersOnline());
//		assertEquals(10, serverPing.getPlayersMaximum());
//		assertEquals("Minecraft 123", serverPing.getVersionName());
//		assertEquals(4, serverPing.getVersionProtocol());
//
//		assertArrayEquals(original, serverPing.getFavicon().getData());
//
//		CompressedImage copy = CompressedImage.fromBase64Png(Base64Coder.encodeLines(tux.getData()));
//		assertArrayEquals(copy.getData(), serverPing.getFavicon().getData());
//	}

}
