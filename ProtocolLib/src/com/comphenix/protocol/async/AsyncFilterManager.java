package com.comphenix.protocol.async;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.events.PacketEvent;

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
	
	public ListenerToken registerAsyncHandler(Plugin plugin, AsyncListener listener) {
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
	
	public void enqueueSyncPacket(PacketEvent syncPacket, int sendingDelta, long timeoutDelta) {
		AsyncPacket asyncPacket = new AsyncPacket(packetStream, syncPacket, 
				currentSendingIndex.getAndIncrement() + sendingDelta,
				System.currentTimeMillis(),
				timeoutDelta);
		
		// Start the process
		sendingQueue.enqueue(asyncPacket);
		processingQueue.enqueuePacket(asyncPacket);
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

	public void cleanupAll() {
		// Remove all listeners
		
		// We don't necessarily remove packets, as this might be a part of a server reload
	}
}
