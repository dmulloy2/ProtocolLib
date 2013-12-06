package com.comphenix.protocol.timing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.timing.TimedListenerManager.ListenerType;
import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class TimingReportGenerator {
	private static final String NEWLINE = System.getProperty("line.separator");
	private static final String META_STARTED = "Started: %s" + NEWLINE;
	private static final String META_STOPPED = "Stopped: %s (after %s seconds)" + NEWLINE;
	private static final String PLUGIN_HEADER = "=== PLUGIN %s ===" + NEWLINE;
	private static final String LISTENER_HEADER = " TYPE: %s " + NEWLINE;
	private static final String SEPERATION_LINE = " ------------------------------- " + NEWLINE;
	private static final String STATISTICS_HEADER = " Packet:      Count:       Min (ms):       Max (ms):       Mean (ms):      Std (ms): " + NEWLINE;
	private static final String STATISTICS_ROW =    " %-12s %-12d %-15.6f %-15.6f %-15.6f %.6f " + NEWLINE;
	private static final String SUM_MAIN_THREAD = " => Time on main thread: %.6f ms" + NEWLINE;
	
	public void saveTo(File destination, TimedListenerManager manager) throws IOException {
		BufferedWriter writer = null;
		Date started = manager.getStarted();
		Date stopped = manager.getStopped();
		long seconds = Math.abs((stopped.getTime() - started.getTime()) / 1000);
		
		try {
			writer = Files.newWriter(destination, Charsets.UTF_8);

			// Write some timing information
			writer.write(String.format(META_STARTED, started));
			writer.write(String.format(META_STOPPED, stopped, seconds));
			writer.write(NEWLINE);
			
			for (String plugin : manager.getTrackedPlugins()) {
				writer.write(String.format(PLUGIN_HEADER, plugin));
				
				for (ListenerType type : ListenerType.values()) {
					TimedTracker tracker = manager.getTracker(plugin, type);
					
					// We only care if it has any observations at all
					if (tracker.getObservations() > 0) {
						writer.write(String.format(LISTENER_HEADER, type));
						
						writer.write(SEPERATION_LINE);
						saveStatistics(writer, tracker, type);
						writer.write(SEPERATION_LINE);
					}
				}
				// Next plugin
				writer.write(NEWLINE);
			}
			
		} finally {
			if (writer != null) {
				// Don't suppress exceptions
				writer.flush();
				Closeables.closeQuietly(writer);
			}
		}
	}
	
	private void saveStatistics(Writer destination, TimedTracker tracker, ListenerType type) throws IOException {
		StatisticsStream[] streams = tracker.getStatistics();
		StatisticsStream sum = new StatisticsStream();
		int count = 0;
		
		destination.write(STATISTICS_HEADER);
		destination.write(SEPERATION_LINE);
		
		// Write every packet ID that we care about
		for (int i = 0; i < Packets.PACKET_COUNT; i++) {
			final StatisticsStream stream = streams[i];
			
			if (stream != null && stream.getCount() > 0) {
				printStatistic(destination, Integer.toString(i), stream);
				
				// Add it
				count++;
				sum = sum.add(stream);
			}
		}
		
		// Write the sum - if its useful
		if (count > 1) {
			printStatistic(destination, "SUM", sum);
		}
		// These are executed on the main thread
		if (type == ListenerType.SYNC_SERVER_SIDE) {
			destination.write(String.format(SUM_MAIN_THREAD, 
				toMilli(sum.getCount() * sum.getMean())
			));
		}
	}

	private void printStatistic(Writer destination, String key, final StatisticsStream stream) throws IOException {
		destination.write(String.format(STATISTICS_ROW, 
			key, stream.getCount(), 
			toMilli(stream.getMinimum()), 
			toMilli(stream.getMaximum()), 
			toMilli(stream.getMean()), 
			toMilli(stream.getStandardDeviation())
		));
	}

	/**
	 * Convert a value in nanoseconds to milliseconds.
	 * @param value - the value.
	 * @return The value in milliseconds.
	 */
	private double toMilli(double value) {
		return value / 1000000.0;
	}
}
