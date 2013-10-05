package com.comphenix.protocol.timing;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Represents a system for recording the time spent by each packet listener.
 * @author Kristian
 */
public class TimedListenerManager {
	public enum ListenerType {
		ASYNC_SERVER_SIDE,
		ASYNC_CLIENT_SIDE,
		SYNC_SERVER_SIDE,
		SYNC_CLIENT_SIDE;
	}
	
	// The shared manager
	private final static TimedListenerManager INSTANCE = new TimedListenerManager();
	// Running?
	private final static AtomicBoolean timing = new AtomicBoolean();
	// When it was started
	private volatile Date started;
	private volatile Date stopped;
	
	// The map of time trackers
	private ConcurrentMap<String, ImmutableMap<ListenerType, TimedTracker>> map = Maps.newConcurrentMap();

	/**
	 * Retrieve the shared listener manager.
	 * <p>
	 * This should never change.
	 * @return The shared listener manager.
	 */
	public static TimedListenerManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Start timing listeners.
	 * @return TRUE if we started timing, FALSE if we are already timing listeners.
	 */
	public boolean startTiming() {
		if (setTiming(true)) {
			started = Calendar.getInstance().getTime();
			return true;
		}
		return false;
	}
	
	/**s
	 * Stop timing listeners.
	 * @return TRUE if we stopped timing, FALSE otherwise.
	 */
	public boolean stopTiming() {
		if (setTiming(false)) {
			stopped = Calendar.getInstance().getTime();
			return true;
		}
		return false;
	}
	
	/**
	 * Retrieve the time the listener was started.
	 * @return The time it was started, or NULL if they have never been started.
	 */
	public Date getStarted() {
		return started;
	}
	
	/**
	 * Retrieve the time the time listeners was stopped.
	 * @return The time they were stopped, or NULL if not found.
	 */
	public Date getStopped() {
		return stopped;
	}
	
	/**
	 * Set whether or not the timing manager is enabled.
	 * @param value - TRUE if it should be enabled, FALSE otherwise.
	 * @return TRUE if the value was changed, FALSE otherwise.
	 */
	private boolean setTiming(boolean value) {
		return timing.compareAndSet(!value, value);
	}
	
	/**
	 * Determine if we are currently timing listeners.
	 * @return TRUE if we are, FALSE otherwise.
	 */
	public boolean isTiming() {
		return timing.get();
	}
	
	/**
	 * Reset all packet gathering data.
	 */
	public void clear() {
		map.clear();
	}
	
	/**
	 * Retrieve every tracked plugin.
	 * @return Every tracked plugin.
	 */
	public Set<String> getTrackedPlugins() {
		return map.keySet();
	}
	
	/**
	 * Retrieve the timed tracker associated with the given plugin and listener type.
	 * @param plugin - the plugin.
	 * @param type - the listener type.
	 * @return The timed tracker.
	 */
	public TimedTracker getTracker(Plugin plugin, ListenerType type) {
		return getTracker(plugin.getName(), type);
	}
	
	/**
	 * Retrieve the timed tracker associated with the given listener and listener type.
	 * @param listener - the listener.
	 * @param type - the listener type.
	 * @return The timed tracker.
	 */
	public TimedTracker getTracker(PacketListener listener, ListenerType type) {
		return getTracker(listener.getPlugin().getName(), type);
	}
	
	/**
	 * Retrieve the timed tracker associated with the given plugin and listener type.
	 * @param pluginName - the plugin name.
	 * @param type - the listener type.
	 * @return The timed tracker.
	 */
	public TimedTracker getTracker(String pluginName, ListenerType type) {
		return getTrackers(pluginName).get(type);
	}
	
	/**
	 * Retrieve the map of timed trackers for a specific plugin.
	 * @param pluginName - the plugin name.
	 * @return Map of timed trackers.
	 */
	private ImmutableMap<ListenerType, TimedTracker> getTrackers(String pluginName) {
		ImmutableMap<ListenerType, TimedTracker> trackers = map.get(pluginName);
		
		// Atomic pattern
		if (trackers == null) {
			ImmutableMap<ListenerType, TimedTracker> created = newTrackerMap();
			trackers = map.putIfAbsent(pluginName, created);
			
			// Success!
			if (trackers == null) {
				trackers = created;
			}
		}
		return trackers;
	}
	
	/**
	 * Retrieve a new map of trackers for an unspecified plugin.
	 * @return A map of listeners and timed trackers.
	 */
	private ImmutableMap<ListenerType, TimedTracker> newTrackerMap() {
		ImmutableMap.Builder<ListenerType, TimedTracker> builder = ImmutableMap.builder();
		
		// Construct a map with every listener type
		for (ListenerType type : ListenerType.values()) {
			builder.put(type, new TimedTracker());
		}
		return builder.build();
	}
}
