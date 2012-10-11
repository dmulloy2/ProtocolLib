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

package com.comphenix.protocol.async;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

/**
 * Represents a handler for an asynchronous event.
 * 
 * @author Kristian
 */
public class AsyncListenerHandler {

	/**
	 * Signal an end to packet processing.
	 */
	private static final PacketEvent INTERUPT_PACKET = new PacketEvent(new Object());
	
	/**
	 * Called when the threads have to wake up for something important.
	 */
	private static final PacketEvent WAKEUP_PACKET = new PacketEvent(new Object());
	
	// Unique worker ID
	private static final AtomicInteger nextID = new AtomicInteger();
	
	// Default queue capacity
	private static int DEFAULT_CAPACITY = 1024;
	
	// Cancel the async handler
	private volatile boolean cancelled;
	
	// Number of worker threads
	private final AtomicInteger started = new AtomicInteger();
	
	// The packet listener
	private PacketListener listener;

	// The filter manager
	private AsyncFilterManager filterManager;
	private NullPacketListener nullPacketListener;
	
	// List of queued packets
	private ArrayBlockingQueue<PacketEvent> queuedPackets = new ArrayBlockingQueue<PacketEvent>(DEFAULT_CAPACITY);
	
	// List of cancelled tasks
	private final Set<Integer> stoppedTasks = new HashSet<Integer>();
	private final Object stopLock = new Object();
	
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
	 * Set the synchronized listener that has been automatically created.
	 * @param nullPacketListener - automatically created listener.
	 */
	void setNullPacketListener(NullPacketListener nullPacketListener) {
		this.nullPacketListener = nullPacketListener;
	}

