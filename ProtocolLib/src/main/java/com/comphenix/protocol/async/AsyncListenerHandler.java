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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimedListenerManager.ListenerType;
import com.comphenix.protocol.timing.TimedTracker;
import com.comphenix.protocol.utility.WrappedScheduler;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

/**
 * Represents a handler for an asynchronous event.
 * <p>
 * Use {@link AsyncMarker#incrementProcessingDelay()} to delay a packet until a certain condition has been met.
 * @author Kristian
 */
public class AsyncListenerHandler {
	public static final ReportType REPORT_HANDLER_NOT_STARTED = new ReportType(
		"Plugin %s did not start the asynchronous handler %s by calling start() or syncStart().");	

	/**
	 * Signal an end to packet processing.
	 */
	private static final PacketEvent INTERUPT_PACKET = new PacketEvent(new Object());
	
	/**
	 * Called when the threads have to wake up for something important.
	 */
	private static final PacketEvent WAKEUP_PACKET = new PacketEvent(new Object());
	
	/**
	 * The expected number of ticks per second.
	 */
	private static final int TICKS_PER_SECOND = 20;
	
	// Unique worker ID
	private static final AtomicInteger nextID = new AtomicInteger();
	
	// Default queue capacity
	private static final int DEFAULT_CAPACITY = 1024;
	
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
	
	// Processing task on the main thread
	private int syncTask = -1;
	
	// Minecraft main thread
	private Thread mainThread;
	
	// Warn plugins that the async listener handler must be started
	private int warningTask;
	
	// Timing manager
	private TimedListenerManager timedManager = TimedListenerManager.getInstance();
	
	/**
	 * Construct a manager for an asynchronous packet handler.
	 * @param mainThread - the main game thread.
	 * @param filterManager - the parent filter manager.
	 * @param listener - the current packet listener.
	 */
	AsyncListenerHandler(Thread mainThread, AsyncFilterManager filterManager, PacketListener listener) {
		if (filterManager == null)
			throw new IllegalArgumentException("filterManager cannot be NULL");
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be NULL");

		this.mainThread = mainThread;
		this.filterManager = filterManager;
		this.listener = listener;
		startWarningTask();
	}
	
