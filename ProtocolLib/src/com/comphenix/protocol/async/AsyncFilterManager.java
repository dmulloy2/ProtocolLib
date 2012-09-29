package com.comphenix.protocol.async;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

/**
 * Represents a filter manager for asynchronous packets.
 * 
 * @author Kristian
 */
public class AsyncFilterManager {

	private PacketProcessingQueue processingQueue;
	private PacketSendingQueue sendingQueue;
	
	private PacketStream packetStream;
	private Logger logger;
	
	// The likely main thread
	private Thread mainThread;
	
	// Current packet index
	private AtomicInteger currentSendingIndex = new AtomicInteger();
	
	public AsyncFilterManager(Logger logger, PacketStream packetStream) {
		this.sendingQueue = new PacketSendingQueue();
		this.processingQueue = new PacketProcessingQueue(sendingQueue);
		this.packetStream = packetStream;
		
		this.logger = logger;
		this.mainThread = Thread.currentThread();
	}
	
	public ListenerToken registerAsyncHandler(Plugin plugin, PacketListener listener) {
		ListenerToken token = new ListenerToken(plugin, mainThread, this, listener);
		
		processingQueue.addListener(token, listener.getSendingWhitelist());
		return token;
	}
	
	public void unregisterAsyncHandler(ListenerToken listenerToken) {
		if (listenerToken == null)
			throw new IllegalArgumentException("listenerToken cannot be NULL");
		
		listenerToken.cancel();	
	}
	
	// Called by ListenerToken
	void unregisterAsyncHandlerInternal(ListenerToken listenerToken) {
		// Just remove it from the queue
		processingQueue.removeListener(listenerToken, listenerToken.getAsyncListener().getSendingWhitelist());
	}
	
	/**
	 * Enqueue a packet for asynchronous processing.
	 * @param syncPacket - synchronous packet event.
	 * @param asyncMarker - the asynchronous marker to use.
	 */
	public void enqueueSyncPacket(PacketEvent syncPacket, AsyncPacket asyncMarker) {
		PacketEvent newEvent = PacketEvent.fromSynchronous(syncPacket, asyncMarker);
		
		// Start the process
		sendingQueue.enqueue(newEvent);
		processingQueue.enqueuePacket(newEvent);
	}
	
	/**
	 * Construct an async marker with the given sending priority delta and timeout delta.
	 * @param sendingDelta - how many packets we're willing to wait.
	 * @param timeoutDelta - how long (in ms) until the packet expire.
	 * @return An async marker.
	 */
	public AsyncPacket createAsyncMarker(long sendingDelta, long timeoutDelta) {
		return createAsyncMarker(sendingDelta, timeoutDelta, 
								 currentSendingIndex.incrementAndGet(), System.currentTimeMillis());
	}
	
	// Helper method
	private AsyncPacket createAsyncMarker(long sendingDelta, long timeoutDelta, long sendingIndex, long currentTime) {
		return new AsyncPacket(packetStream, sendingIndex, sendingDelta, System.currentTimeMillis(), timeoutDelta);
	}
	
	public PacketStream getPacketStream() {
		return packetStream;
	}

	public Logger getLogger() {
		return logger;
	}

	PacketProcessingQueue getProcessingQueue() {
		return processingQueue;
	}

	PacketSendingQueue getSendingQueue() {
		return sendingQueue;
	}

	/**
	 * Remove listeners, close threads and transmit every delayed packet.
	 */
	public void cleanupAll() {
		processingQueue.cleanupAll();
		sendingQueue.cleanupAll();
	}
}
