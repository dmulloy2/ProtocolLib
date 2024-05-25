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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.ConcurrentPlayerMap;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.collection.InboundPacketListenerSet;
import com.comphenix.protocol.injector.collection.OutboundPacketListenerSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Contains every sending queue for every player.
 * 
 * @author Kristian
 */
class PlayerSendingHandler {
    private final ConcurrentMap<Player, QueueContainer> playerSendingQueues;
    
    // Timeout listeners
    private final OutboundPacketListenerSet outboundTimeoutListeners;
    private final InboundPacketListenerSet inboundTimeoutListeners;
    
    // Asynchronous packet sending
    private Executor asynchronousSender;
    
    // Whether or not we're currently cleaning up
    private volatile boolean cleaningUp;
    
    /**
     * Sending queues for a given player.
     * 
     * @author Kristian
     */
    private class QueueContainer {
        private final PacketSendingQueue outboundQueue;
        private final PacketSendingQueue inboundQueue;
        
        public QueueContainer() {
            // Server packets can be sent concurrently
            outboundQueue = new PacketSendingQueue(false, asynchronousSender) {
                @Override
                protected void onPacketTimeout(PacketEvent event) {
                    if (!cleaningUp) {
                        outboundTimeoutListeners.invoke(event);
                    }
                }
            };
            
            // Client packets must be synchronized
            inboundQueue = new PacketSendingQueue(true, asynchronousSender) {
                @Override
                protected void onPacketTimeout(PacketEvent event) {
                    if (!cleaningUp) {
                        inboundTimeoutListeners.invoke(event);
                    }
                }
            };
        }

        public PacketSendingQueue getOutboundQueue() {
            return outboundQueue;
        }

        public PacketSendingQueue getInboundQueue() {
            return inboundQueue;
        }
    }
    
    /**
     * Initialize a packet sending handler.
     * @param reporter - error reporter.
     * @param serverTimeoutListeners - set of server timeout listeners.
     * @param clientTimeoutListeners - set of client timeout listeners.
     */
    public PlayerSendingHandler(OutboundPacketListenerSet serverTimeoutListeners, InboundPacketListenerSet clientTimeoutListeners) {
        this.outboundTimeoutListeners = serverTimeoutListeners;
        this.inboundTimeoutListeners = clientTimeoutListeners;
        
        // Initialize storage of queues
        this.playerSendingQueues = ConcurrentPlayerMap.usingAddress();
    }
    
    /**
     * Start the asynchronous packet sender.
     */
    public synchronized void initializeScheduler() {
        if (asynchronousSender == null) {
            ThreadFactory factory = new ThreadFactoryBuilder().
                setDaemon(true).
                setNameFormat("ProtocolLib-AsyncSender %s").
                build();
            asynchronousSender = Executors.newSingleThreadExecutor(factory);
        }
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
        QueueContainer queues = playerSendingQueues.get(packet.getPlayer());
        
        // Safe concurrent initialization
        if (queues == null && createNew) {
            final QueueContainer newContainer = new QueueContainer();

            // Attempt to map the queue
            queues = playerSendingQueues.putIfAbsent(packet.getPlayer(), newContainer);
            
            if (queues == null) {
                queues = newContainer;
            }
        }
        
        // Check for NULL again
        if (queues != null)
            return packet.isServerPacket() ? queues.getOutboundQueue() : queues.getInboundQueue();
        else
            return null;
    }

    /**
     * Send all pending packets.
     */
    public void sendAllPackets() {
        if (!cleaningUp) {
            for (QueueContainer queues : playerSendingQueues.values()) {
                queues.getInboundQueue().cleanupAll();
                queues.getOutboundQueue().cleanupAll();
            }
        }
    }
    
    /**
     * Immediately send every server packet with the given list of IDs.
     * @param types - types of every packet to send immediately.
     * @param synchronusOK - whether we're running on the main thread.
     */
    public void sendServerPackets(List<PacketType> types, boolean synchronusOK) {
        if (!cleaningUp) {
            for (QueueContainer queue : playerSendingQueues.values()) {
                queue.getOutboundQueue().signalPacketUpdate(types, synchronusOK);
            }
        }
    }
    
    /**
     * Immediately send every client packet with the given list of IDs.
     * @param synchronusOK - whether we're running on the main thread.
     */
    public void sendClientPackets(List<PacketType> types, boolean synchronusOK) {
        if (!cleaningUp) {
            for (QueueContainer queue : playerSendingQueues.values()) {
                queue.getInboundQueue().signalPacketUpdate(types, synchronusOK);
            }
        }
    }
    
    /**
     * Send any outstanding server packets.
     * @param onMainThread - whether this is occurring on the main thread.
     */
    public void trySendServerPackets(boolean onMainThread) {
        for (QueueContainer queue : playerSendingQueues.values()) {
            queue.getOutboundQueue().trySendPackets(onMainThread);
        }
    }
    
    /**
     * Send any outstanding server packets.
     * @param onMainThread - whether this is occurring on the main thread.
     */
    public void trySendClientPackets(boolean onMainThread) {
        for (QueueContainer queue : playerSendingQueues.values()) {
            queue.getInboundQueue().trySendPackets(onMainThread);
        }
    }
    
    /**
     * Retrieve every server packet queue for every player.
     * @return Every sever packet queue.
     */
    public List<PacketSendingQueue> getServerQueues() {
        List<PacketSendingQueue> result = new ArrayList<>();
        
        for (QueueContainer queue : playerSendingQueues.values())
            result.add(queue.getOutboundQueue());
        return result;
    }
    
    /**
     * Retrieve every client packet queue for every player.
     * @return Every client packet queue.
     */
    public List<PacketSendingQueue> getClientQueues() {
        List<PacketSendingQueue> result = new ArrayList<>();
        
        for (QueueContainer queue : playerSendingQueues.values())
            result.add(queue.getInboundQueue());
        return result;
    }
    
    /**
     * Send all pending packets and clean up queues.
     */
    public void cleanupAll() {
        if (!cleaningUp) {
            cleaningUp = true;
            
            sendAllPackets();
            playerSendingQueues.clear();
        }
    }

    /**
     * Invoked when a player has just logged out.
     * @param player - the player that just logged out.
     */
    public void removePlayer(Player player) {
        // Every packet will be dropped - there's nothing we can do
        playerSendingQueues.remove(player);
    }
}
