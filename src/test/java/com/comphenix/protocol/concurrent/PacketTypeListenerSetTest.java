package com.comphenix.protocol.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.ImmutableSet;

public class PacketTypeListenerSetTest {

	@Test
	public void test() throws Exception {
		BukkitInitialization.initializeAll();

		PacketTypeListenerSet set = new PacketTypeListenerSet();

		DummyPacketListener a = new DummyPacketListener();
		DummyPacketListener b = new DummyPacketListener();

		assertTrue(set.add(PacketType.Login.Client.START, a));
		assertFalse(set.add(PacketType.Login.Client.START, a));
		assertTrue(set.add(PacketType.Login.Client.START, b));

		assertTrue(set.contains(PacketType.Login.Client.START));
		assertTrue(set.contains(PacketType.Login.Client.START.getPacketClass()));
		assertEquals(ImmutableSet.of(PacketType.Login.Client.START), set.values());

		assertTrue(set.remove(PacketType.Login.Client.START, a));
		assertFalse(set.remove(PacketType.Login.Client.START, a));
		assertTrue(set.remove(PacketType.Login.Client.START, b));
		assertFalse(set.remove(PacketType.Login.Client.START, b));

		assertFalse(set.contains(PacketType.Login.Client.START));
		assertFalse(set.contains(PacketType.Login.Client.START.getPacketClass()));
		assertEquals(ImmutableSet.of(), set.values());

		assertTrue(set.add(PacketType.Login.Client.START, a));
		set.clear();

		assertFalse(set.contains(PacketType.Login.Client.START));
		assertFalse(set.contains(PacketType.Login.Client.START.getPacketClass()));
		assertEquals(ImmutableSet.of(), set.values());
	}

	private class DummyPacketListener implements PacketListener {

		@Override
		public void onPacketSending(PacketEvent event) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void onPacketReceiving(PacketEvent event) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ListeningWhitelist getSendingWhitelist() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ListeningWhitelist getReceivingWhitelist() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Plugin getPlugin() {
			throw new UnsupportedOperationException();
		}
	}
}
