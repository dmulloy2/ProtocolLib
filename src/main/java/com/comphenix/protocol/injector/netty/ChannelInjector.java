/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package com.comphenix.protocol.injector.netty;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.NetworkProcessor;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.ObjectReconstructor;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.internal.TypeParameterMatcher;
import net.sf.cglib.proxy.Factory;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a channel injector.
 * @author Kristian
 */
public class ChannelInjector extends ByteToMessageDecoder implements Injector {
	private static final ReportType REPORT_CANNOT_INTERCEPT_SERVER_PACKET = new ReportType("Unable to intercept a written server packet.");
	private static final ReportType REPORT_CANNOT_INTERCEPT_CLIENT_PACKET = new ReportType("Unable to intercept a read client packet.");
	private static final ReportType REPORT_CANNOT_EXECUTE_IN_CHANNEL_THREAD = new ReportType("Cannot execute code in channel thread.");
	private static final ReportType REPORT_CANNOT_SEND_PACKET = new ReportType("Unable to send packet %s to %s");

	/**
	 * Indicates that a packet has bypassed packet listeners.
	 */
	private static final PacketEvent BYPASSED_PACKET = new PacketEvent(ChannelInjector.class);


	private static final Map<Class<?>, ObjectReconstructor<?>> RECONSTRUCTORS = new ConcurrentHashMap<>();

	// Determine the method of updating packets.
	// Starting in Java 15 (59), the Runnables/Callables are hidden classes and we cannot use reflection to update
	// the values anymore. Instead, the object will have to be reconstructed.
	private static final PacketMessageUpdater PACKET_MESSAGE_UPDATER =
			Float.parseFloat(System.getProperty("java.class.version")) >= 59 ?
					ChannelInjector::updatePacketMessageReconstruct :
					ChannelInjector::updatePacketMessageSetReflection;

	// The login packet
	private static Class<?> PACKET_LOGIN_CLIENT = null;
	private static FieldAccessor LOGIN_GAME_PROFILE = null;

	// Versioning
	private static Class<?> PACKET_SET_PROTOCOL = null;

	private static AtomicInteger keyId = new AtomicInteger();
	private static AttributeKey<Integer> PROTOCOL_KEY;

	static {
		try {
			PROTOCOL_KEY = AttributeKey.valueOf("PROTOCOL-" + keyId.getAndIncrement());
		} catch (Exception ex) {
			throw new RuntimeException("Encountered an error caused by a reload! Please properly restart your server!", ex);
		}
	}

	// Saved accessors
	private Method decodeBuffer;
	private Method encodeBuffer;
	private static FieldAccessor ENCODER_TYPE_MATCHER;

	// For retrieving the protocol
	private static FieldAccessor PROTOCOL_ACCESSOR;

	// The factory that created this injector
	private final InjectionFactory factory;

	// The player, or temporary player
	private Player player;
	private Player updated;
	private String playerName;

	// The player connection
	private Object playerConnection;

	// The current network manager and channel
	private final Object networkManager;
	private final Channel originalChannel;
	private final VolatileField channelField;

	// Known network markers
	private final Map<Object, NetworkMarker> packetMarker = new WeakHashMap<>();

	/**
	 * Indicate that this packet has been processed by event listeners.
	 * <p>
	 * This must never be set outside the channel pipeline's thread.
	 */
	private PacketEvent currentEvent;

	/**
	 * A packet event that should be processed by the write method.
	 */
	private PacketEvent finalEvent;

	/**
	 * A queue of packets that were sent with filtered=false
	 */
	private final PacketFilterQueue unfilteredProcessedPackets = new PacketFilterQueue();

	// Other handlers
	private ByteToMessageDecoder vanillaDecoder;
	private MessageToByteEncoder<Object> vanillaEncoder;

	private final Deque<PacketEvent> finishQueue = new ArrayDeque<>();

	// The channel listener
	private final ChannelListener channelListener;

