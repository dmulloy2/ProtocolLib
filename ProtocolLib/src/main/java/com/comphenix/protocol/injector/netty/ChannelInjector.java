package com.comphenix.protocol.injector.netty;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.socket.SocketChannel;
import net.minecraft.util.io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.util.io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.util.io.netty.util.internal.TypeParameterMatcher;
import net.sf.cglib.proxy.Factory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketOutputHandler;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.reflect.FuzzyReflection.FieldAccessor;
import com.comphenix.protocol.reflect.FuzzyReflection.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;

/**
 * Represents a channel injector.
 * @author Kristian
 */
class ChannelInjector extends ByteToMessageDecoder {
	public static final ReportType REPORT_CANNOT_INTERCEPT_SERVER_PACKET = new ReportType("Unable to intercept a written server packet.");
	public static final ReportType REPORT_CANNOT_INTERCEPT_CLIENT_PACKET = new ReportType("Unable to intercept a read client packet.");
	
	/**
	 * Represents a listener for received or sent packets.
	 * @author Kristian
	 */
	interface ChannelListener {
		/**
		 * Invoked when a packet is being sent to the client.
		 * <p>
		 * This is invoked on the main thread.
		 * @param injector - the channel injector.
		 * @param packet - the packet.
		 * @param marker - the associated network marker, if any.
		 * @return The new packet, if it should be changed, or NULL to cancel.
		 */
		public Object onPacketSending(ChannelInjector injector, Object packet, NetworkMarker marker);
		
		/**
		 * Invoked when a packet is being received from a client.
		 * <p>
		 * This is invoked on an asynchronous worker thread.
		 * @param injector - the channel injector.
		 * @param packet - the packet.
		 * @param marker - the associated network marker, if any.
		 * @return The new packet, if it should be changed, or NULL to cancel.
		 */
		public Object onPacketReceiving(ChannelInjector injector, Object packet, NetworkMarker marker);
		
		/**
		 * Determine if we need the buffer data of a given client side packet.
		 * @param packetClass - the packet class.
		 * @return TRUE if we do, FALSE otherwise.
		 */
		public boolean includeBuffer(Class<?> packetClass);
		
		/**
		 * Retrieve the current error reporter.
		 * @return The error reporter.
		 */
		public ErrorReporter getReporter();
	}
	
	private static final ConcurrentMap<Player, ChannelInjector> cachedInjector = new MapMaker().weakKeys().makeMap();
	
	// Saved accessors
	private static MethodAccessor DECODE_BUFFER;
	private static MethodAccessor ENCODE_BUFFER;
	private static FieldAccessor ENCODER_TYPE_MATCHER;

	// For retrieving the protocol
	private static FieldAccessor PROTOCOL_ACCESSOR;
	
	// The player, or temporary player
	private Player player;
	
	// The player connection
	private Object playerConnection;
	
	// The current network manager and channel
	private final Object networkManager;
	private final Channel originalChannel;
	private VolatileField channelField;

	// Known network markers
	private ConcurrentMap<Object, NetworkMarker> packetMarker = new MapMaker().weakKeys().makeMap();
	private ConcurrentMap<NetworkMarker, PacketEvent> markerEvent = new MapMaker().weakKeys().makeMap();
	
	// Packets to ignore
	private Set<Object> ignoredPackets = Collections.newSetFromMap(new MapMaker().weakKeys().<Object, Boolean>makeMap());
	
	// Other handlers
	private ByteToMessageDecoder vanillaDecoder;
	private MessageToByteEncoder<Object> vanillaEncoder;
	
	// Our extra handler
	private MessageToByteEncoder<Object> protocolEncoder;
	
	// The channel listener
	private ChannelListener channelListener;
	
	// Closed
	private boolean injected;
	private boolean closed;
	
	/**
	 * Construct a new channel injector.
	 * @param player - the current player, or temporary player.
	 * @param networkManager - its network manager.
	 * @param channel - its channel.
	 */
	private ChannelInjector(Player player, Object networkManager, Channel channel, ChannelListener channelListener) {
		this.player =  Preconditions.checkNotNull(player, "player cannot be NULL");
		this.networkManager =  Preconditions.checkNotNull(networkManager, "networkMananger cannot be NULL");
		this.originalChannel = Preconditions.checkNotNull(channel, "channel cannot be NULL");
		this.channelListener = Preconditions.checkNotNull(channelListener, "channelListener cannot be NULL");
	
		// Get the channel field
		this.channelField = new VolatileField(
			FuzzyReflection.fromObject(networkManager, true).
				getFieldByType("channel", Channel.class), 
			networkManager, true);
	}
	
