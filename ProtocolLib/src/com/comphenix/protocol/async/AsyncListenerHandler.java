package com.comphenix.protocol.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

/**
 * Represents a handler for an asynchronous event.
 * 
 * @author Kristian
 */
public class AsyncListenerHandler {

	/**
	 * Signal an end to the packet processing.
	 */
	private static final PacketEvent INTERUPT_PACKET = new PacketEvent(new Object());
	
	// Default queue capacity
	private static int DEFAULT_CAPACITY = 1024;
	
	// Cancel the async handler
	private volatile boolean cancelled;
	
	// If we've started the listener loop before
	private volatile boolean started;
	
	// The packet listener
	private PacketListener listener;

	// The filter manager
	private AsyncFilterManager filterManager;
	
	// List of queued packets
	private ArrayBlockingQueue<PacketEvent> queuedPackets = new ArrayBlockingQueue<PacketEvent>(DEFAULT_CAPACITY);

	// Minecraft main thread
	private Thread mainThread;
	
	public AsyncListenerHandler(Thread mainThread, AsyncFilterManager filterManager, PacketListener listener) {
		if (filterManager == null)
			throw new IllegalArgumentException("filterManager cannot be NULL");
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be NULL");

		this.mainThread = mainThread;
		this.filterManager = filterManager;
		this.listener = listener;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

	public PacketListener getAsyncListener() {
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
		queuedPackets.add(INTERUPT_PACKET);
	}
	
	/**
	 * Queue a packet for processing.
	 * @param packet - a packet for processing.
	 * @throws IllegalStateException If the underlying packet queue is full.
	 */
	public void enqueuePacket(PacketEvent packet) {
		if (packet == null)
			throw new IllegalArgumentException("packet is NULL");
		
		queuedPackets.add(packet);
	}
	
	/**
	 * Create a runnable that will initiate the listener loop.
	 * <p>
	 * <b>Warning</b>: Never call the run() method in the main thread.
	 */
	public Runnable getListenerLoop() {
		return new Runnable() {
			@Override
			public void run() {
				listenerLoop();
			}
		};
	}
	
	// DO NOT call this method from the main thread
	private void listenerLoop() {
		
		// Danger, danger!
		if (Thread.currentThread().getId() == mainThread.getId()) 
			throw new IllegalStateException("Do not call this method from the main thread.");
		if (started) 
			throw new IllegalStateException("A listener cannot be run by multiple threads. Create a new listener instead.");
		if (cancelled)
			throw new IllegalStateException("Listener has been cancelled. Create a new listener instead.");
		
		// Proceed
		started = true;
		
		try {
			mainLoop:
			while (!cancelled) {
				PacketEvent packet = queuedPackets.take();
				AsyncMarker marker = packet.getAsyncMarker();
				
				// Handle cancel requests
				if (packet == null || marker == null || !packet.isAsynchronous()) {
					break;
				}
				
				// Here's the core of the asynchronous processing
				try {
					if (packet.isServerPacket())
						listener.onPacketSending(packet);
					else
						listener.onPacketReceiving(packet);
					
				} catch (Throwable e) {
					// Minecraft doesn't want your Exception.
					filterManager.getLogger().log(Level.SEVERE, 
							"Unhandled exception occured in onAsyncPacket() for " + getPluginName(), e);
				}
				
				// Now, get the next non-cancelled listener
				for (; marker.getListenerTraversal().hasNext(); ) {
					AsyncListenerHandler handler = marker.getListenerTraversal().next().getListener();
					
					if (!handler.isCancelled()) {
						handler.enqueuePacket(packet);
						continue mainLoop;
					}
				}
				
				// There are no more listeners - queue the packet for transmission
				filterManager.signalPacketUpdate(packet);
				filterManager.signalProcessingDone(packet);
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
			started = false;
		}
	}
	
	private String getPluginName() {
		return PacketAdapter.getPluginName(listener);
	}

	/**
	 * Retrieve the plugin associated with this async listener.
	 * @return The plugin.
	 */
	public Plugin getPlugin() {
		return listener != null ? listener.getPlugin() : null;
	}

	/**
	 * Start the asynchronous listener using the Bukkit scheduler.
	 */
	public void start() {
		if (listener.getPlugin() == null)
			throw new IllegalArgumentException("Cannot start task without a valid plugin.");
		
		filterManager.scheduleAsyncTask(listener.getPlugin(), getListenerLoop());
	}
}
