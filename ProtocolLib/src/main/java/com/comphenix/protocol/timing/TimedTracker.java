package com.comphenix.protocol.timing;

import java.util.Map;
import java.util.Map.Entry;

import com.comphenix.protocol.PacketType;
import com.google.common.collect.Maps;

/**
 * Tracks the invocation time for a particular plugin against a list of packets.
 * @author Kristian
 */
public class TimedTracker {	
	// Table of packets and invocations
	private Map<PacketType, StatisticsStream> packets = Maps.newHashMap();
	private int observations;
	
	/**
	 * Begin tracking an execution time.
	 * @return The current tracking token.
	 */
	public long beginTracking() {
		return System.nanoTime();
	}

	/**
	 * Stop and record the execution time since the creation of the given tracking token.
	 * @param trackingToken - the tracking token.
	 * @param packetId - the packet ID.
	 */
	public synchronized void endTracking(long trackingToken, PacketType type) {
		StatisticsStream stream = packets.get(type);
		
		// Lazily create a stream
		if (stream == null) {
			packets.put(type, stream = new StatisticsStream());
		}
		// Store this observation
		stream.observe(System.nanoTime() - trackingToken);
		observations++;
	}
	
	/**
	 * Retrieve the total number of observations.
	 * @return Total number of observations.
	 */
	public int getObservations() {
		return observations;
	}
	
	/**
	 * Retrieve an map (indexed by packet type) of all relevant statistics.
	 * @return The map of statistics.
	 */
	public synchronized Map<PacketType, StatisticsStream> getStatistics() {
		Map<PacketType, StatisticsStream> clone = Maps.newHashMap();
		
		for (Entry<PacketType, StatisticsStream> entry : packets.entrySet()) {
			clone.put(
				entry.getKey(), 
				new StatisticsStream(entry.getValue())
			);
		}
		return clone;
	}
}
