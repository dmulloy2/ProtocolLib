package com.comphenix.protocol.timing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.comphenix.protocol.PacketType;

public class PluginTimingTracker implements TimingTracker {

	private final Map<PacketType, StatisticsStream> statistics = new ConcurrentHashMap<>();
	private volatile boolean hasReceivedData = false;

	@Override
	public void track(PacketType packetType, Runnable runnable) {
		long startTime = System.nanoTime();
		runnable.run();
		long endTime = System.nanoTime();

		this.statistics.computeIfAbsent(packetType, key -> new StatisticsStream())
				.observe(endTime - startTime);

		this.hasReceivedData = true;
	}

	public boolean hasReceivedData() {
		return hasReceivedData;
	}

	public Map<PacketType, StatisticsStream> getStatistics() {
		return statistics;
	}
}