	/**
	 * Construct or retrieve a channel injector from an existing Bukkit player.
	 * @param player - the existing Bukkit player.
	 * @param channelListener - the listener.
	 * @return A new injector, or an existing injector associated with this player.
	 */
	public static ChannelInjector fromPlayer(Player player, ChannelListener listener) {
		ChannelInjector injector = cachedInjector.get(player);
		
		if (injector != null)
			return injector;
		
		Object networkManager = MinecraftFields.getNetworkManager(player);
		Channel channel = FuzzyReflection.getFieldValue(networkManager, Channel.class, true);
		
		// See if a channel has already been created
		injector = (ChannelInjector) findChannelHandler(channel, ChannelInjector.class);
		
		if (injector != null) {
			// Update the player instance
			injector.player = player;
		} else {
			injector = new ChannelInjector(player, networkManager, channel, listener);
		}
		// Cache injector and return
		cachedInjector.put(player, injector);
		return injector;
	}
	
	/**
	 * Construct a new channel injector for the given channel.
	 * @param channel - the channel.
	 * @param playerFactory - a temporary player creator.
	 * @param channelListener - the listener.
	 * @param loader - the current (plugin) class loader.
	 * @return The channel injector.
	 */
	public static ChannelInjector fromChannel(Channel channel, ChannelListener listener, TemporaryPlayerFactory playerFactory) {
		Object networkManager = findNetworkManager(channel);	
		Player temporaryPlayer = playerFactory.createTemporaryPlayer(Bukkit.getServer());
		ChannelInjector injector = new ChannelInjector(temporaryPlayer, networkManager, channel, listener);
		
		// Initialize temporary player
		TemporaryPlayerFactory.setInjectorInPlayer(temporaryPlayer, new ChannelSocketInjector(injector));
		return injector;
	}
	
