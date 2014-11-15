package com.comphenix.protocol.events;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.utility.ByteBufferInputStream;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.StreamSerializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

/**
 * Marker containing the serialized packet data seen from the network, 
 * or output handlers that will serialize the current packet.
 * 
 * @author Kristian
 */
public abstract class NetworkMarker {	
	public static class EmptyBufferMarker extends NetworkMarker {
		public EmptyBufferMarker(@Nonnull ConnectionSide side) {
			super(side, (byte[]) null, null);
		}

		@Override
		protected DataInputStream skipHeader(DataInputStream input) throws IOException {
			throw new IllegalStateException("Buffer is empty.");
		}

		@Override
		protected ByteBuffer addHeader(ByteBuffer buffer, PacketType type) {
			throw new IllegalStateException("Buffer is empty.");
		}

		@Override
		protected DataInputStream addHeader(DataInputStream input, PacketType type) {
			throw new IllegalStateException("Buffer is empty.");
		}		
	}
	
	// Custom network handler
	private PriorityQueue<PacketOutputHandler> outputHandlers;
	// Post listeners
	private List<PacketPostListener> postListeners;
	// Post packets
	private List<ScheduledPacket> scheduledPackets;
	
	// The input buffer
	private ByteBuffer inputBuffer;
	private final ConnectionSide side;
	private final PacketType type;
	
	// Cache serializer too
	private StreamSerializer serializer;
	
	/**
	 * Construct a new network marker.
	 * @param side - whether or not this marker belongs to a client or server packet. 
	 * @param inputBuffer - the read serialized packet data.
	 */
	public NetworkMarker(@Nonnull ConnectionSide side, ByteBuffer inputBuffer, PacketType type) {
		this.side = Preconditions.checkNotNull(side, "side cannot be NULL.");
		this.inputBuffer = inputBuffer;
		this.type = type;
	}
	
	/**
	 * Construct a new network marker.
	 * <p>
	 * The input buffer is only non-null for client-side packets.
	 * @param side - whether or not this marker belongs to a client or server packet. 
	 * @param inputBuffer - the read serialized packet data.
	 * @param handler - handle skipping headers.
	 */
	public NetworkMarker(@Nonnull ConnectionSide side, byte[] inputBuffer, PacketType type) {
		this.side = Preconditions.checkNotNull(side, "side cannot be NULL.");
		this.type = type;
			
		if (inputBuffer != null) {
			this.inputBuffer = ByteBuffer.wrap(inputBuffer);
		}
	}
	
	/**
	 * Retrieve whether or not this marker belongs to a client or a server side packet.
	 * @return The side the parent packet belongs to.
	 */
	public ConnectionSide getSide() {
		return side;
	}

	/**
	 * Retrieve a utility class for serializing and deserializing Minecraft objects.
	 * @return Serialization utility class.
	 */
	public StreamSerializer getSerializer() {
		if (serializer == null)
			serializer = new StreamSerializer();
		return serializer;
	}
	
	/**
	 * Retrieve the serialized packet data (excluding the header by default) from the network input stream.
	 * <p>
	 * The returned buffer is read-only. If the parent event is a server side packet this 
	 * method throws {@link IllegalStateException}.
	 * <p>
	 * It returns NULL if the packet was transmitted by a plugin locally.
	 * @return A byte buffer containing the raw packet data read from the network.
	 */
	public ByteBuffer getInputBuffer() {
		return getInputBuffer(true);
	}
		
	/**
	 * Retrieve the serialized packet data from the network input stream.
	 * <p>
	 * The returned buffer is read-only. If the parent event is a server side packet this 
	 * method throws {@link IllegalStateException}.
	 * <p>
	 * It returns NULL if the packet was transmitted by a plugin locally.
	 * @param excludeHeader - whether or not to exclude the packet ID header.
	 * @return A byte buffer containing the raw packet data read from the network.
	 */
	public ByteBuffer getInputBuffer(boolean excludeHeader) {
		if (side.isForServer())
			throw new IllegalStateException("Server-side packets have no input buffer.");
		
		if (inputBuffer != null) {
			ByteBuffer result = inputBuffer.asReadOnlyBuffer();
			
			try {
				if (excludeHeader) 
					result = skipHeader(result);
				else
					result = addHeader(result, type);
			} catch (IOException e) {
				throw new RuntimeException("Cannot skip packet header.", e);
			}
			return result;
		}
		return null;
	}
	
