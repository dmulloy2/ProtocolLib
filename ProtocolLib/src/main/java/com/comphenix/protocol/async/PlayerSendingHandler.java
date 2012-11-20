package com.comphenix.protocol.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.SortedPacketListenerList;

/**
 * Contains every sending queue for every player.
 * 
 * @author Kristian
 */
class PlayerSendingHandler {
	
	private ErrorReporter reporter;
	private ConcurrentHashMap<String, QueueContainer> playerSendingQueues;
	
	// Timeout listeners
	private SortedPacketListenerList serverTimeoutListeners;
	private SortedPacketListenerList clientTimeoutListeners;
	
	// Whether or not we're currently cleaning up
	private volatile boolean cleaningUp;
	
	/**
	 * Sending queues for a given player.
	 * 
	 * @author Kristian
	 */
	private class QueueContainer {
		private PacketSendingQueue serverQueue;
		private PacketSendingQueue clientQueue;
		
		public QueueContainer() {
			// Server packets are synchronized already
			serverQueue = new PacketSendingQueue(false) {
				@Override
				protected void onPacketTimeout(PacketEvent event) {
					if (!cleaningUp) {
						serverTimeoutListeners.invokePacketSending(reporter, event);
					}
				}
			};
			
			// Client packets must be synchronized
			clientQueue = new PacketSendingQueue(true) {
				@Override
				protected void onPacketTimeout(PacketEvent event) {
					if (!cleaningUp) {
						clientTimeoutListeners.invokePacketSending(reporter, event);
					}
				}
			};
		}

		public PacketSendingQueue getServerQueue() {
			return serverQueue;
		}

		public PacketSendingQueue getClientQueue() {
			return clientQueue;
		}
	}
	
	public PlayerSendingHandler(ErrorReporter reporter, 
			SortedPacketListenerList serverTimeoutListeners, SortedPacketListenerList clientTimeoutListeners) {
		
		this.reporter = reporter;
		this.serverTimeoutListeners = serverTimeoutListeners;
		this.clientTimeoutListeners = clientTimeoutListeners;
		
		// Initialize storage of queues
		playerSendingQueues = new ConcurrentHashMap<String, QueueContainer>();
	}

	/**
	 * Retrieve the sending queue this packet belongs to.
	 * @param packet - the packet.
	 * @return The server or client sending queue the packet belongs to.
	 */
	public PacketSendingQueue getSendingQueue(PacketEvent packet) {
		return getSendingQueue(packet, true);
	}
	
	/**
	 * Retrieve the sending queue this packet belongs to.
	 * @param packet - the packet.
	 * @param createNew - if TRUE, create a new queue if it hasn't already been created.
	 * @return The server or client sending queue the packet belongs to.
	 */
	public PacketSendingQueue getSendingQueue(PacketEvent packet, boolean createNew) {
		String name =  packet.getPlayer().getName();
		QueueContainer queues = playerSendingQueues.get(name);
		
		// Safe concurrent initialization
		if (queues == null && createNew) {
			QueueContainer previous = playerSendingQueues.putIfAbsent(name, new QueueContainer());
			
			if (previous != null)
				queues = previous;
		}
		
		// Check for NULL again
		if (queues != null)
			return packet.isServerPacket() ? queues.getServerQueue() : queues.getClientQueue();
		else
			return null;
	}

	/**
	 * Send all pending packets.
	 */
	public void sendAllPackets() {
		if (!cleaningUp) {
			for (QueueContainer queues : playerSendingQueues.values()) {
				queues.getClientQueue().cleanupAll();
				queues.getServerQueue().cleanupAll();
			}
		}
	}
	
	/**
	 * Immediately send every server packet with the given list of IDs.
	 * @param ids - ID of every packet to send immediately.
	 * @param synchronusOK - whether or not we're running on the main thread. 
	 */
	public void sendServerPackets(List<Integer> ids, boolean synchronusOK) {
		if (!cleaningUp) {
			for (QueueContainer queue : playerSendingQueues.values()) {
				queue.getServerQueue().signalPacketUpdate(ids, synchronusOK);
			}
		}
	}
	
	/**
	 * Immediately send every client packet with the given list of IDs.
	 * @param ids - ID of every packet to send immediately.
	 * @param synchronusOK - whether or not we're running on the main thread. 
	 */
	public void sendClientPackets(List<Integer> ids, boolean synchronusOK) {
		if (!cleaningUp) {
			for (QueueContainer queue : playerSendingQueues.values()) {
				queue.getClientQueue().signalPacketUpdate(ids, synchronusOK);
			}
		}
	}
	
	/**
	 * Send any outstanding server packets.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public void trySendServerPackets(boolean onMainThread) {
		for (QueueContainer queue : playerSendingQueues.values()) {
			queue.getServerQueue().trySendPackets(onMainThread);
		}
	}
	
	/**
	 * Send any outstanding server packets.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public void trySendClientPackets(boolean onMainThread) {
		for (QueueContainer queue : playerSendingQueues.values()) {
			queue.getClientQueue().trySendPackets(onMainThread);
		}
	}
	
	/**
	 * Retrieve every server packet queue for every player.
	 * @return Every sever packet queue.
	 */
	public List<PacketSendingQueue> getServerQueues() {
		List<PacketSendingQueue> result = new ArrayList<PacketSendingQueue>();
		
		for (QueueContainer queue : playerSendingQueues.values())
			result.add(queue.getServerQueue());
		return result;
	}
	
	/**
	 * Retrieve every client packet queue for every player.
	 * @return Every client packet queue.
	 */
	public List<PacketSendingQueue> getClientQueues() {
		List<PacketSendingQueue> result = new ArrayList<PacketSendingQueue>();
		
		for (QueueContainer queue : playerSendingQueues.values())
			result.add(queue.getClientQueue());
		return result;
	}
	
	/**
	 * Send all pending packets and clean up queues.
	 */
	public void cleanupAll() {
		cleaningUp = true;
		
		sendAllPackets();
		playerSendingQueues.clear();
	}

	/**
	 * Invoked when a player has just logged out.
	 * @param player - the player that just logged out.
	 */
	public void removePlayer(Player player) {
		String name = player.getName();
		
		// Every packet will be dropped - there's nothing we can do
		playerSendingQueues.remove(name);
	}
}