	// Processing network markers
	private final NetworkProcessor processor;

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
	ChannelInjector(Player player, Object networkManager, Channel channel, ChannelListener channelListener, InjectionFactory factory) {
		this.player =  Preconditions.checkNotNull(player, "player cannot be NULL");
		this.networkManager =  Preconditions.checkNotNull(networkManager, "networkMananger cannot be NULL");
		this.originalChannel = Preconditions.checkNotNull(channel, "channel cannot be NULL");
		this.channelListener = Preconditions.checkNotNull(channelListener, "channelListener cannot be NULL");
		this.factory = Preconditions.checkNotNull(factory, "factory cannot be NULL");
		this.processor = new NetworkProcessor(ProtocolLibrary.getErrorReporter());

		// Get the channel field
		this.channelField = new VolatileField(FuzzyReflection
				.fromObject(networkManager, true)
				.getFieldByType("channel", Channel.class), networkManager, true);
	}

	/**
	 * Get the version of the current protocol.
	 * @return The version.
	 */
	@Override
	public int getProtocolVersion() {
		Integer value = originalChannel.attr(PROTOCOL_KEY).get();
		return value != null ? value : MinecraftProtocolVersion.getCurrentVersion();
	}

	private void updateBufferMethods() {
		try {
			decodeBuffer = vanillaDecoder.getClass().getDeclaredMethod("decode",
					ChannelHandlerContext.class, ByteBuf.class, List.class);
			decodeBuffer.setAccessible(true);
		} catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException("Unable to find decode method in " + vanillaDecoder.getClass());
		}

