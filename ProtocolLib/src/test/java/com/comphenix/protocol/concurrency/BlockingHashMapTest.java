package com.comphenix.protocol.concurrency;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class BlockingHashMapTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {

		final BlockingHashMap<Integer, String> map = BlockingHashMap.create();

		ExecutorService service = Executors.newSingleThreadExecutor();
		
		// Create a reader
		Future<String> future = service.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				// Combine for easy reading
				return map.get(0) + map.get(1);
			}
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
