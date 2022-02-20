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

package com.comphenix.protocol.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

public class BlockingHashMapTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {

		final BlockingHashMap<Integer, String> map = BlockingHashMap.create();

		ExecutorService service = Executors.newSingleThreadExecutor();

		// Create a reader
		Future<String> future = service.submit(() -> {
			// Combine for easy reading
			return map.get(0) + map.get(1);
		});

		// Wait a bit
		Thread.sleep(50);

		// Insert values
		map.put(0, "hello ");
		map.put(1, "world");

		// Wait for the other thread to complete
		assertEquals(future.get(), "hello world");
	}
}
