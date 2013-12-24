package com.comphenix.protocol.injector.netty;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.util.com.mojang.authlib.GameProfile;
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

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketOutputHandler;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;

/**
 * Represents a channel injector.
 * @author Kristian
 */
class ChannelInjector extends ByteToMessageDecoder implements Injector {
	public static final ReportType REPORT_CANNOT_INTERCEPT_SERVER_PACKET = new ReportType("Unable to intercept a written server packet.");
	public static final ReportType REPORT_CANNOT_INTERCEPT_CLIENT_PACKET = new ReportType("Unable to intercept a read client packet.");
	
	// The login packet
	private static Class<?> PACKET_LOGIN_CLIENT = null;
	private static FieldAccessor LOGIN_GAME_PROFILE = null;
	
	// Saved accessors
	private static MethodAccessor DECODE_BUFFER;
	private static MethodAccessor ENCODE_BUFFER;
	private static FieldAccessor ENCODER_TYPE_MATCHER;

	// For retrieving the protocol
	private static FieldAccessor PROTOCOL_ACCESSOR;
	
	// The factory that created this injector
	private InjectionFactory factory;
	
	// The player, or temporary player
	private Player player;
	private Player updated;
	
	// The player connection
	private Object playerConnection;
	
	// The current network manager and channel
	private final Object networkManager;
	private final Channel originalChannel;
	private VolatileField channelField;

	// Known network markers
	private ConcurrentMap<Object, NetworkMarker> packetMarker = new MapMaker().weakKeys().makeMap();
	private ConcurrentMap<NetworkMarker, PacketEvent> markerEvent = new MapMaker().weakKeys().makeMap();
	
	// Packets we have processed before
	private Set<Object> processedPackets = Collections.newSetFromMap(new MapMaker().weakKeys().<Object, Boolean>makeMap());
	
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
	 * @param channelListener - a listener.
	 * @param factory - the factory that created this injector
	 */
	public ChannelInjector(Player player, Object networkManager, Channel channel, ChannelListener channelListener, InjectionFactory factory) {
		this.player =  Preconditions.checkNotNull(player, "player cannot be NULL");
		this.networkManager =  Preconditions.checkNotNull(networkManager, "networkMananger cannot be NULL");
		this.originalChannel = Preconditions.checkNotNull(channel, "channel cannot be NULL");
		this.channelListener = Preconditions.checkNotNull(channelListener, "channelListener cannot be NULL");
		this.factory = Preconditions.checkNotNull(factory, "factory cannot be NULL");
		
		// Get the channel field
		this.channelField = new VolatileField(
			FuzzyReflection.fromObject(networkManager, true).
				getFieldByType("channel", Channel.class), 
			networkManager, true);
	}
		
