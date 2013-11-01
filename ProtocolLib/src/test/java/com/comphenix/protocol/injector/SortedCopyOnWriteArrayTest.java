/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.injector;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.comphenix.protocol.concurrency.SortedCopyOnWriteArray;
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
		
		// Test remove
		test.remove(b);
		assertEquals(2, test.size());
		assertFalse(test.contains(b));
	}
	
	private static class PriorityStuff implements Comparable<PriorityStuff> {
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
