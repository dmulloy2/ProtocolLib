package com.comphenix.protocol.timing;

import com.comphenix.protocol.Packets;

/**
 * Tracks the invocation time for a particular plugin against a list of packets.
 * @author Kristian
 */
public class TimedTracker {	
	// Table of packets and invocations
	private StatisticsStream[] packets;
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
	public synchronized void endTracking(long trackingToken, int packetId) {
		// Lazy initialization
		if (packets == null)
			packets = new StatisticsStream[Packets.PACKET_COUNT];
		if (packets[packetId] == null)
			packets[packetId] = new StatisticsStream();
		
		// Store this observation
		packets[packetId].observe(System.nanoTime() - trackingToken);
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
	 * Retrieve an array (indexed by packet ID) of all relevant statistics.
	 * @return The array of statistics.
	 */
	public synchronized StatisticsStream[] getStatistics() {
		StatisticsStream[] clone = new StatisticsStream[Packets.PACKET_COUNT];
		
		if (packets != null) {
			for (int i = 0; i < clone.length; i++) {
				if (packets[i] != null) {
					clone[i] = new StatisticsStream(packets[i]);
				}
			}
		}
		return clone;
	}
}
