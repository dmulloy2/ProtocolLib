package com.comphenix.protocol.injector;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.comphenix.protocol.events.ListenerPriority;
import com.google.common.primitives.Ints;

public class SortedCopyOnWriteArrayTest {

	@Test
	public void testInsertion() {
		
		final int MAX_NUMBER = 50;

		SortedCopyOnWriteArray<Integer> test = new SortedCopyOnWriteArray<Integer>();
		
		// Generate some numbers
		List<Integer> numbers = new ArrayList<Integer>();
		
		for (int i = 0; i < MAX_NUMBER; i++) {
			numbers.add(i);
		}
		
		// Random insertion to test it all
		Collections.shuffle(numbers);
		
		// O(n^2) of course, so don't use too large numbers
		for (int i = 0; i < MAX_NUMBER; i++) {
			test.add(numbers.get(i));
		}
		
		// Check that everything is correct
		for (int i = 0; i < MAX_NUMBER; i++) {
			assertEquals((Integer) i, test.get(i));
		}
	}
	
	@Test
	public void testOrder() {
		PriorityStuff a = new PriorityStuff(ListenerPriority.HIGH, 1);
		PriorityStuff b = new PriorityStuff(ListenerPriority.NORMAL, 2);
		PriorityStuff c = new PriorityStuff(ListenerPriority.NORMAL, 3);
		SortedCopyOnWriteArray<PriorityStuff> test = new SortedCopyOnWriteArray<PriorityStuff>();
		
		test.add(a);
		test.add(b);
		test.add(c);
		
		// Make sure the normal's are in the right order
		assertEquals(2, test.get(0).id);
		assertEquals(3, test.get(1).id);
	}
	
	private class PriorityStuff implements Comparable<PriorityStuff> {
		public ListenerPriority priority;
		public int id;

		public PriorityStuff(ListenerPriority priority, int id) {
			this.priority = priority;
			this.id = id;
		}

		@Override
		public int compareTo(PriorityStuff other) {
			// This ensures that lower priority listeners are executed first
			return Ints.compare(priority.getSlot(),
					            other.priority.getSlot());
		}
	}
}
