package com.comphenix.protocol.concurrent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class SortedCopyOnWriteSetTest {

	@Test
	public void test() {
		SortedCopyOnWriteSet<Integer, String> set = new SortedCopyOnWriteSet<>();

		assertTrue(set.add(1, "a"));
		assertTrue(set.add(4, "b"));
		assertTrue(set.add(3, "a"));
		assertFalse(set.add(1, "b"));
		assertTrue(set.add(2, "a"));
		assertFalse(set.isEmpty());

		Iterator<Integer> iterator = set.iterator();
		assertTrue(iterator.hasNext());

		assertEquals(1, iterator.next());
		assertEquals(3, iterator.next());
		assertEquals(2, iterator.next());
		assertEquals(4, iterator.next());

		assertFalse(iterator.hasNext());
		assertThrows(NoSuchElementException.class, () -> iterator.next());

		assertTrue(set.remove(1));
		assertTrue(set.remove(2));
		assertTrue(set.remove(3));
		assertTrue(set.remove(4));
		assertFalse(set.remove(1));
		assertTrue(set.isEmpty());
	}
}