	/**
	 * Retrieve the serialized packet data (excluding the header by default) as an input stream.
	 * <p>
	 * The data is exactly the same as in {@link #getInputBuffer()}. 
	 * @see #getInputBuffer()
	 * @return The incoming serialized packet data as a stream, or NULL if the packet was transmitted locally.
	 */
	public DataInputStream getInputStream() {
		return getInputStream(true);
	}
	
	/**
	 * Retrieve the serialized packet data as an input stream.
	 * <p>
	 * The data is exactly the same as in {@link #getInputBuffer()}. 
	 * @see #getInputBuffer()
	 * @param excludeHeader - whether or not to exclude the packet ID header.
	 * @return The incoming serialized packet data as a stream, or NULL if the packet was transmitted locally.
	 */
	@SuppressWarnings("resource")
	public DataInputStream getInputStream(boolean excludeHeader) {
		if (side.isForServer())
			throw new IllegalStateException("Server-side packets have no input buffer.");
		if (inputBuffer == null)
			return null;
		
		DataInputStream input = new DataInputStream(
				new ByteArrayInputStream(inputBuffer.array())
		);
		
		try {
			if (excludeHeader) 
				input = skipHeader(input);
			else
				input = addHeader(input, type);
		} catch (IOException e) {
			throw new RuntimeException("Cannot skip packet header.", e);
		}
		return input;
	}
	
	/**
	 * Whether or not the output handlers have to write a packet header.
	 * @return TRUE if they do, FALSE otherwise.
	 */
	public boolean requireOutputHeader() {
		return MinecraftReflection.isUsingNetty();
	}
	
	/**
	 * Enqueue the given output handler for managing how the current packet will be written to the network stream.
	 * <p>
	 * Note that output handlers are not serialized, as most consumers will probably implement them using anonymous classes. 
	 * It is not safe to serialize anonymous classes, as their name depend on the order in which they are declared in the parent class.
	 * <p>
	 * This can only be invoked on server side packet events.
	 * @param handler - the handler that will take part in serializing the packet.
	 * @return TRUE if it was added, FALSE if it has already been added.
	 */
	public boolean addOutputHandler(@Nonnull PacketOutputHandler handler) {
		checkServerSide();
		Preconditions.checkNotNull(handler, "handler cannot be NULL.");
		
		// Lazy initialization - it's imperative that we save space and time here
		if (outputHandlers == null) {
			outputHandlers = new PriorityQueue<PacketOutputHandler>(10, new Comparator<PacketOutputHandler>() {
				@Override
				public int compare(PacketOutputHandler o1, PacketOutputHandler o2) {
					return Ints.compare(o1.getPriority().getSlot(), o2.getPriority().getSlot());
				}
			});
		}
		return outputHandlers.add(handler);
	}
	
	/**
	 * Remove a given output handler from the serialization queue.
	 * <p>
	 * This can only be invoked on server side packet events.
	 * @param handler - the handler to remove.
	 * @return TRUE if the handler was removed, FALSE otherwise.
	 */
	public boolean removeOutputHandler(@Nonnull PacketOutputHandler handler) {
		checkServerSide();
		Preconditions.checkNotNull(handler, "handler cannot be NULL.");
		
		if (outputHandlers != null) {
			return outputHandlers.remove(handler);
		}
		return false;
	}
	
