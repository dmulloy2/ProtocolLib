package com.comphenix.protocol.async;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

/**
 * Represents a filter manager for asynchronous packets.
 * 
 * @author Kristian
 */
public class AsyncFilterManager implements AsynchronousManager {

	private PacketProcessingQueue serverProcessingQueue;
	private PacketSendingQueue serverQueue;
	
	private PacketProcessingQueue clientProcessingQueue;
	private PacketSendingQueue clientQueue;
	
	private PacketStream packetStream;
	private Logger logger;
	
	// The likely main thread
	private Thread mainThread;
	
	// Current packet index
	private AtomicInteger currentSendingIndex = new AtomicInteger();
	
	public AsyncFilterManager(Logger logger, PacketStream packetStream) {
		this.serverQueue = new PacketSendingQueue();
		this.clientQueue = new PacketSendingQueue();
		this.serverProcessingQueue = new PacketProcessingQueue(serverQueue);
		this.clientProcessingQueue = new PacketProcessingQueue(clientQueue);
		this.packetStream = packetStream;
		
		this.logger = logger;
		this.mainThread = Thread.currentThread();
	}
	
	@Override
	public AsyncListenerHandler registerAsyncHandler(Plugin plugin, PacketListener listener) {
		AsyncListenerHandler handler = new AsyncListenerHandler(plugin, mainThread, this, listener);
		
		// Add listener to either or both processing queue
		if (hasValidWhitelist(listener.getSendingWhitelist()))
			serverProcessingQueue.addListener(handler, listener.getSendingWhitelist());
		if (hasValidWhitelist(listener.getReceivingWhitelist()))
			clientProcessingQueue.addListener(handler, listener.getReceivingWhitelist());
		
		return handler;
	}
	
	private boolean hasValidWhitelist(ListeningWhitelist whitelist) {
		return whitelist != null && whitelist.getWhitelist().size() > 0;
	}
	
	@Override
	public void unregisterAsyncHandler(AsyncListenerHandler handler) {
		if (handler == null)
			throw new IllegalArgumentException("listenerToken cannot be NULL");
		
		handler.cancel();	
	}
	
	// Called by AsyncListenerHandler
	void unregisterAsyncHandlerInternal(AsyncListenerHandler handler) {
		
		PacketListener listener = handler.getAsyncListener();
		
		// Just remove it from the queue(s)
		if (hasValidWhitelist(listener.getSendingWhitelist()))
			serverProcessingQueue.removeListener(handler, listener.getSendingWhitelist());
		if (hasValidWhitelist(listener.getReceivingWhitelist()))
			clientProcessingQueue.removeListener(handler, listener.getReceivingWhitelist());
	}
	
	/**
	 * Enqueue a packet for asynchronous processing.
	 * @param syncPacket - synchronous packet event.
	 * @param asyncMarker - the asynchronous marker to use.
	 */
	public void enqueueSyncPacket(PacketEvent syncPacket, AsyncMarker asyncMarker) {
		PacketEvent newEvent = PacketEvent.fromSynchronous(syncPacket, asyncMarker);
		
		// Start the process
		getSendingQueue(syncPacket).enqueue(newEvent);
		getProcessingQueue(syncPacket).enqueue(newEvent);
	}
	
	@Override
	public Set<Integer> getSendingFilters() {
		return serverProcessingQueue.keySet();
	}
	
	@Override
	public Set<Integer> getReceivingFilters() {
		return clientProcessingQueue.keySet();
	}
	
	@Override
	public boolean hasAsynchronousListeners(PacketEvent packet) {
		return getProcessingQueue(packet).getListener(packet.getPacketID()).size() > 0;
	}
	
	/**
	 * Construct a asynchronous marker with all the default values.
	 * @return Asynchronous marker.
	 */
	public AsyncMarker createAsyncMarker() {
		return createAsyncMarker(AsyncMarker.DEFAULT_SENDING_DELTA, AsyncMarker.DEFAULT_TIMEOUT_DELTA);
	}
	
	/**
	 * Construct an async marker with the given sending priority delta and timeout delta.
	 * @param sendingDelta - how many packets we're willing to wait.
	 * @param timeoutDelta - how long (in ms) until the packet expire.
	 * @return An async marker.
	 */
	public AsyncMarker createAsyncMarker(long sendingDelta, long timeoutDelta) {
		return createAsyncMarker(sendingDelta, timeoutDelta, 
								 currentSendingIndex.incrementAndGet(), System.currentTimeMillis());
	}
	
	// Helper method
	private AsyncMarker createAsyncMarker(long sendingDelta, long timeoutDelta, long sendingIndex, long currentTime) {
		return new AsyncMarker(packetStream, sendingIndex, sendingDelta, System.currentTimeMillis(), timeoutDelta);
	}
	
	@Override
	public PacketStream getPacketStream() {
		return packetStream;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public void cleanupAll() {
		serverProcessingQueue.cleanupAll();
		serverQueue.cleanupAll();
	}

	/**
	 * Signal that a packet is ready to be transmitted.
	 * @param packet - packet to signal.
	 */
	public void signalPacketUpdate(PacketEvent packet) {
		getSendingQueue(packet).signalPacketUpdate(packet);
	}

	/**
	 * Retrieve the sending queue this packet belongs to.
	 * @param packet - the packet.
	 * @return The server or client sending queue the packet belongs to.
	 */
	private PacketSendingQueue getSendingQueue(PacketEvent packet) {
		return packet.isServerPacket() ? serverQueue : clientQueue;
	}
	
	/**
	 * Signal that a packet has finished processing.
	 * @param packet - packet to signal.
	 */
	public void signalProcessingDone(PacketEvent packet) {
		getProcessingQueue(packet).signalProcessingDone();
	}
	
	/**
	 * Retrieve the processing queue this packet belongs to.
	 * @param packet - the packet.
	 * @return The server or client sending processing the packet belongs to.
	 */
	private PacketProcessingQueue getProcessingQueue(PacketEvent packet) {
		return packet.isServerPacket() ? serverProcessingQueue : clientProcessingQueue;
	}

	/**
	 * Send any due packets, or clean up packets that have expired.
	 */
	public void sendProcessedPackets() {
		clientQueue.trySendPackets();
		serverQueue.trySendPackets();
	}
}
