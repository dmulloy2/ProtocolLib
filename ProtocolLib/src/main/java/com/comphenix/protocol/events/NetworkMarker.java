package com.comphenix.protocol.events;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.annotation.Nonnull;

import com.comphenix.protocol.utility.StreamSerializer;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

/**
 * Marker containing the serialized packet data seen from the network, 
 * or output handlers that will serialize the current packet.
 * 
 * @author Kristian
 */
public class NetworkMarker {
	// Custom network handler
	private PriorityQueue<PacketOutputHandler> outputHandlers;
	// The input buffer
	private ByteBuffer inputBuffer;
	
	private final ConnectionSide side;
	
	// Cache serializer too
	private StreamSerializer serializer;
	
	/**
	 * Construct a new network marker.
	 * <p>
	 * The input buffer is only non-null for client-side packets.
	 * @param side - whether or not this marker belongs to a client or server packet. 
	 * @param inputBuffer - the read serialized packet data.
	 */
	public NetworkMarker(@Nonnull ConnectionSide side, byte[] inputBuffer) {
		this.side = Preconditions.checkNotNull(side, "side cannot be NULL.");
		
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
	 * Retrieve the serialized packet data (excluding the header) from the network input stream.
	 * <p>
	 * The returned buffer is read-only. If the parent event is a server side packet this 
	 * method throws {@link IllegalStateException}.
	 * <p>
	 * It returns NULL if the packet was transmitted by a plugin locally.
	 * @return A byte buffer containing the raw packet data read from the network.
	 */
	public ByteBuffer getInputBuffer() {
		if (side.isForServer())
			throw new IllegalStateException("Server-side packets have no input buffer.");
		return inputBuffer != null ? inputBuffer.asReadOnlyBuffer() : null;
	}
	
	/**
	 * Retrieve the serialized packet data as an input stream.
	 * <p>
	 * The data is exactly the same as in {@link #getInputBuffer()}. 
	 * @see {@link #getInputBuffer()}
	 * @return The incoming serialized packet data as a stream, or NULL if the packet was transmitted locally.
	 */
	public DataInputStream getInputStream() {
		if (side.isForServer())
			throw new IllegalStateException("Server-side packets have no input buffer.");
		if (inputBuffer == null)
			return null;
		
		return new DataInputStream(
				new ByteArrayInputStream(inputBuffer.array())
		);
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
	 * Ensure that the packet event is server side.
	 */
	private void checkServerSide() {
		if (side.isForClient()) {
			throw new IllegalStateException("Must be a server side packet.");
		}
	}
	
	/**
	 * Determine if the given marker has any output handlers.
	 * @param marker - the marker to check.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public static boolean hasOutputHandlers(NetworkMarker marker) {
		return marker != null && !marker.getOutputHandlers().isEmpty();
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
}