		try {
			encodeBuffer = vanillaEncoder.getClass().getDeclaredMethod("encode",
					ChannelHandlerContext.class, Object.class, ByteBuf.class);
			encodeBuffer.setAccessible(true);
		} catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException("Unable to find encode method in " + vanillaEncoder.getClass());
		}
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

			// Main thread? We should synchronize with the channel thread, otherwise we might see a
			// pipeline with only some of the handlers removed
			if (Bukkit.isPrimaryThread()) {
				// Just like in the close() method, we'll avoid blocking the main thread
				executeInChannelThread(this::inject);
				return false; // We don't know
			}

			// Don't inject the same channel twice
			if (findChannelHandler(originalChannel, ChannelInjector.class) != null) {
				return false;
			}

			// Get the vanilla decoder, so we don't have to replicate the work
			vanillaDecoder = (ByteToMessageDecoder) originalChannel.pipeline().get("decoder");
			vanillaEncoder = (MessageToByteEncoder<Object>) originalChannel.pipeline().get("encoder");

			if (vanillaDecoder == null)
				throw new IllegalArgumentException("Unable to find vanilla decoder in " + originalChannel.pipeline());
			if (vanillaEncoder == null)
				throw new IllegalArgumentException("Unable to find vanilla encoder in " + originalChannel.pipeline());
			patchEncoder(vanillaEncoder);

			updateBufferMethods();

			// Intercept sent packets
			MessageToByteEncoder<Object> protocolEncoder = new MessageToByteEncoder<Object>() {
				@Override
				protected void encode(ChannelHandlerContext ctx, Object packet, ByteBuf output) throws Exception {
					if (packet instanceof WirePacket) {
						// Special case for wire format
						ChannelInjector.this.encodeWirePacket((WirePacket) packet, output);
					} else {
						ChannelInjector.this.encode(ctx, packet, output);
					}
				}

				@Override
				public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
					super.write(ctx, packet, promise);
					ChannelInjector.this.finalWrite();
				}
			};

			// Intercept recieved packets
			ChannelInboundHandlerAdapter finishHandler = new ChannelInboundHandlerAdapter() {
				@Override
				public void channelRead(ChannelHandlerContext ctx, Object msg) {
					// Execute context first
					ctx.fireChannelRead(msg);
					ChannelInjector.this.finishRead();
				}
			};

			// Insert our handlers - note that we effectively replace the vanilla encoder/decoder
			originalChannel.pipeline().addBefore("decoder", "protocol_lib_decoder", this);
			originalChannel.pipeline().addBefore("protocol_lib_decoder", "protocol_lib_finish", finishHandler);
			originalChannel.pipeline().addAfter("encoder", "protocol_lib_encoder", protocolEncoder);

			// Intercept all write methods
			channelField.setValue(new ChannelProxy(originalChannel, MinecraftReflection.getPacketClass()) {
				// Compatibility with Spigot 1.8
				private final PipelineProxy pipelineProxy = new PipelineProxy(originalChannel.pipeline(), this) {
					@Override
					public ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler) {
						// Correct the position of the decoder
						if ("decoder".equals(baseName)) {
							if (super.get("protocol_lib_decoder") != null && guessCompression(handler)) {
								super.addBefore("protocol_lib_decoder", name, handler);
								return this;
							}
						}

						return super.addBefore(baseName, name, handler);
					}
				};

				@Override
				public ChannelPipeline pipeline() {
					return pipelineProxy;
				}

				@Override
				protected <T> Callable<T> onMessageScheduled(final Callable<T> callable, FieldAccessor packetAccessor) {
					Pair<Callable<T>, PacketEvent> handled = handleScheduled(callable, packetAccessor);

					// Handle cancelled events
					if (handled.getSecond() != null && handled.getSecond().isCancelled())
						return null;

					return () -> {
						T result;

						// This field must only be updated in the pipeline thread
						currentEvent = handled.getSecond();
						result = handled.getFirst().call();
						currentEvent = null;
						return result;
					};
				}

				@Override
				protected Runnable onMessageScheduled(final Runnable runnable, FieldAccessor packetAccessor) {
					Pair<Runnable, PacketEvent> handled = handleScheduled(runnable, packetAccessor);

					// Handle cancelled events
					if (handled.getSecond() != null && handled.getSecond().isCancelled())
						return null;

					return () -> {
						currentEvent = handled.getSecond();
						handled.getFirst().run();
						currentEvent = null;
					};
				}

				<T> Pair<T, PacketEvent> handleScheduled(T instance, FieldAccessor accessor) {
					// Let the filters handle this packet
					Object original = accessor.get(instance);

					// See if we've been instructed not to process packets
					if (unfilteredProcessedPackets.contains(original)) {
						NetworkMarker marker = getMarker(original);

						if (marker != null)	{
							PacketEvent result = new PacketEvent(ChannelInjector.class);
							result.setNetworkMarker(marker);
							return new Pair<>(instance, result);
						} else {
							return new Pair<>(instance, BYPASSED_PACKET);
						}
					}

					PacketEvent event = processSending(original);
					if (event != null && !event.isCancelled()) {
						Object changed = event.getPacket().getHandle();

						// Change packet to be scheduled
						if (original != changed)
							instance = (T) PACKET_MESSAGE_UPDATER.update(instance, changed, accessor);
					}
					return new Pair<>(instance, event != null ? event : BYPASSED_PACKET);
				}
			});

			injected = true;
			return true;
		}
	}

	/**
	 * Changes the packet in a packet message using a {@link FieldAccessor}.
	 * @see PacketMessageUpdater
	 */
	private static Object updatePacketMessageSetReflection(Object instance, Object newPacket, FieldAccessor accessor) {
		accessor.set(instance, newPacket);
		return instance;
	}

	/**
	 * Changes the packet in a packet message using a {@link ObjectReconstructor}.
	 * @see PacketMessageUpdater
	 */
	private static Object updatePacketMessageReconstruct(Object instance, Object newPacket, FieldAccessor accessor) {
		final ObjectReconstructor<?> objectReconstructor =
				RECONSTRUCTORS.computeIfAbsent(instance.getClass(), ObjectReconstructor::new);

		final Object[] values = objectReconstructor.getValues(instance);
		final Field[] fields = objectReconstructor.getFields();
		for (int idx = 0; idx < fields.length; ++idx)
			if (fields[idx].equals(accessor.getField()))
				values[idx] = newPacket;

		return objectReconstructor.reconstruct(values);
	}

	/**
	 * Determine if the given object is a compressor or decompressor.
	 * @param handler - object to test.
	 * @return TRUE if it is, FALSE if not or unknown.
	 */
	private boolean guessCompression(ChannelHandler handler) {
		String className = handler != null ? handler.getClass().getCanonicalName() : "";
		return className.contains("Compressor") || className.contains("Decompressor");
	}

	/**
	 * Process a given message on the packet listeners.
	 * @param message - the message/packet.
	 * @return The resulting message/packet.
	 */
	private PacketEvent processSending(Object message) {
		return channelListener.onPacketSending(ChannelInjector.this, message, getMarker(message));
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

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (channelListener.isDebug()) {
			cause.printStackTrace();
		}

		super.exceptionCaught(ctx, cause);
	}

	private void encodeWirePacket(WirePacket packet, ByteBuf output) {
		packet.writeId(output);
		packet.writeBytes(output);
	}

	/**
	 * Encode a packet to a byte buffer, taking over for the standard Minecraft encoder.
	 * @param ctx - the current context.
	 * @param packet - the packet to encode to a byte array.
	 * @param output - the output byte array.
	 */
	private void encode(ChannelHandlerContext ctx, Object packet, ByteBuf output) throws Exception {
		NetworkMarker marker = null;
		PacketEvent event = currentEvent;

		try {
			// Skip every kind of non-filtered packet
			if (unfilteredProcessedPackets.remove(packet)) {
				return;
			}

			// This packet has not been seen by the main thread
			if (event == null) {
				Class<?> clazz = packet.getClass();

				// Schedule the transmission on the main thread instead
				if (channelListener.hasMainThreadListener(clazz)) {
					// Delay the packet
					scheduleMainThread(packet);
					packet = null;
				} else {
					event = processSending(packet);

					// Handle the output
					if (event != null) {
						packet = !event.isCancelled() ? event.getPacket().getHandle() : null;
					}
				}
			}

			if (event != null) {
				// Retrieve marker without accidentally constructing it
				marker = NetworkMarker.getNetworkMarker(event);
			}

			// Process output handler
			if (packet != null && event != null && NetworkMarker.hasOutputHandlers(marker)) {
				ByteBuf packetBuffer = ctx.alloc().buffer();
				encodeBuffer.invoke(vanillaEncoder, ctx, packet, packetBuffer);

				// Let each handler prepare the actual output
				byte[] data = processor.processOutput(event, marker, getBytes(packetBuffer));

				// Write the result
				output.writeBytes(data);
				packet = null;

				// Sent listeners?
				finalEvent = event;
			}
		} catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof Exception) {
				throw (Exception) ex.getCause();
			}
		} catch (Exception e) {
			channelListener.getReporter().reportDetailed(this,
					Report.newBuilder(REPORT_CANNOT_INTERCEPT_SERVER_PACKET).callerParam(packet).error(e).build());
		} finally {
			// Attempt to handle the packet nevertheless
			if (packet != null) {
				try {
					encodeBuffer.invoke(vanillaEncoder, ctx, packet, output);
				} catch (InvocationTargetException ex) {
					if (ex.getCause() instanceof Exception) {
						//noinspection ThrowFromFinallyBlock
						throw (Exception) ex.getCause();
					}
				}

				finalEvent = event;
			}
		}
	}

	/**
	 * Invoked when a packet has been written to the channel
	 */
	private void finalWrite() {
		PacketEvent event = finalEvent;

		if (event != null) {
			// Necessary to prevent infinite loops
			finalEvent = null;
			currentEvent = null;

			processor.invokePostEvent(event, NetworkMarker.getNetworkMarker(event));
		}
	}

	private void scheduleMainThread(final Object packetCopy) {
		// Don't use BukkitExecutors for this - it has a bit of overhead
		Bukkit.getScheduler().scheduleSyncDelayedTask(factory.getPlugin(), () -> {
			if (!closed) {
				invokeSendPacket(packetCopy);
			}
		});
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuffer, List<Object> packets) throws Exception {
		try {
			try {
				decodeBuffer.invoke(vanillaDecoder, ctx, byteBuffer, packets);
			} catch (IllegalArgumentException ex) {
				updateBufferMethods();
				decodeBuffer.invoke(vanillaDecoder, ctx, byteBuffer, packets);
			}

			// Reset queue
			finishQueue.clear();

			for (ListIterator<Object> it = packets.listIterator(); it.hasNext(); ) {
				Object input = it.next();
				Class<?> packetClass = input.getClass();
				NetworkMarker marker = null;

				// Special case!
				handleLogin(packetClass, input);

				if (channelListener.includeBuffer(packetClass)) {
					if (byteBuffer.readableBytes() != 0) {
						byteBuffer.resetReaderIndex();
						marker = new NettyNetworkMarker(ConnectionSide.CLIENT_SIDE, getBytes(byteBuffer));
					}
				}

				PacketEvent output = channelListener.onPacketReceiving(this, input, marker);

				// Handle packet changes
				if (output != null) {
					if (output.isCancelled()) {
						it.remove();
					} else {
						if (output.getPacket().getHandle() != input) {
							it.set(output.getPacket().getHandle());
						}

						finishQueue.addLast(output);
					}
				}
			}
		} catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof Exception) {
				throw (Exception) ex.getCause();
			}
		} catch (Exception e) {
			channelListener.getReporter().reportDetailed(this,
					Report.newBuilder(REPORT_CANNOT_INTERCEPT_CLIENT_PACKET).callerParam(byteBuffer).error(e).build());
		}
	}

	/**
	 * Invoked after our decoder
	 */
	private void finishRead() {
		// Assume same order
		PacketEvent event = finishQueue.pollFirst();

		if (event != null) {
			NetworkMarker marker = NetworkMarker.getNetworkMarker(event);

			if (marker != null) {
				processor.invokePostEvent(event, marker);
			}
		}
	}

	/**
	 * Invoked when we may need to handle the login packet.
	 * @param packetClass - the packet class.
	 * @param packet - the packet.
	 */
	private void handleLogin(Class<?> packetClass, Object packet) {
		// Try to find the login packet class
		if (PACKET_LOGIN_CLIENT == null) {
			PACKET_LOGIN_CLIENT = PacketType.Login.Client.START.getPacketClass();
		}

		// If we can't, there's an issue
		if (PACKET_LOGIN_CLIENT == null) {
			throw new IllegalStateException("Failed to obtain login start packet. Did you build Spigot with BuildTools?");
		}

		if (LOGIN_GAME_PROFILE == null) {
			LOGIN_GAME_PROFILE = Accessors.getFieldAccessor(PACKET_LOGIN_CLIENT, MinecraftReflection.getGameProfileClass(), true);
		}

		// See if we are dealing with the login packet
		if (PACKET_LOGIN_CLIENT.equals(packetClass)) {
			WrappedGameProfile profile = WrappedGameProfile.fromHandle(LOGIN_GAME_PROFILE.get(packet));

			// Save the channel injector
			factory.cacheInjector(profile.getName(), this);
		}

		if (PACKET_SET_PROTOCOL == null) {
			try {
				PACKET_SET_PROTOCOL = PacketType.Handshake.Client.SET_PROTOCOL.getPacketClass();
			} catch (Throwable ex) {
				PACKET_SET_PROTOCOL = getClass(); // If we can't find it don't worry about it
			}
		}

		if (PACKET_SET_PROTOCOL.equals(packetClass)) {
			FuzzyReflection fuzzy = FuzzyReflection.fromObject(packet);
			try {
				int protocol = (int) fuzzy.invokeMethod(packet, "getProtocol", int.class);
				originalChannel.attr(PROTOCOL_KEY).set(protocol);
			} catch (Throwable ex) {
				// Oh well
			}
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
	private byte[] getBytes(ByteBuf buffer) {
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

		if (!filtered) {
			unfilteredProcessedPackets.add(packet);
		}

		invokeSendPacket(packet);
	}

	/**
	 * Invoke the sendPacket method in Minecraft.
	 * @param packet - the packet to send.
 	 */
	private void invokeSendPacket(Object packet) {
		Validate.isTrue(!closed, "cannot send packets to a closed channel");

		// Attempt to send the packet with NetworkMarker.handle(), or the PlayerConnection if its active
		try {
			if (player instanceof Factory) {
				MinecraftMethods.getNetworkManagerHandleMethod().invoke(networkManager, packet);
			} else {
				MinecraftMethods.getSendPacketMethod().invoke(getPlayerConnection(), packet);
			}
		} catch (Throwable ex) {
			ProtocolLibrary.getErrorReporter().reportWarning(this,
					Report.newBuilder(REPORT_CANNOT_SEND_PACKET).messageParam(packet, playerName).error(ex).build());
		}
	}

	@Override
	public void recieveClientPacket(final Object packet) {
		// TODO: Ensure the packet listeners are executed in the channel thread.

		// Execute this in the channel thread
		Runnable action = () -> {
			try {
				MinecraftMethods.getNetworkManagerReadPacketMethod().invoke(networkManager, null, packet);
			} catch (Exception e) {
				// Inform the user
				ProtocolLibrary.getErrorReporter().reportMinimal(factory.getPlugin(), "recieveClientPacket", e);
			}
		};

		// Execute in the worker thread
		if (originalChannel.eventLoop().inEventLoop()) {
			action.run();
		} else {
			originalChannel.eventLoop().execute(action);
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
			Player player = getPlayer();
			if (player == null) {
				throw new IllegalStateException("cannot send packet to offline player" + (playerName != null ?  " " + playerName : ""));
			}

			playerConnection = MinecraftFields.getPlayerConnection(player);
		}

		return playerConnection;
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
	public Player getPlayer() {
		if (player == null && playerName != null) {
			return Bukkit.getPlayerExact(playerName);
		}

		return player;
	}

	/**
	 * Set the player instance.
	 * @param player - current instance.
	 */
	@Override
    public void setPlayer(Player player) {
		this.player = player;
		this.playerName = player.getName();
	}

	/**
	 * Set the updated player instance.
	 * @param updated - updated instance.
	 */
	@Override
    public void setUpdatedPlayer(Player updated) {
		this.updated = updated;
		this.playerName = updated.getName();
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
				// TL;DR: Concurrency is hard.
				executeInChannelThread(() -> {
					String[] handlers = new String[] {
							"protocol_lib_decoder", "protocol_lib_finish", "protocol_lib_encoder"
					};

					for (String handler : handlers) {
						try {
							originalChannel.pipeline().remove(handler);
						} catch (NoSuchElementException e) {
							// Ignore
						}
					}
				});

				// Clear cache
				factory.invalidate(player);

				// Clear player instances
				// Should help fix memory leaks
				this.player = null;
				this.updated = null;
			}
		}
	}

	/**
	 * Execute a specific command in the channel thread.
	 * <p>
	 * Exceptions are printed through the standard error reporter mechanism.
	 * @param command - the command to execute.
	 */
	private void executeInChannelThread(final Runnable command) {
		originalChannel.eventLoop().execute(() -> {
			try {
				command.run();
			} catch (Exception e) {
				ProtocolLibrary.getErrorReporter().reportDetailed(ChannelInjector.this,
						Report.newBuilder(REPORT_CANNOT_EXECUTE_IN_CHANNEL_THREAD).error(e).build());
			}
		});
	}

	/**
	 * Find the first channel handler that is assignable to a given type.
	 * @param channel - the channel.
	 * @param clazz - the type.
	 * @return The first handler, or NULL.
	 */
	static ChannelHandler findChannelHandler(Channel channel, Class<?> clazz) {
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
	public static class ChannelSocketInjector implements SocketInjector {
		private final ChannelInjector injector;

		ChannelSocketInjector(ChannelInjector injector) {
			this.injector = Preconditions.checkNotNull(injector, "injector cannot be NULL");
		}

		@Override
		public Socket getSocket() {
			return SocketAdapter.adapt((SocketChannel) injector.originalChannel);
		}

		@Override
		public SocketAddress getAddress() {
			return injector.originalChannel.remoteAddress();
		}

		@Override
		public void disconnect(String message) {
			injector.disconnect(message);
		}

		@Override
		public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) {
			injector.sendServerPacket(packet, marker, filtered);
		}

		@Override
		public Player getPlayer() {
			return injector.getPlayer();
		}

		@Override
		public Player getUpdatedPlayer() {
			return injector.updated != null ? injector.updated : getPlayer();
		}

		@Override
		public void transferState(SocketInjector delegate) {
			// Do nothing
		}

		@Override
		public void setUpdatedPlayer(Player updatedPlayer) {
			injector.setPlayer(updatedPlayer);
		}

		ChannelInjector getChannelInjector() {
			return injector;
		}
	}

	public Channel getChannel() {
		return originalChannel;
	}

	/**
	 * Represents a method of updating a packet in a scheduled packet message.
	 */
	@FunctionalInterface
	private interface PacketMessageUpdater {
		/**
		 * Updates a packet in a scheduled packet message.
		 * @param instance  The current packet message to update.
		 * @param newPacket The new packet to put in the packet message.
		 * @param accessor  The FieldAccessor for the packet field in the packet message.
		 * @return The updated packet message.
		 */
		Object update(Object instance, Object newPacket, FieldAccessor accessor);
	}
}
