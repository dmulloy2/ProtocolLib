package com.comphenix.protocol.timing;

import com.comphenix.protocol.PacketType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks the invocation time for a particular plugin against a list of packets.
 *
 * @author Kristian
 */
public class TimedTracker {

	// Table of packets and invocations
	private final AtomicInteger observations = new AtomicInteger();
	private final Map<PacketType, StatisticsStream> packets = new HashMap<>();

	/**
	 * Begin tracking an execution time.
	 *
	 * @return The current tracking token.
	 */
	public long beginTracking() {
		return System.nanoTime();
	}

	/**
	 * Stop and record the execution time since the creation of the given tracking token.
	 *
	 * @param trackingToken - the tracking token.
	 * @param type          - the packet type.
	 */
	public synchronized void endTracking(long trackingToken, PacketType type) {
		StatisticsStream stream = this.packets.get(type);

		// Lazily create a stream
		if (stream == null) {
			this.packets.put(type, stream = new StatisticsStream());
		}
		// Store this observation
		stream.observe(System.nanoTime() - trackingToken);
		this.observations.incrementAndGet();
	}

	/**
	 * Retrieve the total number of observations.
	 *
	 * @return Total number of observations.
	 */
	public int getObservations() {
		return this.observations.get();
	}

	/**
	 * Retrieve an map (indexed by packet type) of all relevant statistics.
	 *
	 * @return The map of statistics.
	 */
	public synchronized Map<PacketType, StatisticsStream> getStatistics() {
		final Map<PacketType, StatisticsStream> clone = new HashMap<>();

		for (Entry<PacketType, StatisticsStream> entry : this.packets.entrySet()) {
			clone.put(
					entry.getKey(),
					new StatisticsStream(entry.getValue())
			);
		}
		return clone;
	}
}
