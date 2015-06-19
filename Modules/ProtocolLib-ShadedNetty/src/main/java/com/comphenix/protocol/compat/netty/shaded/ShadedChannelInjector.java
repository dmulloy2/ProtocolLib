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
package com.comphenix.protocol.compat.netty.shaded;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelPipeline;
import net.minecraft.util.io.netty.channel.ChannelPromise;
import net.minecraft.util.io.netty.channel.socket.SocketChannel;
import net.minecraft.util.io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.util.io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.util.io.netty.util.internal.TypeParameterMatcher;
import net.sf.cglib.proxy.Factory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.compat.netty.ChannelInjector;
import com.comphenix.protocol.compat.netty.WrappedByteBuf;
import com.comphenix.protocol.compat.netty.WrappedChannel;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.NetworkProcessor;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.netty.NettyNetworkMarker;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;

/**
 * Represents a channel injector.
 * @author Kristian
 */
public class ShadedChannelInjector extends ByteToMessageDecoder implements ChannelInjector {
	public static final ReportType REPORT_CANNOT_INTERCEPT_SERVER_PACKET = new ReportType("Unable to intercept a written server packet.");
	public static final ReportType REPORT_CANNOT_INTERCEPT_CLIENT_PACKET = new ReportType("Unable to intercept a read client packet.");
	public static final ReportType REPORT_CANNOT_EXECUTE_IN_CHANNEL_THREAD = new ReportType("Cannot execute code in channel thread.");
	public static final ReportType REPORT_CANNOT_FIND_GET_VERSION = new ReportType("Cannot find getVersion() in NetworkMananger");
	public static final ReportType REPORT_CANNOT_SEND_PACKET = new ReportType("Unable to send packet %s to %s");

	/**
	 * Indicates that a packet has bypassed packet listeners.
	 */
	private static final PacketEvent BYPASSED_PACKET = new PacketEvent(ShadedChannelInjector.class);

	// The login packet
	private static Class<?> PACKET_LOGIN_CLIENT = null;
	private static FieldAccessor LOGIN_GAME_PROFILE = null;

	// Saved accessors
	private static MethodAccessor DECODE_BUFFER;
	private static MethodAccessor ENCODE_BUFFER;
	private static FieldAccessor ENCODER_TYPE_MATCHER;

	// For retrieving the protocol
	private static FieldAccessor PROTOCOL_ACCESSOR;

	// For retrieving the protocol version
	private static MethodAccessor PROTOCOL_VERSION;

	// The factory that created this injector
	private ShadedInjectionFactory factory;

	// The player, or temporary player
	private Player player;
	private Player updated;
	private String playerName;

	// The player connection
	private Object playerConnection;

	// The current network manager and channel
	private final Object networkManager;
	private final Channel originalChannel;
	private VolatileField channelField;

	// Known network markers
	private ConcurrentMap<Object, NetworkMarker> packetMarker = new MapMaker().weakKeys().makeMap();

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
	 * A flag set by the main thread to indiciate that a packet should not be processed.
	 */
	private final ThreadLocal<Boolean> scheduleProcessPackets = new ThreadLocal<Boolean>() {
		@Override
        protected Boolean initialValue() {
			return true;
		};
	};

	// Other handlers
	private ByteToMessageDecoder vanillaDecoder;
	private MessageToByteEncoder<Object> vanillaEncoder;

	private Deque<PacketEvent> finishQueue = new ArrayDeque<PacketEvent>();

	// The channel listener
	private ChannelListener channelListener;

	// Processing network markers
	private NetworkProcessor processor;

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
	public ShadedChannelInjector(Player player, Object networkManager, Channel channel, ChannelListener channelListener, ShadedInjectionFactory factory) {
		this.player =  Preconditions.checkNotNull(player, "player cannot be NULL");
		this.networkManager =  Preconditions.checkNotNull(networkManager, "networkMananger cannot be NULL");
		this.originalChannel = Preconditions.checkNotNull(channel, "channel cannot be NULL");
		this.channelListener = Preconditions.checkNotNull(channelListener, "channelListener cannot be NULL");
		this.factory = Preconditions.checkNotNull(factory, "factory cannot be NULL");
		this.processor = new NetworkProcessor(ProtocolLibrary.getErrorReporter());

		// Get the channel field
		this.channelField = new VolatileField(FuzzyReflection.fromObject(networkManager, true).getFieldByType("channel", Channel.class),
				networkManager, true);
	}

