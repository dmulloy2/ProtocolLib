package com.comphenix.protocol.injector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketPostListener;
import com.comphenix.protocol.events.ScheduledPacket;

import java.util.Set;

/**
 * Represents a processor for network markers.
 *
 * @author Kristian
 */
public class NetworkProcessor {

	private final ErrorReporter reporter;

	/**
	 * Construct a new network processor.
	 *
	 * @param reporter - the reporter.
	 */
	public NetworkProcessor(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	/**
	 * Invoke the post listeners and packet transmission, if any.
	 *
	 * @param event  - PacketEvent
	 * @param marker - the network marker, or NULL.
	 */
	public void invokePostEvent(PacketEvent event, NetworkMarker marker) {
		if (marker == null) {
			return;
		}

		if (event != null && NetworkMarker.hasPostListeners(marker)) {
			// Invoke every sent listener
			for (PacketPostListener listener : marker.getPostListeners()) {
				try {
					listener.onPostEvent(event);
				} catch (OutOfMemoryError | ThreadDeath e) {
					throw e;
				} catch (Throwable e) {
					this.reporter.reportMinimal(listener.getPlugin(), "SentListener.run()", e);
				}
			}
		}

		this.sendScheduledPackets(marker);
	}

	/**
	 * Send any scheduled packets.
	 *
	 * @param marker - the network marker.
	 */
	private void sendScheduledPackets(NetworkMarker marker) {
		// Next, invoke post packet transmission
		Set<ScheduledPacket> scheduled = NetworkMarker.readScheduledPackets(marker);
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();

		if (scheduled != null) {
			for (ScheduledPacket packet : scheduled) {
				packet.schedule(manager);
			}
		}
	}
}