	/**
	 * Inject the current channel.
	 */
	@SuppressWarnings("unchecked")
	public boolean inject() {
		synchronized (networkManager) {
			if (originalChannel instanceof Factory)
				return false;
			
			// Don't inject the same channel twice
			if (findChannelHandler(originalChannel, ChannelInjector.class) != null) {
				// Invalidate cache
				if (player != null)
					cachedInjector.remove(player);
				return false;
			}
			
			// Get the vanilla decoder, so we don't have to replicate the work
			vanillaDecoder = (ByteToMessageDecoder) originalChannel.pipeline().get("decoder");
			vanillaEncoder = (MessageToByteEncoder<Object>) originalChannel.pipeline().get("encoder");
			patchEncoder(vanillaEncoder);
			
			if (vanillaDecoder == null)
				throw new IllegalArgumentException("Unable to find vanilla decoder.in " + originalChannel.pipeline());
			if (vanillaEncoder == null)
				throw new IllegalArgumentException("Unable to find vanilla encoder in " + originalChannel.pipeline());
			
			if (DECODE_BUFFER == null)
				DECODE_BUFFER = FuzzyReflection.getMethodAccessor(vanillaDecoder.getClass(), 
					"decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
			if (ENCODE_BUFFER == null)
				ENCODE_BUFFER = FuzzyReflection.getMethodAccessor(vanillaEncoder.getClass(),
					"encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
			
			protocolEncoder = new MessageToByteEncoder<Object>() {
				@Override
				protected void encode(ChannelHandlerContext ctx, Object packet, ByteBuf output) throws Exception {
					try {
						NetworkMarker marker = getMarker(output);
						PacketEvent event = markerEvent.remove(marker);
						
						if (event != null && NetworkMarker.hasOutputHandlers(marker)) {
							ByteBuf packetBuffer = ctx.alloc().buffer();
							ENCODE_BUFFER.invoke(vanillaEncoder, ctx, packet, packetBuffer);
							byte[] data = getBytes(packetBuffer);
							
							for (PacketOutputHandler handler : marker.getOutputHandlers()) {
								handler.handle(event, data);
							}
							// Write the result
							output.writeBytes(data);
							return;
						}
					} catch (Exception e) {
						channelListener.getReporter().reportDetailed(this, 
							Report.newBuilder(REPORT_CANNOT_INTERCEPT_SERVER_PACKET).callerParam(packet).error(e).build());
					} finally {
						// Attempt to handle the packet nevertheless
						ENCODE_BUFFER.invoke(vanillaEncoder, ctx, packet, output);
					}
				}
				
				public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {
					throwable.printStackTrace();
				}
			};
			
			// Insert our handlers - note that we effectively replace the vanilla encoder/decoder 
			originalChannel.pipeline().addBefore("decoder", "protocol_lib_decoder", this);
			originalChannel.pipeline().addAfter("encoder", "protocol_lib_encoder", protocolEncoder);
			
			// Intercept all write methods
			channelField.setValue(new ChannelProxy(originalChannel) {
				@Override
				protected Object onMessageWritten(Object message) {
					return channelListener.onPacketSending(ChannelInjector.this, message, packetMarker.get(message));
				}
			});
			
			injected = true;
			return true;
		}
	}
	
	/**
	 * This method patches the encoder so that it skips already created packets.
	 * @param encoder - the encoder to patch.
	 */
	private void patchEncoder(MessageToByteEncoder<Object> encoder) {
		if (ENCODER_TYPE_MATCHER == null) {
			ENCODER_TYPE_MATCHER = FuzzyReflection.getFieldAccessor(encoder.getClass(), "matcher", true);
		}
		ENCODER_TYPE_MATCHER.set(encoder, TypeParameterMatcher.get(MinecraftReflection.getPacketClass()));
	}
	
	/**
	 * Close the current injector.
	 */
	public void close() {
		if (!closed) {
			closed = true;
			
			if (injected) {
				channelField.revertValue();
				
				try {
					originalChannel.pipeline().remove(this);
					originalChannel.pipeline().remove(protocolEncoder);
				} catch (NoSuchElementException e) {
					// Ignore it - the player has logged out
				}
			}
		}
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuffer, List<Object> packets) throws Exception {
		try {
			byteBuffer.markReaderIndex();
			DECODE_BUFFER.invoke(vanillaDecoder, ctx, byteBuffer, packets);
			
			if (packets.size() > 0) {
				Object input = packets.get(0);
				Class<?> packetClass = input.getClass();
				NetworkMarker marker = null;
				
				if (channelListener.includeBuffer(packetClass)) {
					byteBuffer.resetReaderIndex();
					marker = new NetworkMarker(ConnectionSide.CLIENT_SIDE, getBytes(byteBuffer));
				}
				Object output = channelListener.onPacketReceiving(this, input, marker);
				
				// Handle packet changes
				if (output == null)
					packets.clear();
				else if (output != input)
					packets.set(0, output);
			}
		} catch (Exception e) {
			channelListener.getReporter().reportDetailed(this, 
					Report.newBuilder(REPORT_CANNOT_INTERCEPT_CLIENT_PACKET).callerParam(byteBuffer).error(e).build());
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		
		// See NetworkManager.channelActive(ChannelHandlerContext) for why
		if (channelField != null) {
			channelField.refreshValue();
		}
	}
	
	/**
	 * Retrieve every byte in the given byte buffer.
	 * @param buffer - the buffer.
	 * @return The bytes.
	 */
	private byte[] getBytes(ByteBuf buffer){
		byte[] data = new byte[buffer.readableBytes()];
		
		buffer.readBytes(data);
		return data;
	}
	
	/**
	 * Disconnect the current player.
	 * @param message - the disconnect message, if possible.
	 */
	private void disconnect(String message) {
		// If we're logging in, we can only close the channel
		if (playerConnection == null || player instanceof Factory) {
			originalChannel.disconnect();
		} else {
			// Call the disconnect method
			try {
				MinecraftMethods.getDisconnectMethod(playerConnection.getClass()).
					invoke(playerConnection, message);
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to invoke disconnect method.", e);
			}
		}
	}
	
	/**
	 * Send a packet to a player's client.
	 * @param packet - the packet to send.
	 * @param marker - the network marker.
	 * @param filtered - whether or not the packet is filtered.
	 */
	public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) {
		saveMarker(packet, marker);
		
		// Record if this packet should be ignored by most listeners
		if (!filtered) {
			ignoredPackets.add(packet);
		}
		
		// Attempt to send the packet with NetworkMarker.handle(), or the PlayerConnection if its active
		try {
			if (player instanceof Factory) {
				MinecraftMethods.getNetworkManagerHandleMethod().invoke(networkManager, packet, new GenericFutureListener[0]);
			} else {
				MinecraftMethods.getSendPacketMethod().invoke(getPlayerConnection(), packet);
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to send server packet " + packet, e);
		}
	}
	
	/**
	 * Recieve a packet on the server.
	 * @param packet - the (NMS) packet to send.
	 * @param marker - the network marker.
	 * @param filtered - whether or not the packet is filtered.
	 */
	public void recieveClientPacket(Object packet, NetworkMarker marker, boolean filtered) {
		saveMarker(packet, marker);
	
		if (!filtered) {
			ignoredPackets.add(packet);
		}
		
		try {
			MinecraftMethods.getNetworkManagerReadPacketMethod().invoke(networkManager, null, packet);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to receive client packet " + packet, e);
		}
	}
	
	/**
	 * Retrieve the current protocol state.
	 * @return The current protocol.
	 */
	public Protocol getCurrentProtocol() {
		if (PROTOCOL_ACCESSOR == null) {
			PROTOCOL_ACCESSOR = FuzzyReflection.getFieldAccessor(
					networkManager.getClass(), MinecraftReflection.getEnumProtocolClass(), true);
		}
		return Protocol.fromVanilla((Enum<?>) PROTOCOL_ACCESSOR.get(networkManager));
	}
	
	/**
	 * Retrieve the player connection of the current player.
	 * @return The player connection.
	 */
	private Object getPlayerConnection() {
		if (playerConnection == null) {
			playerConnection = MinecraftFields.getPlayerConnection(player);
		}
		return playerConnection;
	}
	
	/**
	 * Undo the ignore status of a packet.
	 * @param packet - the packet.
	 * @return TRUE if the ignore status was undone, FALSE otherwise.
	 */
	public boolean unignorePacket(Object packet) {
		return ignoredPackets.remove(packet);
	}
	
	/**
	 * Ignore the given packet.
	 * @param packet - the packet to ignore.
	 * @return TRUE if it was ignored, FALSE if it already is ignored.
	 */
	public boolean ignorePacket(Object packet) {
		return ignoredPackets.add(packet);
	}
	
	/**
	 * Retrieve the network marker associated with a given packet.
	 * @param packet - the packet.
	 * @return The network marker.
	 */
	public NetworkMarker getMarker(Object packet) {
		return packetMarker.get(packet);
	}
	
	/**
	 * Associate a given network marker with a specific packet.
	 * @param packet - the NMS packet.
	 * @param marker - the associated marker.
	 */
	public void saveMarker(Object packet, NetworkMarker marker) {
		if (marker != null) {
			packetMarker.put(packet, marker);
		}
	}
	
	/**
	 * Associate a given network marker with a packet event.
	 * @param marker - the marker.
	 * @param event - the packet event
	 */
	public void saveEvent(NetworkMarker marker, PacketEvent event) {
		if (marker != null) {
			markerEvent.put(marker, event);
		}
	}
		
	/**
	 * Find the network manager in a channel's pipeline.
	 * @param channel - the channel.
	 * @return The network manager.
	 */
	private static Object findNetworkManager(Channel channel) {
		// Find the network manager
		Object networkManager = findChannelHandler(channel, MinecraftReflection.getNetworkManagerClass());
		
		if (networkManager != null)
			return networkManager;
		throw new IllegalArgumentException("Unable to find NetworkManager in " + channel);
	}
	
	/**
	 * Find the first channel handler that is assignable to a given type.
	 * @param channel - the channel.
	 * @param clazz - the type.
	 * @return The first handler, or NULL.
	 */
	private static ChannelHandler findChannelHandler(Channel channel, Class<?> clazz) {
		for (Entry<String, ChannelHandler> entry : channel.pipeline()) {
			if (clazz.isAssignableFrom(entry.getValue().getClass())) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Retrieve the current player or temporary player associated with the injector.
	 * @return The current player.
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Determine if the channel has already been injected.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public boolean isInjected() {
		return injected;
	}
	
	/**
	 * Determine if this channel has been closed and cleaned up.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * Represents a socket injector that foreards to the current channel injector.
	 * @author Kristian
	 */
	private static class ChannelSocketInjector implements SocketInjector {
		private final ChannelInjector injector;
		
		public ChannelSocketInjector(ChannelInjector injector) {
			this.injector = injector;
		}

		@Override
		public Socket getSocket() throws IllegalAccessException {
			return NettySocketAdaptor.adapt((SocketChannel) injector.originalChannel);
		}

		@Override
		public SocketAddress getAddress() throws IllegalAccessException {
			return injector.originalChannel.localAddress();
		}

		@Override
		public void disconnect(String message) throws InvocationTargetException {
			injector.disconnect(message);
		}

		@Override
		public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) throws InvocationTargetException {
			injector.sendServerPacket(packet, marker, filtered);
		}

		@Override
		public Player getPlayer() {
			return injector.player;
		}

		@Override
		public Player getUpdatedPlayer() {
			return injector.player;
		}

		@Override
		public void transferState(SocketInjector delegate) {
			// Do nothing
		}

		@Override
		public void setUpdatedPlayer(Player updatedPlayer) {
			injector.player = updatedPlayer;
		}	
	}
}