	@Override
	@SuppressWarnings("unchecked")
	public boolean inject() {
		synchronized (networkManager) {
			if (closed)
				return false;
			if (originalChannel instanceof Factory)
				return false;
			if (!originalChannel.isActive())
				return false;
			
			// Don't inject the same channel twice
			if (findChannelHandler(originalChannel, ChannelInjector.class) != null) {
				return false;
			}
			
			// Get the vanilla decoder, so we don't have to replicate the work
			vanillaDecoder = (ByteToMessageDecoder) originalChannel.pipeline().get("decoder");
			vanillaEncoder = (MessageToByteEncoder<Object>) originalChannel.pipeline().get("encoder");
			
			if (vanillaDecoder == null)
				throw new IllegalArgumentException("Unable to find vanilla decoder.in " + originalChannel.pipeline() );
			if (vanillaEncoder == null)
				throw new IllegalArgumentException("Unable to find vanilla encoder in " + originalChannel.pipeline() );
			patchEncoder(vanillaEncoder);
			
			if (DECODE_BUFFER == null)
				DECODE_BUFFER = Accessors.getMethodAccessor(vanillaDecoder.getClass(), 
					"decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
			if (ENCODE_BUFFER == null)
				ENCODE_BUFFER = Accessors.getMethodAccessor(vanillaEncoder.getClass(),
					"encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
			
			// Intercept sent packets
			protocolEncoder = new MessageToByteEncoder<Object>() {
				@Override
				protected void encode(ChannelHandlerContext ctx, Object packet, ByteBuf output) throws Exception {
					ChannelInjector.this.encode(ctx, packet, output);
				}
			};
			
			// Insert our handlers - note that we effectively replace the vanilla encoder/decoder 
			originalChannel.pipeline().addBefore("decoder", "protocol_lib_decoder", this);
			originalChannel.pipeline().addAfter("encoder", "protocol_lib_encoder", protocolEncoder);
			
			// Intercept all write methods
			channelField.setValue(new ChannelProxy(originalChannel, MinecraftReflection.getPacketClass()) {
				@Override
				protected Object onMessageScheduled(Object message) {
					Object result = processSending(message);
					
					// We have now processed this packet once already
					if (result != null) {
						processedPackets.add(result);
					}
					return result;
				}
			});
			
			injected = true;
			return true;
		}
	}
	
	/**
	 * Process a given message on the packet listeners.
	 * @param message - the message/packet.
	 * @return The resulting message/packet.
	 */
	private Object processSending(Object message) {
		return channelListener.onPacketSending(ChannelInjector.this, message, packetMarker.get(message));
	}
	
	/**
	 * This method patches the encoder so that it skips already created packets.
	 * @param encoder - the encoder to patch.
	 */
	private void patchEncoder(MessageToByteEncoder<Object> encoder) {
		if (ENCODER_TYPE_MATCHER == null) {
			ENCODER_TYPE_MATCHER = Accessors.getFieldAccessor(encoder.getClass(), "matcher", true);
		}
		ENCODER_TYPE_MATCHER.set(encoder, TypeParameterMatcher.get(MinecraftReflection.getPacketClass()));
	}
		
	/**
	 * Encode a packet to a byte buffer, taking over for the standard Minecraft encoder.
	 * @param ctx - the current context. 
	 * @param packet - the packet to encode to a byte array.
	 * @param output - the output byte array.
	 * @throws Exception If anything went wrong.
	 */
	protected void encode(ChannelHandlerContext ctx, Object packet, ByteBuf output) throws Exception {
		try {
			NetworkMarker marker = getMarker(packet);
			PacketEvent event = markerEvent.remove(marker);
			
			// Try again, in case this packet was sent directly in the event loop
			if (event == null && !processedPackets.remove(packet)) {
				Class<?> clazz = packet.getClass();
				
				// Schedule the transmission on the main thread instead
				if (channelListener.hasMainThreadListener(clazz)) {
					// Delay the packet
					scheduleMainThread(packet);
					packet = null;
					
				} else {
					packet = processSending(packet);
					marker = getMarker(packet);
					event = markerEvent.remove(marker);
				}
			}
			
			// Process output handler
			if (packet != null && event != null && NetworkMarker.hasOutputHandlers(marker)) {
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
			if (packet != null) {
				ENCODE_BUFFER.invoke(vanillaEncoder, ctx, packet, output);
			}
		}
	}

	private void scheduleMainThread(final Object packetCopy) {
		// Do not process this packet agai
		processedPackets.add(packetCopy);
		
		ProtocolLibrary.getExecutorSync().execute(new Runnable() {
			@Override
			public void run() {
				invokeSendPacket(packetCopy);
			}
		});
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuffer, List<Object> packets) throws Exception {
		byteBuffer.markReaderIndex();
		DECODE_BUFFER.invoke(vanillaDecoder, ctx, byteBuffer, packets);
		
		try {			
			if (packets.size() > 0) {
				Object input = packets.get(0);
				Class<?> packetClass = input.getClass();
				NetworkMarker marker = null;
				
				// Special case!
				handleLogin(packetClass, input);
				
				if (channelListener.includeBuffer(packetClass)) {
					byteBuffer.resetReaderIndex();
					marker = new NettyNetworkMarker(ConnectionSide.CLIENT_SIDE, getBytes(byteBuffer));
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
	
	/**
	 * Invoked when we may need to handle the login packet.
	 * @param packetClass - the packet class.
	 * @param packet - the packet.
	 */
	protected void handleLogin(Class<?> packetClass, Object packet) {
		// Initialize packet class
		if (PACKET_LOGIN_CLIENT == null) {
			PACKET_LOGIN_CLIENT = PacketType.Login.Client.START.getPacketClass();
			LOGIN_GAME_PROFILE = Accessors.getFieldAccessor(PACKET_LOGIN_CLIENT, GameProfile.class, true);
		}
			
		// See if we are dealing with the login packet
		if (PACKET_LOGIN_CLIENT.equals(packetClass)) {
			GameProfile profile = (GameProfile) LOGIN_GAME_PROFILE.get(packet);
			
			// Save the channel injector
			factory.cacheInjector(profile.getName(), this);
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

	@Override
	public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) {
		saveMarker(packet, marker);
		processedPackets.remove(packet);
		
		// Record if this packet should be ignored by most listeners
		if (!filtered) {
			ignoredPackets.add(packet);
		} else {
			ignoredPackets.remove(packet);
		}
		invokeSendPacket(packet);
	}
	
	/**
	 * Invoke the sendPacket method in Minecraft.
	 * @param packet - the packet to send.
 	 */
	private void invokeSendPacket(Object packet) {
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
	
	@Override
	public void recieveClientPacket(Object packet, NetworkMarker marker, boolean filtered) {
		saveMarker(packet, marker);
		processedPackets.remove(packet);
	
		if (!filtered) {
			ignoredPackets.add(packet);
		} else {
			ignoredPackets.remove(packet);
		}
		
		try {
			MinecraftMethods.getNetworkManagerReadPacketMethod().invoke(networkManager, null, packet);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to receive client packet " + packet, e);
		}
	}
	
	@Override
	public Protocol getCurrentProtocol() {
		if (PROTOCOL_ACCESSOR == null) {
			PROTOCOL_ACCESSOR = Accessors.getFieldAccessor(
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
	
	@Override
	public boolean unignorePacket(Object packet) {
		return ignoredPackets.remove(packet);
	}
	
	@Override
	public boolean ignorePacket(Object packet) {
		return ignoredPackets.add(packet);
	}
	
	@Override
	public NetworkMarker getMarker(Object packet) {
		return packetMarker.get(packet);
	}
	
	@Override
	public void saveMarker(Object packet, NetworkMarker marker) {
		if (marker != null) {
			packetMarker.put(packet, marker);
		}
	}
	
	@Override
	public void saveEvent(NetworkMarker marker, PacketEvent event) {
		if (marker != null) {
			markerEvent.put(marker, event);
		}
	}
			
	@Override
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Set the player instance.
	 * @param player - current instance.
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	/**
	 * Set the updated player instance.
	 * @param updated - updated instance.
	 */
	public void setUpdatedPlayer(Player updated) {
		this.updated = updated;
	}
	
	@Override
	public boolean isInjected() {
		return injected;
	}
	
	/**
	 * Determine if this channel has been closed and cleaned up.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	@Override
	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public void close() {
		if (!closed) {
			closed = true;
			
			if (injected) {
				channelField.revertValue();
				
				// Calling remove() in the main thread will block the main thread, which may lead 
				// to a deadlock:
				//    http://pastebin.com/L3SBVKzp
				// 
				// ProtocolLib executes this close() method through a PlayerQuitEvent in the main thread,
				// which has implicitly aquired a lock on SimplePluginManager (see SimplePluginManager.callEvent(Event)). 
				// Unfortunately, the remove() method will schedule the removal on one of the Netty worker threads if 
				// it's called from a different thread, blocking until the removal has been confirmed.
				// 
				// This is bad enough (Rule #1: Don't block the main thread), but the real trouble starts if the same 
				// worker thread happens to be handling a server ping connection when this removal task is scheduled. 
				// In that case, it may attempt to invoke an asynchronous ServerPingEvent (see PacketStatusListener) 
				// using SimplePluginManager.callEvent(). But, since this has already been locked by the main thread, 
				// we end up with a deadlock. The main thread is waiting for the worker thread to process the task, and 
			    // the worker thread is waiting for the main thread to finish executing PlayerQuitEvent.
				//
				// TLDR: Concurrenty is hard.
				originalChannel.eventLoop().submit(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						originalChannel.pipeline().remove(ChannelInjector.this);
						originalChannel.pipeline().remove(protocolEncoder);
						return null;
					}
				});
				// Clear cache
				factory.invalidate(player);
			}
		}
	}
	
	/**
	 * Find the first channel handler that is assignable to a given type.
	 * @param channel - the channel.
	 * @param clazz - the type.
	 * @return The first handler, or NULL.
	 */
	public static ChannelHandler findChannelHandler(Channel channel, Class<?> clazz) {
		for (Entry<String, ChannelHandler> entry : channel.pipeline()) {
			if (clazz.isAssignableFrom(entry.getValue().getClass())) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Represents a socket injector that foreards to the current channel injector.
	 * @author Kristian
	 */
	static class ChannelSocketInjector implements SocketInjector {
		private final ChannelInjector injector;
		
		public ChannelSocketInjector(ChannelInjector injector) {
			this.injector = Preconditions.checkNotNull(injector, "injector cannot be NULL");
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
			return injector.updated;
		}

		@Override
		public void transferState(SocketInjector delegate) {
			// Do nothing
		}

		@Override
		public void setUpdatedPlayer(Player updatedPlayer) {
			injector.player = updatedPlayer;
		}
		
		public ChannelInjector getChannelInjector() {
			return injector;
		}
	}
}