	/**
	 * Retrieve every registered output handler in no particular order.
	 * @return Every registered output handler.
	 */
	@Nonnull
	public Collection<PacketOutputHandler> getOutputHandlers() {
		if (outputHandlers != null) {
			return outputHandlers;
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Add a listener that is invoked after a packet has been successfully sent to the client, or received 
	 * by the server. 
	 * <p>
	 * Received packets are not guarenteed to have been fully processed, but packets passed 
	 * to {@link ProtocolManager#recieveClientPacket(Player, PacketContainer)} will be processed after the
	 * current packet event.
	 * <p>
	 * Note that post listeners will be executed asynchronously off the main thread. They are not executed
	 * in any defined order.
	 * @param listener - the listener that will be invoked.
	 * @return TRUE if it was added.
	 */
	public boolean addPostListener(PacketPostListener listener) {
		if (postListeners == null)
			postListeners = Lists.newArrayList();
		return postListeners.add(listener);
	}
	
	/**
	 * Remove the first instance of the given listener.
	 * @param listener - listener to remove.
	 * @return TRUE if it was removed, FALSE otherwise.
	 */
	public boolean removePostListener(PacketPostListener listener) {
		if (postListeners != null) {
			return postListeners.remove(listener);
		}
		return false;
	}
	
	/**
	 * Retrieve an immutable view of all the listeners that will be invoked once the packet has been sent or received.
	 * @return Every post packet listener. Never NULL.
	 */
	public List<PacketPostListener> getPostListeners() {
		return postListeners != null ? Collections.unmodifiableList(postListeners) : Collections.<PacketPostListener>emptyList();
	}
	
	/**
	 * Retrieve a list of packets that will be schedule (in-order) when the current packet has been successfully transmitted.
	 * <p>
	 * This list is modifiable.
	 * @return List of packets that will be scheduled.
	 */
	public List<ScheduledPacket> getScheduledPackets() {
		if (scheduledPackets == null)
			scheduledPackets = Lists.newArrayList();
		return scheduledPackets;
	}
	
	/**
	 * Ensure that the packet event is server side.
	 */
	private void checkServerSide() {
		if (side.isForClient()) {
			throw new IllegalStateException("Must be a server side packet.");
		}
	}
	
	/**
	 * Return a byte buffer without the header in the current packet.
	 * <p>
	 * It's safe to modify the position of the buffer.
	 * @param buffer - a read-only byte source. 
	 */
	protected ByteBuffer skipHeader(ByteBuffer buffer) throws IOException {
		skipHeader(new DataInputStream(new ByteBufferInputStream(buffer)));
		return buffer;
	}
	
	/**
	 * Return an input stream without the header in the current packet.
	 * <p>
	 * It's safe to modify the input stream.
	 */
	protected abstract DataInputStream skipHeader(DataInputStream input) throws IOException;
	
	/**
	 * Return the byte buffer prepended with the packet header.
	 * @param buffer - the read-only byte buffer. 
	 * @param type - the current packet. 
	 * @return The byte buffer.
	 */
	protected abstract ByteBuffer addHeader(ByteBuffer buffer, PacketType type);
	
	/**
	 * Return the input stream prepended with the packet header.
	 * @param input - the input stream.
	 * @param type - the current packet. 
	 * @return The byte buffer.
	 */
	protected abstract DataInputStream addHeader(DataInputStream input, PacketType type);
	
	/**
	 * Determine if the given marker has any output handlers.
	 * @param marker - the marker to check.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public static boolean hasOutputHandlers(NetworkMarker marker) {
		return marker != null && !marker.getOutputHandlers().isEmpty();
	}
	
	/**
	 * Determine if the given marker has any post listeners.
	 * @param marker - the marker to check.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public static boolean hasPostListeners(NetworkMarker marker) {
		return marker != null && !marker.getPostListeners().isEmpty();
	}
	
	/**
	 * Retrieve the byte buffer stored in the given marker.
	 * @param marker - the marker.
	 * @return The byte buffer, or NULL if not found.
	 */
	public static byte[] getByteBuffer(NetworkMarker marker) {
		if (marker != null) {
			ByteBuffer buffer = marker.getInputBuffer();
			
			if (buffer != null) {
				byte[] data = new byte[buffer.remaining()];
				
				buffer.get(data, 0, data.length);
				return data;
			}
		}
		return null;
	}
	
	/**
	 * Retrieve the network marker of a particular event without creating it.
	 * <p>
	 * This is an internal method that should not be used by API users.
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
	 * @param marker - the marker.
	 * @return The list, or NULL if not found or initialized.
	 */
	public static List<ScheduledPacket> readScheduledPackets(NetworkMarker marker) {
		return marker.scheduledPackets;
	}
}
