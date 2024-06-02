package com.comphenix.protocol.timing;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.ImmutableMap;

public class TimingTrackerManager {

	private final static AtomicBoolean IS_TRACKING = new AtomicBoolean();

	private static volatile Date startTime;
	private static volatile Date stopTime;

	private static final Map<String, ImmutableMap<TimingListenerType, PluginTimingTracker>> TRACKER_MAP = new ConcurrentHashMap<>();

	public static boolean startTracking() {
		if (IS_TRACKING.compareAndSet(false, true)) {
			startTime = Calendar.getInstance().getTime();
			return true;
		}
		return false;
	}

	public static boolean isTracking() {
		return IS_TRACKING.get();
	}

	public static boolean stopTracking() {
		if (IS_TRACKING.compareAndSet(true, false)) {
			stopTime = Calendar.getInstance().getTime();
			return true;
		}
		return false;
	}

	public static TimingReport createReportAndReset() {
		TimingReport report = new TimingReport(startTime, stopTime, ImmutableMap.copyOf(TRACKER_MAP));
		TRACKER_MAP.clear();
		return report;
	}

	public static TimingTracker get(PacketListener listener, TimingListenerType type) {
		if (!IS_TRACKING.get()) {
			return TimingTracker.EMPTY;
		}

		String plugin = listener.getPlugin().getName();
		return TRACKER_MAP.computeIfAbsent(plugin, k -> newTrackerMap()).get(type);
	}

	private static ImmutableMap<TimingListenerType, PluginTimingTracker> newTrackerMap() {
		ImmutableMap.Builder<TimingListenerType, PluginTimingTracker> builder = ImmutableMap.builder();

		for (TimingListenerType type : TimingListenerType.values()) {
			builder.put(type, new PluginTimingTracker());
		}

		return builder.build();
	}
}
