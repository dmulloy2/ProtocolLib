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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PlayerLoggedOutException;
import com.comphenix.protocol.reflect.FieldAccessException;

/**
 * Represents packets ready to be transmitted to a client.
 * @author Kristian
 */
abstract class PacketSendingQueue {
	public static final ReportType REPORT_DROPPED_PACKET = new ReportType("Warning: Dropped packet index %s of type %s.");
	
	public static final int INITIAL_CAPACITY = 10;
	
	private PriorityBlockingQueue<PacketEventHolder> sendingQueue;
	
	// Asynchronous packet sending
	private Executor asynchronousSender;
	// Whether or not packet transmission must occur on a specific thread
	private final boolean notThreadSafe;
	// Whether or not we've run the cleanup procedure
	private boolean cleanedUp = false;
	
	/**
	 * Create a packet sending queue.
	 * @param notThreadSafe - whether or not to synchronize with the main thread or a background thread.
	 */
	public PacketSendingQueue(boolean notThreadSafe, Executor asynchronousSender) {
		this.sendingQueue = new PriorityBlockingQueue<PacketEventHolder>(INITIAL_CAPACITY);
		this.notThreadSafe = notThreadSafe;
		this.asynchronousSender = asynchronousSender;
	}
	
	/**
	 * Number of packet events in the queue.
	 * @return The number of packet events in the queue.
	 */
	public int size() {
		return sendingQueue.size();
	}
	
	/**
	 * Enqueue a packet for sending. 
	 * @param packet - packet to queue.
	 */
	public void enqueue(PacketEvent packet) {
		sendingQueue.add(new PacketEventHolder(packet));
	}
	
	/**
	 * Invoked when one of the packets have finished processing.
	 * @param packetUpdated - the packet that has now been updated.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public synchronized void signalPacketUpdate(PacketEvent packetUpdated, boolean onMainThread) {
		
		AsyncMarker marker = packetUpdated.getAsyncMarker();
		
		// Should we reorder the event?
		if (marker.getQueuedSendingIndex() != marker.getNewSendingIndex() && !marker.hasExpired()) {
			PacketEvent copy = PacketEvent.fromSynchronous(packetUpdated, marker);
			
			// "Cancel" the original event
			packetUpdated.setReadOnly(false);
			packetUpdated.setCancelled(true);
			
			// Enqueue the copy with the new sending index
			enqueue(copy);
		}
		
		// Mark this packet as finished
		marker.setProcessed(true);
		trySendPackets(onMainThread);
	}

	/***
	 * Invoked when a list of packet IDs are no longer associated with any listeners.
	 * @param packetsRemoved - packets that no longer have any listeners.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public synchronized void signalPacketUpdate(List<PacketType> packetsRemoved, boolean onMainThread) {
		Set<PacketType> lookup = new HashSet<PacketType>(packetsRemoved);
		
		// Note that this is O(n), so it might be expensive
		for (PacketEventHolder holder : sendingQueue) {
			PacketEvent event = holder.getEvent();
			
			if (lookup.contains(event.getPacketType())) {
				event.getAsyncMarker().setProcessed(true);
			}
		}
		
		// This is likely to have changed the situation a bit
		trySendPackets(onMainThread);
	}
	
	/**
	 * Attempt to send any remaining packets.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public void trySendPackets(boolean onMainThread) {
		// Whether or not to continue sending packets
		boolean sending = true;
		
		// Transmit as many packets as we can
		while (sending) {
			PacketEventHolder holder = sendingQueue.poll();
			
			if (holder != null) {
				sending = processPacketHolder(onMainThread, holder);
					
				if (!sending) {
					// Add it back again
					sendingQueue.add(holder);
				}
				
			} else {
				// No more packets to send
				sending = false;
			}
		}
	}
	
	/**
	 * Invoked when a packet might be ready for transmission.
	 * @param onMainThread - TRUE if we're on the main thread, FALSE otherwise.
	 * @param holder - packet container.
	 * @return TRUE to continue sending packets, FALSE otherwise.
	 */
	private boolean processPacketHolder(boolean onMainThread, final PacketEventHolder holder) {
		PacketEvent current = holder.getEvent();
		AsyncMarker marker = current.getAsyncMarker();
		boolean hasExpired = marker.hasExpired();
		
		// Guard in cause the queue is closed
		if (cleanedUp) {
			return true;
		}
				
		// End condition?
		if (marker.isProcessed() || hasExpired) {
			if (hasExpired) {
				// Notify timeout listeners
				onPacketTimeout(current);
				
				// Recompute
				marker = current.getAsyncMarker();
				hasExpired = marker.hasExpired();
				
				// Could happen due to the timeout listeners
				if (!marker.isProcessed() && !hasExpired) {
					return false;
				}
			}
			
			// Is it okay to send the packet?
			if (!current.isCancelled() && !hasExpired) {
				// Make sure we're on the main thread
				if (notThreadSafe) {
					try {
						boolean wantAsync = marker.isMinecraftAsync(current);
						boolean wantSync = !wantAsync;
						
						// Wait for the next main thread heartbeat if we haven't fulfilled our promise
						if (!onMainThread && wantSync) {
							return false;
						}
						
						// Let's give it what it wants
						if (onMainThread && wantAsync) {
							asynchronousSender.execute(new Runnable() {
								@Override
								public void run() {
									// We know this isn't on the main thread
									processPacketHolder(false, holder);
								}
							});
							
							// Scheduler will do the rest
							return true;
						}
						
					} catch (FieldAccessException e) {
						e.printStackTrace();
						
						// Just drop the packet
						return true;
					}
				} 
				
				// Silently skip players that have logged out
				if (isOnline(current.getPlayer())) {
					sendPacket(current);
				}
			} 
			
			// Drop the packet
			return true;
		}
		
		// Add it back and stop sending
		return false;
	}
	
	/**
	 * Invoked when a packet has timed out.
	 * @param event - the timed out packet.
	 */
	protected abstract void onPacketTimeout(PacketEvent event);
	
	private boolean isOnline(Player player) {
		return player != null && player.isOnline();
	}
	
	/**
	 * Send every packet, regardless of the processing state.
	 */
	private void forceSend() {
		while (true) {
			PacketEventHolder holder = sendingQueue.poll();
			
			if (holder != null) {
				sendPacket(holder.getEvent());
			} else {
				break;
			}
		}
	}
	
	/**
	 * Whether or not the packet transmission must synchronize with the main thread.
	 * @return TRUE if it must, FALSE otherwise.
	 */
	public boolean isSynchronizeMain() {
		return notThreadSafe;
	}

	/**
	 * Transmit a packet, if it hasn't already.
	 * @param event - the packet to transmit.
	 */
	private void sendPacket(PacketEvent event) {
		
		AsyncMarker marker = event.getAsyncMarker();
		
		try {
			// Don't send a packet twice
			if (marker != null && !marker.isTransmitted()) {
				marker.sendPacket(event);
			}
		
		} catch (PlayerLoggedOutException e) {
			ProtocolLibrary.getErrorReporter().reportDebug(this, Report.newBuilder(REPORT_DROPPED_PACKET).
				messageParam(marker.getOriginalSendingIndex(), event.getPacketType()).
				callerParam(event)
			);
			
		} catch (IOException e) {
			// Just print the error
			e.printStackTrace();
		}
	}

	/**
	 * Automatically transmits every delayed packet.
	 */
	public void cleanupAll() {
		if (!cleanedUp) {
			// Note that the cleanup itself will always occur on the main thread
			forceSend();
			
			// And we're done
			cleanedUp = true;
		}
	}
}
