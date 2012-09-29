package com.comphenix.protocol.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

public class ListenerToken {

	// Default queue capacity
	private static int DEFAULT_CAPACITY = 1024;
	
	// Cancel the async handler
	private volatile boolean cancelled;
	
	// The packet listener
	private AsyncListener listener;
	
	// The original plugin
	private Plugin plugin;
	
	// The filter manager
	private AsyncFilterManager filterManager;
	
	// List of queued packets
	private ArrayBlockingQueue<AsyncPacket> queuedPackets = new ArrayBlockingQueue<AsyncPacket>(DEFAULT_CAPACITY);

	// Minecraft main thread
	private Thread mainThread;
	
	public ListenerToken(Plugin plugin, Thread mainThread, AsyncFilterManager filterManager, AsyncListener listener) {
		if (filterManager == null)
			throw new IllegalArgumentException("filterManager cannot be NULL");
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be NULL");
		
		this.plugin = plugin;
		this.mainThread = mainThread;
		this.filterManager = filterManager;
		this.listener = listener;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

	public AsyncListener getAsyncListener() {
		return listener;
	}

	/**
	 * Cancel the handler.
	 */
	public void cancel() {
		// Remove the listener as quickly as possible
		close();
		
		// Poison Pill Shutdown
		queuedPackets.clear();
		queuedPackets.add(AsyncPacket.INTERUPT_PACKET);
	}
	
	/**
	 * Queue a packet for processing.
	 * @param packet - a packet for processing.
	 * @throws IllegalStateException If the underlying packet queue is full.
	 */
	public void enqueuePacket(AsyncPacket packet) {
		if (packet == null)
			throw new IllegalArgumentException("packet is NULL");
		
		queuedPackets.add(packet);
	}
	
	/**
	 * Entry point for the background thread that will be processing the packet asynchronously.
	 * <p>
	 * <b>WARNING:</b>
	 * Never call this method from the main thread. Doing so will block Minecraft.
	 */
	public void listenerLoop() {
		// Danger, danger!
		if (Thread.currentThread().getId() == mainThread.getId()) 
			throw new IllegalStateException("Do not call this method from the main thread.");
		
		try {
			mainLoop:
			while (!cancelled) {
				AsyncPacket packet = queuedPackets.take();
				
				// Handle cancel requests
				if (packet == null || packet.isInteruptPacket()) {
					break;
				}
				
				// Here's the core of the asynchronous processing
				try {
					listener.onAsyncPacket(packet);
				} catch (Throwable e) {
					// Minecraft doesn't want your Exception.
					filterManager.getLogger().log(Level.SEVERE, 
							"Unhandled exception occured in onAsyncPacket() for " + getPluginName(), e);
				}
				
				// Now, get the next non-cancelled listener
				for (; packet.getListenerTraversal().hasNext(); ) {
					ListenerToken token = packet.getListenerTraversal().next().getListener();
					
					if (!token.isCancelled()) {
						token.enqueuePacket(packet);
						continue mainLoop;
					}
				}
				
				// There are no more listeners - queue the packet for transmission
				filterManager.getSendingQueue().signalPacketUpdate(packet);
				filterManager.getProcessingQueue().signalProcessingDone();
			}
			
		} catch (InterruptedException e) {
			// We're done
		}
		
		// Clean up
		close();
	}
	
	private void close() {
		// Remove the listener itself
		if (!cancelled) {
			filterManager.unregisterAsyncHandlerInternal(this);
			cancelled = true;
		}
	}
	
	private String getPluginName() {
		return plugin != null ? plugin.getName() : "UNKNOWN";
	}
}
