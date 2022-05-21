package com.comphenix.protocol.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Marker containing the serialized packet data seen from the network, or output handlers that will serialize the
 * current packet.
 *
 * @author Kristian
 */
public class NetworkMarker {

	// The input data
	private final PacketType type;
	private final ConnectionSide side;

	// Post-processing of the packet
	private Set<PacketPostListener> postListeners;
	private Set<ScheduledPacket> scheduledPackets;

	/**
	 * Construct a new network marker.
	 * <p>
	 * The input buffer is only non-null for client-side packets.
	 *
	 * @param side - which side this marker belongs to.
	 * @param type - packet type
	 */
	public NetworkMarker(@Nonnull ConnectionSide side, PacketType type) {
		this.side = Preconditions.checkNotNull(side, "side cannot be NULL.");
		this.type = type;
	}

	/**
	 * Determine if the given marker has any post listeners.
	 *
	 * @param marker - the marker to check.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public static boolean hasPostListeners(NetworkMarker marker) {
		return marker != null && !marker.getPostListeners().isEmpty();
	}

	/**
	 * Retrieve the network marker of a particular event without creating it.
	 * <p>
	 * This is an internal method that should not be used by API users.
	 *
	 * @param event - the event.
	 * @return The network marker.
	 */
	public static NetworkMarker getNetworkMarker(PacketEvent event) {
		return event.networkMarker;
	}

	/**
	 * Retrieve the scheduled packets of a particular network marker without constructing the list.
	 * <p>
	 * This is an internal method that should not be used by API users.
	 *
	 * @param marker - the marker.
	 * @return The list, or NULL if not found or initialized.
	 */
	public static Set<ScheduledPacket> readScheduledPackets(NetworkMarker marker) {
		return marker.scheduledPackets;
	}

	/**
	 * Retrieve whether or not this marker belongs to a client or a server side packet.
	 *
	 * @return The side the parent packet belongs to.
	 */
	public ConnectionSide getSide() {
		return this.side;
	}

	public PacketType getType() {
		return this.type;
	}

	/**
	 * Add a listener that is invoked after a packet has been successfully sent to the client, or received by the server.
	 * <p>
	 * Received packets are not guarenteed to have been fully processed, but packets passed to {@link
	 * ProtocolManager#receiveClientPacket(Player, PacketContainer)} will be processed after the current packet event.
	 * <p>
	 * Note that post listeners will be executed asynchronously off the main thread. They are not executed in any defined
	 * order.
	 *
	 * @param listener - the listener that will be invoked.
	 * @return TRUE if it was added.
	 */
	public boolean addPostListener(PacketPostListener listener) {
		if (this.postListeners == null) {
			this.postListeners = new HashSet<>();
		}

		return this.postListeners.add(listener);
	}

	/**
	 * Remove the first instance of the given listener.
	 *
	 * @param listener - listener to remove.
	 * @return TRUE if it was removed, FALSE otherwise.
	 */
	public boolean removePostListener(PacketPostListener listener) {
		if (this.postListeners != null) {
			return this.postListeners.remove(listener);
		}

		return false;
	}

	/**
	 * Retrieve an immutable view of all the listeners that will be invoked once the packet has been sent or received.
	 *
	 * @return Every post packet listener. Never NULL.
	 */
	public Set<PacketPostListener> getPostListeners() {
		return this.postListeners != null ? this.postListeners : Collections.emptySet();
	}

	/**
	 * Retrieve a list of packets that will be schedule (in-order) when the current packet has been successfully
	 * transmitted.
	 * <p>
	 * This list is modifiable.
	 *
	 * @return List of packets that will be scheduled.
	 */
	public Set<ScheduledPacket> getScheduledPackets() {
		if (this.scheduledPackets == null) {
			this.scheduledPackets = new HashSet<>();
		}

		return this.scheduledPackets;
	}

	/**
	 * Ensure that the packet event is server side.
	 */
	private void checkServerSide() {
		if (this.side.isForClient()) {
			throw new IllegalStateException("Must be a server side packet.");
		}
	}
}
