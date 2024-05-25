package com.comphenix.protocol.concurrent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.google.common.collect.ImmutableSet;

public class PacketTypeMultiMapTest {

	@Test
	public void test() {
		BukkitInitialization.initializeAll();

		PacketTypeMultiMap<Integer> map = new PacketTypeMultiMap<>();

		ListeningWhitelist a = ListeningWhitelist.newBuilder()
				.priority(ListenerPriority.NORMAL)
				.types(PacketType.Login.Client.START, PacketType.Login.Client.ENCRYPTION_BEGIN)
				.build();

		ListeningWhitelist b = ListeningWhitelist.newBuilder()
				.priority(ListenerPriority.NORMAL)
				.types(PacketType.Login.Server.SUCCESS, PacketType.Login.Server.ENCRYPTION_BEGIN)
				.build();

		ListeningWhitelist c = ListeningWhitelist.newBuilder(a)
				.priority(ListenerPriority.HIGH)
				.build();

		ListeningWhitelist d = ListeningWhitelist.newBuilder(a)
				.priority(ListenerPriority.LOW)
				.build();

		assertFalse(map.contains(PacketType.Login.Client.START));
		map.put(a, 1);
		map.put(c, 1);
		map.put(b, 2);
		map.put(c, 3);
		map.put(d, 4);

		assertTrue(map.contains(PacketType.Login.Client.START));
		assertEquals(ImmutableSet.of(
				PacketType.Login.Client.START,
				PacketType.Login.Client.ENCRYPTION_BEGIN,
				PacketType.Login.Server.SUCCESS,
				PacketType.Login.Server.ENCRYPTION_BEGIN
		), map.getPacketTypes());

		Iterator<Integer> iterator = map.get(PacketType.Login.Client.START).iterator();
		assertTrue(iterator.hasNext());
		assertEquals(4, iterator.next());
		assertEquals(1, iterator.next());
		assertEquals(3, iterator.next());
		assertFalse(iterator.hasNext());
		assertThrows(NoSuchElementException.class, () -> iterator.next());

		map.remove(a, 1);
		map.remove(a, 2); // try to remove a element with the wrong packet types

		Iterator<Integer> iteratorB = map.get(PacketType.Login.Client.START).iterator();
		assertEquals(4, iteratorB.next());
		assertEquals(3, iteratorB.next());
		assertThrows(NoSuchElementException.class, () -> iteratorB.next());

		map.remove(a, 3);
		map.remove(a, 4);
		map.remove(a, 4); // try to remove something that isn't even there
		assertFalse(map.contains(PacketType.Login.Client.START));

		map.put(a, 1);
		map.clear();
		assertTrue(map.getPacketTypes().isEmpty());

		Iterator<Integer> iteratorC = map.get(PacketType.Login.Client.START).iterator();
		assertFalse(iteratorC.hasNext());
		assertThrows(NoSuchElementException.class, () -> iteratorC.next());
	}
}
