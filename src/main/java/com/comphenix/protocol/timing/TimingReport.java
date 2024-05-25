package com.comphenix.protocol.timing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;

import com.comphenix.protocol.PacketType;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

public class TimingReport {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String META_STARTED = "Started: %s" + NEWLINE;
    private static final String META_STOPPED = "Stopped: %s (after %s seconds)" + NEWLINE;
    private static final String PLUGIN_HEADER = "=== PLUGIN %s ===" + NEWLINE;
    private static final String LISTENER_HEADER = " TYPE: %s " + NEWLINE;
    private static final String SEPERATION_LINE = " " + Strings.repeat("-", 139) + NEWLINE;
    private static final String STATISTICS_HEADER =
        " Protocol:      Name:                         Count:       Min (ms):       " +
        "Max (ms):       Mean (ms):      Std (ms): " + NEWLINE;
    private static final String STATISTICS_ROW =    " %-14s %-29s %-12d %-15.6f %-15.6f %-15.6f %.6f " + NEWLINE;
    private static final String SUM_MAIN_THREAD = " => Time on main thread: %.6f ms" + NEWLINE;

	private final Date startTime;
	private final Date stopTime;
	private final ImmutableMap<String, ImmutableMap<TimingListenerType, PluginTimingTracker>> trackerMap;

	public TimingReport(Date startTime, Date stopTime, ImmutableMap<String, ImmutableMap<TimingListenerType, PluginTimingTracker>> trackerMap) {
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.trackerMap = trackerMap;
	}

	public void saveTo(Path path) throws IOException {
		final long seconds = Math.abs((stopTime.getTime() - startTime.getTime()) / 1000);

		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			// Write some timing information
			writer.write(String.format(META_STARTED, startTime));
			writer.write(String.format(META_STOPPED, stopTime, seconds));
			writer.write(NEWLINE);

			for (Map.Entry<String, ImmutableMap<TimingListenerType, PluginTimingTracker>> pluginEntry : trackerMap.entrySet()) {
				writer.write(String.format(PLUGIN_HEADER, pluginEntry.getKey()));

				for (Map.Entry<TimingListenerType, PluginTimingTracker> entry : pluginEntry.getValue().entrySet()) {
					TimingListenerType type = entry.getKey();
					PluginTimingTracker tracker = entry.getValue();

					// We only care if it has any observations at all
					if (tracker.hasReceivedData()) {
						writer.write(String.format(LISTENER_HEADER, type));

						writer.write(SEPERATION_LINE);
						saveStatistics(writer, tracker, type);
						writer.write(SEPERATION_LINE);
					}
				}
				// Next plugin
				writer.write(NEWLINE);
			}
		}
	}

    private void saveStatistics(Writer destination, PluginTimingTracker tracker, TimingListenerType type) throws IOException {
        Map<PacketType, StatisticsStream> streams = tracker.getStatistics();
        StatisticsStream sum = new StatisticsStream();
        int count = 0;

        destination.write(STATISTICS_HEADER);
        destination.write(SEPERATION_LINE);

        // Write every packet ID that we care about
        for (PacketType key : new TreeSet<>(streams.keySet())) {
            final StatisticsStream stream = streams.get(key);

            if (stream != null && stream.getCount() > 0) {
                printStatistic(destination, key, stream);

                // Add it
                count++;
                sum = sum.add(stream);
            }
        }

        // Write the sum - if its useful
        if (count > 1) {
            printStatistic(destination, null, sum);
        }
        // These are executed on the main thread
        if (type == TimingListenerType.SYNC_OUTBOUND) {
            destination.write(String.format(SUM_MAIN_THREAD,
                nanoToMillis(sum.getCount() * sum.getMean())
            ));
        }
    }

    private void printStatistic(Writer destination, PacketType key, final StatisticsStream stream) throws IOException {
        destination.write(String.format(STATISTICS_ROW,
            key != null ? key.getProtocol() : "SUM",
            key != null ? key.name() : "-",
            stream.getCount(),
            nanoToMillis(stream.getMinimum()),
            nanoToMillis(stream.getMaximum()),
            nanoToMillis(stream.getMean()),
            nanoToMillis(stream.getStandardDeviation())
        ));
    }

    /**
     * Convert a value in nanoseconds to milliseconds.
     * @param value - the value.
     * @return The value in milliseconds.
     */
    private double nanoToMillis(double value) {
        return value / 1000000.0;
    }
}