	/**
	 * Get the version of the current protocol.
	 * @return The version.
	 */
	@Override
	public int getProtocolVersion() {
		MethodAccessor accessor = PROTOCOL_VERSION;
		if (accessor == null) {
			try {
				accessor = Accessors.getMethodAccessor(networkManager.getClass(), "getVersion");
			} catch (Throwable ex) {
			}
		}

		if (accessor != null) {
			return (Integer) accessor.invoke(networkManager);
		} else {
			return MinecraftProtocolVersion.getCurrentVersion();
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
				executeInChannelThread(new Runnable() {
					@Override
					public void run() {
						inject();
					}
				});
				return false; // We don't know
			}

			// Don't inject the same channel twice
			if (findChannelHandler(originalChannel, ShadedChannelInjector.class) != null) {
				return false;
			}

			// Get the vanilla decoder, so we don't have to replicate the work
			vanillaDecoder = (ByteToMessageDecoder) originalChannel.pipeline().get("decoder");
			vanillaEncoder = (MessageToByteEncoder<Object>) originalChannel.pipeline().get("encoder");

			if (vanillaDecoder == null)
				throw new IllegalArgumentException("Unable to find vanilla decoder in " + originalChannel.pipeline() );
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
			MessageToByteEncoder<Object> protocolEncoder = new MessageToByteEncoder<Object>() {
				@Override
				protected void encode(ChannelHandlerContext ctx, Object packet, ByteBuf output) throws Exception {
					if (packet instanceof WirePacket) {
						// Special case for wire format
						ShadedChannelInjector.this.encodeWirePacket((WirePacket) packet, new ShadedByteBuf(output));
					} else {
						ShadedChannelInjector.this.encode(ctx, packet, output);
					}
				}

				@Override
				public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
					super.write(ctx, packet, promise);
					ShadedChannelInjector.this.finalWrite(ctx, packet, promise);
				}
			};

