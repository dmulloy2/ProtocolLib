package com.comphenix.protocol.async;

import java.util.concurrent.ArrayBlockingQueue;

public class ListenerToken {

	// Cancel the async handler
	private volatile boolean cancelled;

	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Cancel the handler.
	 */
	public void cancel() {
		cancelled = true;
	}
	

	public void beginListener(AsyncListener asyncListener) {
		
		try {
			AsyncPacket packet = processingQueue.take();
			
			// Now, 
			asyncListener.onAsyncPacket(packet);
			
			
		} catch (InterruptedException e) {
			
		}
		
		
		
	}
}