	/**
	 * Retrieve the synchronized listener that was automatically created.
	 * @return Automatically created listener.
	 */
	PacketListener getNullPacketListener() {
		return nullPacketListener;
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
	 * Cancel the handler.
	 */
	public void cancel() {
		// Remove the listener as quickly as possible
		close();
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
	 * Create a worker that will initiate the listener loop. Note that using stop() to
	 * close a specific worker is less efficient than stopping an arbitrary worker.
	 * <p>
	 * <b>Warning</b>: Never call the run() method in the main thread.
	 */
	public AsyncRunnable getListenerLoop() {
		return new AsyncRunnable() {

			private final AtomicBoolean firstRun = new AtomicBoolean();
			private final AtomicBoolean finished = new AtomicBoolean();
			private final int id = nextID.incrementAndGet();
			
			@Override
			public int getID() {
				return id;
			}
			
			@Override
			public void run() {
				// Careful now
				if (firstRun.compareAndSet(false, true)) {
					listenerLoop(id);
					
					synchronized (stopLock) {
						stoppedTasks.remove(id);
						stopLock.notifyAll();
						finished.set(true);
					}
					
				} else {
					if (finished.get())
						throw new IllegalStateException(
							"This listener has already been run. Create a new instead.");
					else
						throw new IllegalStateException(
							"This listener loop has already been started. Create a new instead.");
				}
			}
			
			@Override
			public boolean stop() throws InterruptedException {
				synchronized (stopLock) {
					if (!isRunning())
						return false;

					stoppedTasks.add(id);
			
					// Wake up threads - we have a listener to stop
					for (int i = 0; i < getWorkers(); i++) {
						queuedPackets.offer(WAKEUP_PACKET);
					}
					
					finished.set(true);
					waitForStops();
					return true;
				}
			}

			@Override
			public boolean isRunning() {
				return firstRun.get() && !finished.get();
			}
			
			@Override
			public boolean isFinished() {
				return finished.get();
			}
		};
	}
	
	/**
	 * Start a singler worker thread handling the asynchronous listener.
	 */
	public synchronized void start() {
		if (listener.getPlugin() == null)
			throw new IllegalArgumentException("Cannot start task without a valid plugin.");
		if (cancelled)
			throw new IllegalStateException("Cannot start a worker when the listener is closing.");
		
		final AsyncRunnable listenerLoop = getListenerLoop();
		
		filterManager.scheduleAsyncTask(listener.getPlugin(), new Runnable() {
			@Override
			public void run() {
				Thread thread = Thread.currentThread();
				
				String previousName = thread.getName();
				String workerName = getFriendlyWorkerName(listenerLoop.getID());

				// Add the friendly worker name
				thread.setName(workerName);
				listenerLoop.run();
				thread.setName(previousName);
			}
		});
	}
	
	/**
	 * Start a singler worker thread handling the asynchronous listener.
	 * <p>
	 * This method is intended to allow callers to customize the thread priority
	 * before the worker loop is actually called. This is simpler than to
	 * schedule the worker threads manually.
	 * <pre><code>
	 * listenerHandler.start(new Function&lt;AsyncRunnable, Void&gt;() {
	 *     &#64;Override
	 *     public Void apply(&#64;Nullable AsyncRunnable workerLoop) {
	 *         Thread thread = Thread.currentThread();
	 *         int prevPriority = thread.getPriority();
	 *	       
	 *         thread.setPriority(Thread.MIN_PRIORITY);
	 *         workerLoop.run();
	 *         thread.setPriority(prevPriority);
	 *         return null;
	 *     }
	 *   });
	 * }
	 * </code></pre>
	 * @param executor - a method that will execute the given listener loop.
	 */
	public synchronized void start(Function<AsyncRunnable, Void> executor) {
		if (listener.getPlugin() == null)
			throw new IllegalArgumentException("Cannot start task without a valid plugin.");
		if (cancelled)
			throw new IllegalStateException("Cannot start a worker when the listener is closing.");
		
		final AsyncRunnable listenerLoop = getListenerLoop();
		final Function<AsyncRunnable, Void> delegateCopy = executor;
		
		filterManager.scheduleAsyncTask(listener.getPlugin(), new Runnable() {
			@Override
			public void run() {
				delegateCopy.apply(listenerLoop);
			}
		});
	}
	
	/**
	 * Create a friendly thread name using the following convention:
	 * <p><code>
	 * &nbsp;&nbsp;&nbsp;&nbsp;Protocol Worker {id} - {plugin} - [recv: {packets}, send: {packets}]
	 * </code></p>
	 * @param id - the worker ID.
	 * @return A friendly thread name.
	 */
	public String getFriendlyWorkerName(int id) {
		return String.format("Protocol Worker #%s - %s - [recv: %s, send: %s]", 
				id, 
				PacketAdapter.getPluginName(listener), 
				fromWhitelist(listener.getReceivingWhitelist()),
				fromWhitelist(listener.getSendingWhitelist())
		);
	}
	
	/**
	 * Convert the given whitelist to a comma-separated list of packet IDs.
	 * @param whitelist - the whitelist.
	 * @return A comma separated list of packet IDs in the whitelist, or the emtpy string.
	 */
	private String fromWhitelist(ListeningWhitelist whitelist) {
		if (whitelist == null)
			return "";
		else
			return Joiner.on(", ").join(whitelist.getWhitelist());
	}
	
	/**
	 * Start multiple worker threads for this listener.
	 * @param count - number of worker threads to start.
	 */
	public synchronized void start(int count) {
		for (int i = 0; i < count; i++)
			start();
	}
	
	/**
	 * Stop a worker thread.
	 */
	public synchronized void stop() {
		queuedPackets.add(INTERUPT_PACKET);
	}
	
	/**
	 * Stop the given amount of worker threads.
	 * @param count - number of threads to stop.
	 */
	public synchronized void stop(int count) {
		for (int i = 0; i < count; i++)
			stop();
	}
	
	/**
	 * Set the current number of workers. 
	 * <p>
	 * This method can only be called with a count of zero when the listener is closing.
	 * @param count - new number of workers.
	 */
	public synchronized void setWorkers(int count) {
		if (count < 0)
			throw new IllegalArgumentException("Number of workers cannot be less than zero.");
		if (count > DEFAULT_CAPACITY)
			throw new IllegalArgumentException("Cannot initiate more than " + DEFAULT_CAPACITY + " workers");
		if (cancelled && count > 0)
			throw new IllegalArgumentException("Cannot add workers when the listener is closing.");
		
		long time = System.currentTimeMillis();
		
		// Try to get to the correct count
		while (started.get() != count) {
			if (started.get() < count)
				start();
			else
				stop();
			
			// May happen if another thread is doing something similar to "setWorkers"
			if ((System.currentTimeMillis() - time) > 50)
				throw new RuntimeException("Failed to set worker count.");
		}
	}
	
	/**
	 * Retrieve the current number of registered workers.
	 * <p>
	 * Note that the returned value may be out of data.
	 * @return Number of registered workers.
	 */
	public synchronized int getWorkers() {
		return started.get();
	}
	
	/**
	 * Wait until every tasks scheduled to stop has actually stopped.
	 * @return TRUE if the current listener should stop, FALSE otherwise.
	 * @throws InterruptedException - If the current thread was interrupted.
	 */
	private boolean waitForStops() throws InterruptedException {
		synchronized (stopLock) {
			while (stoppedTasks.size() > 0 && !cancelled) {
				stopLock.wait();
			}
			return cancelled;
		}
	}
	
	// DO NOT call this method from the main thread
	private void listenerLoop(int workerID) {
		
		// Danger, danger!
		if (Thread.currentThread().getId() == mainThread.getId()) 
			throw new IllegalStateException("Do not call this method from the main thread.");
		if (cancelled)
			throw new IllegalStateException("Listener has been cancelled. Create a new listener instead.");

		try {
			// Wait if certain threads are stopping
			if (waitForStops())
				return;
			
			// Proceed
			started.incrementAndGet();
			
			mainLoop:
			while (!cancelled) {
				PacketEvent packet = queuedPackets.take();
				AsyncMarker marker = packet.getAsyncMarker();
				
				// Handle cancel requests
				if (packet == null || marker == null || packet == INTERUPT_PACKET) {
					return;
					
				} else if (packet == WAKEUP_PACKET) {
					// This is a bit slow, but it should be safe
					synchronized (stopLock) {
						// Are we the one who is supposed to stop?
						if (stoppedTasks.contains(workerID)) 
							return;
						if (waitForStops())
							return;
					}
				}
				
				// Here's the core of the asynchronous processing
				try {
					marker.setListenerHandler(this);
					marker.setWorkerID(workerID);
					
					synchronized (marker.getProcessingLock()) {
						if (packet.isServerPacket())
							listener.onPacketSending(packet);
						else
							listener.onPacketReceiving(packet);
					}
					
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
				filterManager.signalFreeProcessingSlot(packet);
				
				// Note that listeners can opt to delay the packet transmission
				filterManager.signalPacketTransmission(packet);
			}
			
		} catch (InterruptedException e) {
			// We're done
		} finally {
			// Clean up
			started.decrementAndGet();
			close();
		}
	}
	
	private synchronized void close() {
		// Remove the listener itself
		if (!cancelled) {
			filterManager.unregisterAsyncHandlerInternal(this);
			cancelled = true;
			
			// Tell every uncancelled thread to end
			stopThreads();
		}
	}
	
	/**
	 * Use the poision pill method to stop every worker thread.
	 */
	private void stopThreads() {
		// Poison Pill Shutdown
		queuedPackets.clear();
		stop(started.get());
		
		// Individual shut down is irrelevant now
		synchronized (stopLock) {
			stopLock.notifyAll();
		}
	}
}