			// Intercept recieved packets
			ChannelInboundHandlerAdapter finishHandler = new ChannelInboundHandlerAdapter() {
				@Override
				public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					// Execute context first
					ctx.fireChannelRead(msg);
					ShadedChannelInjector.this.finishRead(ctx, msg);
				}
			};

			ChannelHandlerAdapter exceptionHandler = new ChannelHandlerAdapter() {
				@Override
				public void exceptionCaught(ChannelHandlerContext context, Throwable ex) throws Exception {
					if (ex instanceof ClosedChannelException) {
						// Ignore
					} else {
						// TODO Actually handle exceptions?
						System.err.println("[ProtocolLib] Encountered an uncaught exception in the channel pipeline:");
						ex.printStackTrace();
					}
				}
			};

			// Insert our handlers - note that we effectively replace the vanilla encoder/decoder
			originalChannel.pipeline().addBefore("decoder", "protocol_lib_decoder", this);
			originalChannel.pipeline().addBefore("protocol_lib_decoder", "protocol_lib_finish", finishHandler);
			originalChannel.pipeline().addAfter("encoder", "protocol_lib_encoder", protocolEncoder);
			originalChannel.pipeline().addLast("protocol_lib_exception_handler", exceptionHandler);

			// Intercept all write methods
			channelField.setValue(new ShadedChannelProxy(originalChannel, MinecraftReflection.getPacketClass()) {
				// Compatibility with Spigot 1.8
				private final ShadedPipelineProxy pipelineProxy = new ShadedPipelineProxy(originalChannel.pipeline(), this) {
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
					final PacketEvent event = handleScheduled(callable, packetAccessor);

					// Handle cancelled events
					if (event != null && event.isCancelled())
						return null;

					return new Callable<T>() {
						@Override
						public T call() throws Exception {
							T result = null;

							// This field must only be updated in the pipeline thread
							currentEvent = event;
							result = callable.call();
							currentEvent = null;
							return result;
						}
					};
				}

				@Override
				protected Runnable onMessageScheduled(final Runnable runnable, FieldAccessor packetAccessor) {
					final PacketEvent event = handleScheduled(runnable, packetAccessor);

					// Handle cancelled events
					if (event != null && event.isCancelled())
						return null;

					return new Runnable() {
						@Override
						public void run() {
							currentEvent = event;
							runnable.run();
							currentEvent = null;
						}
					};
				}

				protected PacketEvent handleScheduled(Object instance, FieldAccessor accessor) {
					// Let the filters handle this packet
					Object original = accessor.get(instance);

					// See if we've been instructed not to process packets
					if (!scheduleProcessPackets.get()) {
						NetworkMarker marker = getMarker(original);

						if (marker != null)	{
							PacketEvent result = new PacketEvent(ShadedChannelInjector.class);
							result.setNetworkMarker(marker);
							return result;
						} else {
							return BYPASSED_PACKET;
						}
					}
					PacketEvent event = processSending(original);

					if (event != null && !event.isCancelled()) {
						Object changed = event.getPacket().getHandle();

						// Change packet to be scheduled
						if (original != changed)
							accessor.set(instance, changed);
					};
					return event != null ? event : BYPASSED_PACKET;
				}
			});

			injected = true;
			return true;
		}
	}

	/**
	 * Determine if the given object is a compressor or decompressor.
	 * @param handler - object to test.
	 * @return TRUE if it is, FALSE if not or unknown.
	 */
	private boolean guessCompression(ChannelHandler handler) {
		String className = handler != null ? handler.getClass().getCanonicalName() : null;
		return className.contains("Compressor") || className.contains("Decompressor");
	}

	/**
	 * Process a given message on the packet listeners.
	 * @param message - the message/packet.
	 * @return The resulting message/packet.
	 */
	private PacketEvent processSending(Object message) {
		return channelListener.onPacketSending(ShadedChannelInjector.this, message, getMarker(message));
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
		if (channelListener.isDebug())
			cause.printStackTrace();
		super.exceptionCaught(ctx, cause);
	}

	protected void encodeWirePacket(WirePacket packet, WrappedByteBuf output) throws Exception {
		packet.writeId(output);
		packet.writeBytes(output);
	}

	/**
	 * Encode a packet to a byte buffer, taking over for the standard Minecraft encoder.
	 * @param ctx - the current context.
	 * @param packet - the packet to encode to a byte array.
	 * @param output - the output byte array.
	 * @throws Exception If anything went wrong.
	 */
	protected void encode(ChannelHandlerContext ctx, Object packet, ByteBuf output) throws Exception {
		NetworkMarker marker = null;
		PacketEvent event = currentEvent;

		try {
			// Skip every kind of non-filtered packet
			if (!scheduleProcessPackets.get()) {
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
				ENCODE_BUFFER.invoke(vanillaEncoder, ctx, packet, packetBuffer);

				// Let each handler prepare the actual output
				byte[] data = processor.processOutput(event, marker, getBytes(packetBuffer));

				// Write the result
				output.writeBytes(data);
				packet = null;

				// Sent listeners?
				finalEvent = event;
				return;
			}
		} catch (Exception e) {
			channelListener.getReporter().reportDetailed(this,
				Report.newBuilder(REPORT_CANNOT_INTERCEPT_SERVER_PACKET).callerParam(packet).error(e).build());
		} finally {
			// Attempt to handle the packet nevertheless
			if (packet != null) {
				ENCODE_BUFFER.invoke(vanillaEncoder, ctx, packet, output);
				finalEvent = event;
			}
		}
	}

	/**
	 * Invoked when a packet has been written to the channel.
	 * @param ctx - current context.
	 * @param packet - the packet that has been written.
	 * @param promise - a promise.
	 */
	protected void finalWrite(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) {
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
		Bukkit.getScheduler().scheduleSyncDelayedTask(factory.getPlugin(), new Runnable() {
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
			// Reset queue
			finishQueue.clear();

			for (ListIterator<Object> it = packets.listIterator(); it.hasNext(); ) {
				Object input = it.next();
				Class<?> packetClass = input.getClass();
				NetworkMarker marker = null;

				// Special case!
				handleLogin(packetClass, input);

				if (channelListener.includeBuffer(packetClass)) {
					byteBuffer.resetReaderIndex();
					marker = new NettyNetworkMarker(ConnectionSide.CLIENT_SIDE, getBytes(byteBuffer));
				}

				PacketEvent output = channelListener.onPacketReceiving(this, input, marker);

				// Handle packet changes
				if (output != null) {
					if (output.isCancelled()) {
						it.remove();
						continue;
					} else if (output.getPacket().getHandle() != input) {
						it.set(output.getPacket().getHandle());
					}

					finishQueue.addLast(output);
				}
			}
		} catch (Exception e) {
			channelListener.getReporter().reportDetailed(this,
					Report.newBuilder(REPORT_CANNOT_INTERCEPT_CLIENT_PACKET).callerParam(byteBuffer).error(e).build());
		}
	}

	/**
	 * Invoked after our decoder.
	 * @param ctx - current context.
	 * @param msg - the current packet.
	 */
	protected void finishRead(ChannelHandlerContext ctx, Object msg) {
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
	protected void handleLogin(Class<?> packetClass, Object packet) {
		Class<?> loginClass = PACKET_LOGIN_CLIENT;
		FieldAccessor loginClient = LOGIN_GAME_PROFILE;

		// Initialize packet class and login
		if (loginClass == null) {
			loginClass = PacketType.Login.Client.START.getPacketClass();
			PACKET_LOGIN_CLIENT = loginClass;
		}
		if (loginClient == null) {
			loginClient = Accessors.getFieldAccessor(PACKET_LOGIN_CLIENT, MinecraftReflection.getGameProfileClass(), true);
			LOGIN_GAME_PROFILE = loginClient;
		}

		// See if we are dealing with the login packet
		if (loginClass.equals(packetClass)) {
			// GameProfile profile = (GameProfile) loginClient.get(packet);
			WrappedGameProfile profile = WrappedGameProfile.fromHandle(loginClient.get(packet));

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

		try {
			scheduleProcessPackets.set(filtered);
			invokeSendPacket(packet);
		} finally {
			scheduleProcessPackets.set(true);
		}
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
		} catch (Throwable ex) {
			ProtocolLibrary.getErrorReporter().reportWarning(factory.getPlugin(),
					Report.newBuilder(REPORT_CANNOT_SEND_PACKET).messageParam(packet, playerName).error(ex).build());
		}
	}

	@Override
	public void recieveClientPacket(final Object packet) {
		// TODO: Ensure the packet listeners are executed in the channel thread.

		// Execute this in the channel thread
		Runnable action = new Runnable() {
			@Override
			public void run() {
				try {
					MinecraftMethods.getNetworkManagerReadPacketMethod().invoke(networkManager, null, packet);
				} catch (Exception e) {
					// Inform the user
					ProtocolLibrary.getErrorReporter().reportMinimal(factory.getPlugin(), "recieveClientPacket", e);
				}
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
			return Bukkit.getPlayer(playerName);
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
				// TLDR: Concurrency is hard.
				executeInChannelThread(new Runnable() {
					@Override
					public void run() {
						String[] handlers = new String[] {
								"protocol_lib_decoder", "protocol_lib_finish", "protocol_lib_encoder", "protocol_lib_exception_handler"
						};

						for (String handler : handlers) {
							try {
								originalChannel.pipeline().remove(handler);
							} catch (NoSuchElementException e) {
								// Ignore
							}
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
		originalChannel.eventLoop().execute(new Runnable() {
			@Override
			public void run() {
				try {
					command.run();
				} catch (Exception e) {
					ProtocolLibrary.getErrorReporter().reportDetailed(ShadedChannelInjector.this,
						Report.newBuilder(REPORT_CANNOT_EXECUTE_IN_CHANNEL_THREAD).error(e).build());
				}
			}
		});
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
	public static class ChannelSocketInjector implements SocketInjector {
		private final ShadedChannelInjector injector;

		public ChannelSocketInjector(ShadedChannelInjector injector) {
			this.injector = Preconditions.checkNotNull(injector, "injector cannot be NULL");
		}

		@Override
		public Socket getSocket() throws IllegalAccessException {
			return ShadedSocketAdapter.adapt((SocketChannel) injector.originalChannel);
		}

		@Override
		public SocketAddress getAddress() throws IllegalAccessException {
			return injector.originalChannel.remoteAddress();
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
			return injector.getPlayer();
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
			injector.setPlayer(updatedPlayer);
		}

		public ShadedChannelInjector getChannelInjector() {
			return injector;
		}
	}

	@Override
	public WrappedChannel getChannel() {
		return new ShadedChannel(originalChannel);
	}
}