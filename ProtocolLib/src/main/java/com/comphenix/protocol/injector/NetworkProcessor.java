package com.comphenix.protocol.injector;

import java.util.List;
import java.util.PriorityQueue;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketOutputHandler;
import com.comphenix.protocol.events.PacketPostListener;
import com.comphenix.protocol.events.ScheduledPacket;

/**
 * Represents a processor for network markers.
 * @author Kristian
 */
public class NetworkProcessor {
	private ErrorReporter reporter;
	
	/**
	 * Construct a new network processor.
	 * @param reporter - the reporter.
	 */
	public NetworkProcessor(ErrorReporter reporter) {
		this.reporter = reporter;
	}
	
	/**
	 * Process the serialized packet byte array with the given network marker.
	 * @param event - current packet event.
	 * @param marker - the network marker.
	 * @param input - the input array.
	 * @return The output array.
	 */
	public byte[] processOutput(PacketEvent event, NetworkMarker marker, final byte[] input) {
		// Bit of a hack - but we need the performance
		PriorityQueue<PacketOutputHandler> handlers = (PriorityQueue<PacketOutputHandler>) 
			marker.getOutputHandlers();
		byte[] output = input;
		
		// Let each handler prepare the actual output
		while (!handlers.isEmpty()) {
			PacketOutputHandler handler = handlers.poll();
			
			try {
				byte[] changed = handler.handle(event, output);
				
				// Don't break just because a plugin returned NULL
				if (changed != null) {
					output = changed;
				} else {
					throw new IllegalStateException("Handler cannot return a NULL array.");
				}
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				reporter.reportMinimal(handler.getPlugin(), "PacketOutputHandler.handle()", e);
			}
		}
		return output;
	}

	/**
	 * Invoke the post listeners and packet transmission, if any.
	 * @param marker - the network marker, or NULL.
	 */
	public void invokePostEvent(PacketEvent event, NetworkMarker marker) {
		if (marker == null)
			return;
		
		if (NetworkMarker.hasPostListeners(marker)) {
			// Invoke every sent listener
			for (PacketPostListener listener : marker.getPostListeners()) {
				try {
					listener.onPostEvent(event);
				} catch (OutOfMemoryError e) {
					throw e;
				} catch (ThreadDeath e) {
					throw e;
				} catch (Throwable e) {
					reporter.reportMinimal(listener.getPlugin(), "SentListener.run()", e);
				}
			}
		}
		sendScheduledPackets(marker);
	}
	
	/**
	 * Send any scheduled packets.
	 * @param marker - the network marker.
	 */
	private void sendScheduledPackets(NetworkMarker marker) {
		// Next, invoke post packet transmission
		List<ScheduledPacket> scheduled = NetworkMarker.readScheduledPackets(marker);
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		
		if (scheduled != null) {
			for (ScheduledPacket packet : scheduled) {
				packet.schedule(manager);
			}
		}
	}
 }
