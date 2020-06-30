/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
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
package com.comphenix.protocol.injector.netty;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Stores packets that need to be sent without being handled by the listeners (filtered=false).
 * When other packets sent after sending the packet are removed, the packet is removed as well
 * to prevent a memory leak, assuming a consistent send order is in place.
 * 
 * @author bergerkiller
 */
public class PacketFilterQueue {
	private Queue<Object> queue = new ArrayDeque<>();

	/**
	 * Adds a packet to this queue, indicating further on that it should not be filtered.
	 * 
	 * @param packet
	 */
	public synchronized void add(Object packet) {
		queue.add(packet);
	}

	/**
	 * Checks whether a packet is contained inside this queue, indicating
	 * it should not be filtered.
	 * 
	 * @param packet
	 * @return True if contained and packet should not be filtered (filtered=false)
	 */
	public synchronized boolean contains(Object packet) {
		return queue.contains(packet);
	}

	/**
	 * Checks whether a packet is contained inside this queue and removes it if so.
	 * Other packets marked in this queue that were sent before this packet are
	 * removed from the queue also, avoiding memory leaks because of dropped packets.
	 * 
	 * @param packet
	 * @return True if contained and packet should not be filtered (filtered=false)
	 */
	public synchronized boolean remove(Object packet) {
		if (queue.isEmpty()) {
			// Nothing in the queue
			return false;
		} else if (queue.peek() == packet) {
			// First in the queue (expected)
			queue.poll();
			return true;
		} else if (!queue.contains(packet)) {
			// There are unfiltered packets, but this one is not
			return false;
		} else {
			// We have skipped over some packets (unexpected)
			// Poll packets until we find it
			while (queue.poll() != packet) {
				if (queue.isEmpty()) {
					// This should never happen! But to avoid infinite loop.
					return false;
				}
			}
			return true;
		}
	}
}