	private void startWarningTask() {
		warningTask = filterManager.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
			@Override
			public void run() {
				ProtocolLibrary.getErrorReporter().reportWarning(AsyncListenerHandler.this, Report.
						newBuilder(REPORT_HANDLER_NOT_STARTED).
						messageParam(listener.getPlugin(), AsyncListenerHandler.this).
						build()
				);
			}
		}, 2 * TICKS_PER_SECOND);
	}
	
	private void stopWarningTask() {
		int taskId = warningTask;
		
		// Ensure we have a task to cancel
		if (warningTask >= 0) {
			filterManager.getScheduler().cancelTask(taskId);
			warningTask = -1;
		}
	}
	
	/**
	 * Determine whether or not this asynchronous handler has been cancelled.
	 * @return TRUE if it has been cancelled/stopped, FALSE otherwise.
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Retrieve the current asynchronous packet listener.
	 * @return Current packet listener.
	 */
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
		
		stopWarningTask();
		scheduleAsync(new Runnable() {
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
		
		scheduleAsync(new Runnable() {
			@Override
			public void run() {
				delegateCopy.apply(listenerLoop);
			}
		});
	}
	
	private void scheduleAsync(Runnable runnable) {
		// Handle deprecation
		WrappedScheduler.runAsynchronouslyRepeat(listener.getPlugin(), filterManager.getScheduler(), runnable, 0L, -1L);
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
			return Joiner.on(", ").join(whitelist.getTypes());
	}
	
	/**
	 * Start processing packets on the main thread.
	 * <p>
	 * This is useful if you need to synchronize with the main thread in your packet listener, but
	 * you're not performing any expensive processing.
	 * <p>
	 * <b>Note</b>: Use a asynchronous worker if the packet listener may use more than 0.5 ms 
	 * of processing time on a single packet. Do as much as possible on the worker thread, and schedule synchronous tasks 
	 * to use the Bukkit API instead.
	 * @return TRUE if the synchronized processing was successfully started, FALSE if it's already running.
	 * @throws IllegalStateException If we couldn't start the underlying task.
	 */
	public synchronized boolean syncStart() {
		return syncStart(500, TimeUnit.MICROSECONDS);
	}
	
	/**
	 * Start processing packets on the main thread.
	 * <p>
	 * This is useful if you need to synchronize with the main thread in your packet listener, but
	 * you're not performing any expensive processing.
	 * <p>
	 * The processing time parameter gives the upper bound for the amount of time spent processing pending packets. 
	 * It should be set to a fairly low number, such as 0.5 ms or 1% of a game tick - to reduce the impact 
	 * on the main thread. Never go beyond 50 milliseconds.
	 * <p>
	 * <b>Note</b>: Use a asynchronous worker if the packet listener may exceed the ideal processing time 
	 * on a single packet. Do as much as possible on the worker thread, and schedule synchronous tasks 
	 * to use the Bukkit API instead.
	 * 
	 * @param time - the amount of processing time alloted per game tick (20 ticks per second).
	 * @param unit - the unit of the processingTime argument.
	 * @return TRUE if the synchronized processing was successfully started, FALSE if it's already running.
	 * @throws IllegalStateException If we couldn't start the underlying task.
	 */
	public synchronized boolean syncStart(final long time, final TimeUnit unit) {
		if (time <= 0)
			throw new IllegalArgumentException("Time must be greater than zero.");
		if (unit == null)
			throw new IllegalArgumentException("TimeUnit cannot be NULL.");
	
		final long tickDelay = 1;
		final int workerID = nextID.incrementAndGet();
		
		if (syncTask < 0) {
			stopWarningTask();
			
			syncTask = filterManager.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new Runnable() {
				@Override
				public void run() {
					long stopTime = System.nanoTime() + unit.convert(time, TimeUnit.NANOSECONDS);
					
					while (!cancelled) {
						PacketEvent packet = queuedPackets.poll();
						
						if (packet == INTERUPT_PACKET || packet == WAKEUP_PACKET) {
							// Sorry, asynchronous threads!
							queuedPackets.add(packet);
							
							// Try again next tick
							break;
						} else if (packet != null && packet.getAsyncMarker() != null) {
							processPacket(workerID, packet, "onSyncPacket()");
						} else {
							// No more packets left - wait a tick
							break;
						}
						
						// Check time here, ensuring that we at least process one packet
						if (System.nanoTime() < stopTime)
							break;
					}
				}
			}, tickDelay, tickDelay);
			
			// This is very bad - force the caller to handle it
			if (syncTask < 0)
				throw new IllegalStateException("Cannot start synchronous task.");
			else
				return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Stop processing packets on the main thread.
	 * @return TRUE if we stopped any processing tasks, FALSE if it has already been stopped.
	 */
	public synchronized boolean syncStop() {
		if (syncTask > 0) {
			filterManager.getScheduler().cancelTask(syncTask);
			
			syncTask = -1;
			return true;
		} else {
			return false;
		}
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
	
	/**
	 * The main processing loop of asynchronous threads.
	 * <p>
	 * Note: DO NOT call this method from the main thread
	 * @param workerID - the current worker ID.
	 */
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
			
			while (!cancelled) {
				PacketEvent packet = queuedPackets.take();
				
				// Handle cancel requests
				if (packet == WAKEUP_PACKET) {
					// This is a bit slow, but it should be safe
					synchronized (stopLock) {
						// Are we the one who is supposed to stop?
						if (stoppedTasks.contains(workerID)) 
							return;
						if (waitForStops())
							return;
					}
				} else if (packet == INTERUPT_PACKET) {
					return;
				}
				
				if (packet != null && packet.getAsyncMarker() != null) {
					processPacket(workerID, packet, "onAsyncPacket()");
				}
			}
			
		} catch (InterruptedException e) {
			// We're done
		} finally {
			// Clean up
			started.decrementAndGet();
		}
	}
	
	/**
	 * Called when a packet is scheduled for processing.
	 * @param workerID - the current worker ID.
	 * @param packet - the current packet.
	 * @param methodName - name of the method.
	 */
	private void processPacket(int workerID, PacketEvent packet, String methodName) {
		AsyncMarker marker = packet.getAsyncMarker();
		
		// Here's the core of the asynchronous processing
		try {	
			synchronized (marker.getProcessingLock()) {
				marker.setListenerHandler(this);
				marker.setWorkerID(workerID);
				
				// We're not THAT worried about performance here
				if (timedManager.isTiming()) {
					// Retrieve the tracker to use
					TimedTracker tracker = timedManager.getTracker(listener, 
						packet.isServerPacket() ? ListenerType.ASYNC_SERVER_SIDE : ListenerType.ASYNC_CLIENT_SIDE);
					long token = tracker.beginTracking();
					
					if (packet.isServerPacket())
						listener.onPacketSending(packet);
					else
						listener.onPacketReceiving(packet);
					
					// And we're done
					tracker.endTracking(token, packet.getPacketType());
					
				} else {
					if (packet.isServerPacket())
						listener.onPacketSending(packet);
					else
						listener.onPacketReceiving(packet);
				}
			}
			
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			// Minecraft doesn't want your Exception.
			filterManager.getErrorReporter().reportMinimal(listener.getPlugin(), methodName, e);
		}
		
		// Now, get the next non-cancelled listener
		if (!marker.hasExpired()) {
			for (; marker.getListenerTraversal().hasNext(); ) {
				AsyncListenerHandler handler = marker.getListenerTraversal().next().getListener();
				
				if (!handler.isCancelled()) {
					handler.enqueuePacket(packet);
					return;
				}
			}
		}
		
		// There are no more listeners - queue the packet for transmission
		filterManager.signalFreeProcessingSlot(packet);
		
		// Note that listeners can opt to delay the packet transmission
		filterManager.signalPacketTransmission(packet);
	}
	
	/**
	 * Close all worker threads and the handler itself.
	 */
	private synchronized void close() {
		// Remove the listener itself
		if (!cancelled) {
			filterManager.unregisterAsyncHandlerInternal(this);
			cancelled = true;
			
			// Close processing tasks
			syncStop();
			
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
