package com.comphenix.protocol.wrappers;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.Util;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;

/**
 * Represents a server ping packet data.
 * @author Kristian
 */
public class WrappedServerPing extends AbstractWrapper implements ClonableWrapper {
	private static Class<?> GAME_PROFILE = MinecraftReflection.getGameProfileClass();
	private static Class<?> GAME_PROFILE_ARRAY = MinecraftReflection.getArrayClass(GAME_PROFILE);

	// Server ping fields
	private static Class<?> SERVER_PING = MinecraftReflection.getServerPingClass();
	private static ConstructorAccessor SERVER_PING_CONSTRUCTOR = Accessors.getConstructorAccessor(SERVER_PING);
	private static FieldAccessor DESCRIPTION = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getIChatBaseComponentClass(), true);
	private static FieldAccessor PLAYERS = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getServerPingPlayerSampleClass(), true);
	private static FieldAccessor VERSION = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getServerPingServerDataClass(), true);
	private static FieldAccessor FAVICON = Accessors.getFieldAccessor(SERVER_PING, String.class, true);
	private static FieldAccessor PREVIEWS_CHAT;

	// For converting to the underlying array
	private static EquivalentConverter<Iterable<? extends WrappedGameProfile>> PROFILE_CONVERT =
		BukkitConverters.getArrayConverter(GAME_PROFILE, BukkitConverters.getWrappedGameProfileConverter());

	// Server ping player sample fields
	private static Class<?> PLAYERS_CLASS = MinecraftReflection.getServerPingPlayerSampleClass();
	private static ConstructorAccessor PLAYERS_CONSTRUCTOR = Accessors.getConstructorAccessor(PLAYERS_CLASS, int.class, int.class);
	private static FieldAccessor[] PLAYERS_INTS = Accessors.getFieldAccessorArray(PLAYERS_CLASS, int.class, true);
	private static FieldAccessor PLAYERS_PROFILES = Accessors.getFieldAccessor(PLAYERS_CLASS, GAME_PROFILE_ARRAY, true);
	private static FieldAccessor PLAYERS_MAXIMUM = PLAYERS_INTS[0];
	private static FieldAccessor PLAYERS_ONLINE = PLAYERS_INTS[1];

	// Server ping serialization
	private static Class<?> GSON_CLASS = MinecraftReflection.getMinecraftGsonClass();
	private static MethodAccessor GSON_TO_JSON = Accessors.getMethodAccessor(GSON_CLASS, "toJson", Object.class);
	private static MethodAccessor GSON_FROM_JSON = Accessors.getMethodAccessor(GSON_CLASS, "fromJson", String.class, Class.class);
	private static FieldAccessor PING_GSON = Accessors.getCached(Accessors.getFieldAccessor(
		PacketType.Status.Server.SERVER_INFO.getPacketClass(), GSON_CLASS, true
	));

	// Server data fields
	private static Class<?> VERSION_CLASS = MinecraftReflection.getServerPingServerDataClass();
	private static ConstructorAccessor VERSION_CONSTRUCTOR = Accessors.getConstructorAccessor(VERSION_CLASS, String.class, int.class);
	private static FieldAccessor VERSION_NAME = Accessors.getFieldAccessor(VERSION_CLASS, String.class, true);
	private static FieldAccessor VERSION_PROTOCOL = Accessors.getFieldAccessor(VERSION_CLASS, int.class, true);

	// Get profile from player
	private static FieldAccessor ENTITY_HUMAN_PROFILE = Accessors.getFieldAccessor(
			MinecraftReflection.getEntityPlayerClass().getSuperclass(), GAME_PROFILE, true);

	// Inner class
	private Object players; // may be NULL
	private Object version;

	/**
	 * Construct a new server ping initialized with a zero player count, and zero maximum.
	 * <p>
	 * Note that the version string is set to 1.9.4.
	 */
	public WrappedServerPing() {
		super(MinecraftReflection.getServerPingClass());
		setHandle(SERVER_PING_CONSTRUCTOR.invoke());
		resetPlayers();
		resetVersion();
	}

	private WrappedServerPing(Object handle) {
		super(MinecraftReflection.getServerPingClass());
		setHandle(handle);
		this.players = PLAYERS.get(handle);
		this.version = VERSION.get(handle);
	}

	/**
	 * Set the player count and player maximum to the default values.
	 */
	protected void resetPlayers() {
		players = PLAYERS_CONSTRUCTOR.invoke(0, 0);
		PLAYERS.set(handle, players);
	}

	/**
	 * Reset the version string to the default state.
	 */
	protected void resetVersion() {
		MinecraftVersion minecraftVersion = MinecraftVersion.getCurrentVersion();
		version = VERSION_CONSTRUCTOR.invoke(minecraftVersion.toString(), MinecraftProtocolVersion.getCurrentVersion());
		VERSION.set(handle, version);
	}

	/**
	 * Construct a wrapped server ping from a native NMS object.
	 * @param handle - the native object.
	 * @return The wrapped server ping object.
	 */
	public static WrappedServerPing fromHandle(Object handle) {
		return new WrappedServerPing(handle);
	}

	/**
	 * Construct a wrapper server ping from an encoded JSON string.
	 * @param json - the JSON string.
	 * @return The wrapped server ping.
	 */
	public static WrappedServerPing fromJson(String json) {
		return fromHandle(GSON_FROM_JSON.invoke(PING_GSON.get(null), json, SERVER_PING));
	}

	/**
	 * Retrieve the message of the day.
	 * @return The messge of the day.
	 */
	public WrappedChatComponent getMotD() {
		return WrappedChatComponent.fromHandle(DESCRIPTION.get(handle));
	}

	/**
	 * Set the message of the day.
	 * @param description - message of the day.
	 */
	public void setMotD(WrappedChatComponent description) {
		DESCRIPTION.set(handle, description.getHandle());
	}

	/**
	 * Set the message of the day.
	 * @param message - the message.
	 */
	public void setMotD(String message) {
		setMotD(WrappedChatComponent.fromLegacyText(message));
	}

	/**
	 * Retrieve the compressed PNG file that is being displayed as a favicon.
	 * @return The favicon, or NULL if no favicon will be displayed.
	 */
	public CompressedImage getFavicon() {
		String favicon = (String) FAVICON.get(handle);
		return (favicon != null) ? CompressedImage.fromEncodedText(favicon) : null;
	}

	/**
	 * Set the compressed PNG file that is being displayed.
	 * @param image - the new compressed image or NULL if no favicon should be displayed.
	 */
	public void setFavicon(CompressedImage image) {
		FAVICON.set(handle, (image != null) ? image.toEncodedText() : null);
	}

	/**
	 * Retrieve whether chat preview is enabled on the server.
	 * @return whether chat preview is enabled on the server.
	 * @since 1.19
	 */
	public boolean isChatPreviewEnabled() {
		if (PREVIEWS_CHAT == null) {
			// TODO: at some point we should make everything nullable to make updates easier
			// see https://github.com/dmulloy2/ProtocolLib/issues/1644 for an example reference
			PREVIEWS_CHAT = Accessors.getFieldAccessor(SERVER_PING, boolean.class, true);
		}
		return (Boolean) PREVIEWS_CHAT.get(handle);
	}

	/**
	 * Sets whether chat preview is enabled on the server.
	 * @param chatPreviewEnabled true if enabled, false otherwise.
	 * @since 1.19
	 */
	public void setChatPreviewEnabled(boolean chatPreviewEnabled) {
		if (PREVIEWS_CHAT == null) {
			PREVIEWS_CHAT = Accessors.getFieldAccessor(SERVER_PING, boolean.class, true);
		}
		PREVIEWS_CHAT.set(handle, chatPreviewEnabled);
	}

	/**
	 * Retrieve the displayed number of online players.
	 * @return The displayed number.
	 * @throws IllegalStateException If the player count has been hidden via {@link #setPlayersVisible(boolean)}.
	 * @see #setPlayersOnline(int)
	 */
	public int getPlayersOnline() {
		if (players == null)
			throw new IllegalStateException("The player count has been hidden.");
		return (Integer) PLAYERS_ONLINE.get(players);
	}

	/**
	 * Set the displayed number of online players.
	 * <p>
	 * As of 1.7.2, this is completely unrestricted, and can be both positive and
	 * negative, as well as higher than the player maximum.
	 * @param online - online players.
	 */
	public void setPlayersOnline(int online) {
		if (players == null)
			resetPlayers();
		PLAYERS_ONLINE.set(players, online);
	}

	/**
	 * Retrieve the displayed maximum number of players.
	 * @return The maximum number.
	 * @throws IllegalStateException If the player maximum has been hidden via {@link #setPlayersVisible(boolean)}.
	 * @see #setPlayersMaximum(int)
	 */
	public int getPlayersMaximum() {
		if (players == null)
			throw new IllegalStateException("The player maximum has been hidden.");
		return (Integer) PLAYERS_MAXIMUM.get(players);
	}

	/**
	 * Set the displayed maximum number of players.
	 * <p>
	 * The 1.7.2 accepts any value as a player maximum, positive or negative. It even permits a player maximum that
	 * is less than the player count.
	 * @param maximum - maximum player count.
	 */
	public void setPlayersMaximum(int maximum) {
		if (players == null)
			resetPlayers();
		PLAYERS_MAXIMUM.set(players, maximum);
	}

	/**
	 * Set whether or not the player count and player maximum is visible.
	 * <p>
	 * Note that this may set the current player count and maximum to their respective real values.
	 * @param visible - TRUE if it should be visible, FALSE otherwise.
	 */
	public void setPlayersVisible(boolean visible) {
		if (isPlayersVisible() != visible) {
			if (visible) {
				// Recreate the count and maximum
				Server server = Bukkit.getServer();
				setPlayersMaximum(server.getMaxPlayers());
				setPlayersOnline(Util.getOnlinePlayers().size());
			} else {
				PLAYERS.set(handle, players = null);
			}
		}
	}

	/**
	 * Determine if the player count and maximum is visible.
	 * <p>
	 * If not, the client will display ??? in the same location.
	 * @return TRUE if the player statistics is visible, FALSE otherwise.
	 */
	public boolean isPlayersVisible() {
		return players != null;
	}

	/**
	 * Retrieve a copy of all the logged in players.
	 * @return Logged in players or an empty list if no player names will be displayed.
	 */
	public ImmutableList<WrappedGameProfile> getPlayers() {
		if (players == null)
			return ImmutableList.of();
		Object playerProfiles = PLAYERS_PROFILES.get(players);
		if (playerProfiles == null)
			return ImmutableList.of();
		return ImmutableList.copyOf(PROFILE_CONVERT.getSpecific(playerProfiles));
	}

	/**
	 * Set the displayed list of logged in players.
	 * @param profile - every logged in player.
	 */
	public void setPlayers(Iterable<? extends WrappedGameProfile> profile) {
		if (players == null)
			resetPlayers();
		PLAYERS_PROFILES.set(players, (profile != null) ? PROFILE_CONVERT.getGeneric(profile) : null);
	}

	/**
	 * Set the displayed lst of logged in players.
	 * @param players - the players to display.
	 */
	public void setBukkitPlayers(Iterable<? extends Player> players) {
		final List<WrappedGameProfile> profiles = new ArrayList<>();

		for (Player player : players) {
			Object profile = ENTITY_HUMAN_PROFILE.get(BukkitUnwrapper.getInstance().unwrapItem(player));
			profiles.add(WrappedGameProfile.fromHandle(profile));
		}

		setPlayers(profiles);
	}

	/**
	 * Retrieve the version name of the current server.
	 * @return The version name.
	 */
	public String getVersionName() {
		return (String) VERSION_NAME.get(version);
	}

	/**
	 * Set the version name of the current server.
	 * @param name - the new version name.
	 */
	public void setVersionName(String name) {
		VERSION_NAME.set(version, name);
	}

	/**
	 * Retrieve the protocol number.
	 * @return The protocol.
	 */
	public int getVersionProtocol() {
		return (Integer) VERSION_PROTOCOL.get(version);
	}

	/**
	 * Set the version protocol
	 * @param protocol - the protocol number.
	 */
	public void setVersionProtocol(int protocol) {
		VERSION_PROTOCOL.set(version, protocol);
	}

	/**
	 * Retrieve a deep copy of the current wrapper object.
	 * @return The current object.
	 */
	public WrappedServerPing deepClone() {
		WrappedServerPing copy = new WrappedServerPing();
		WrappedChatComponent motd = getMotD();

		copy.setPlayers(getPlayers());
		copy.setFavicon(getFavicon());
		copy.setMotD(motd != null ? motd.deepClone() : null);
		copy.setVersionName(getVersionName());
		copy.setVersionProtocol(getVersionProtocol());

		if (isPlayersVisible()) {
			copy.setPlayersMaximum(getPlayersMaximum());
			copy.setPlayersOnline(getPlayersOnline());
		} else {
			copy.setPlayersVisible(false);
		}
		return copy;
	}

	/**
	 * Retrieve the underlying JSON representation of this server ping.
	 * @return The JSON representation.
	 */
	public String toJson() {
		return (String) GSON_TO_JSON.invoke(PING_GSON.get(null), handle);
	}

	@Override
	public String toString() {
		return "WrappedServerPing< " + toJson() + ">";
	}

	/**
	 * Represents a compressed favicon.
	 * @author Kristian
	 */
	// Should not have been an inner class ... oh well.
	public static class CompressedImage {
		protected volatile String mime;
		protected volatile byte[] data;
		protected volatile String encoded;

		/**
		 * Represents a compressed image with no content.
		 */
		protected CompressedImage() {
			// Derived class should initialize some of the fields
		}

		/**
		 * Construct a new compressed image.
		 * @param mime - the mime type.
		 * @param data - the raw compressed image data.
		 */
		public CompressedImage(String mime, byte[] data) {
			this.mime = Preconditions.checkNotNull(mime, "mime cannot be NULL");
			this.data = Preconditions.checkNotNull(data, "data cannot be NULL");
		}

		/**
		 * Retrieve a compressed image from an input stream.
		 * @param input - the PNG as an input stream.
		 * @return The compressed image.
		 * @throws IOException If we cannot read the input stream.
		 */
		public static CompressedImage fromPng(InputStream input) throws IOException {
			return new CompressedImage("image/png", ByteStreams.toByteArray(input));
		}

		/**
		 * Retrieve a compressed image from a byte array of a PNG file.
		 * @param data - the file as a byte array.
		 * @return The compressed image.
		 */
		public static CompressedImage fromPng(byte[] data) {
			return new CompressedImage("image/png", data);
		}

		/**
		 * Retrieve a compressed image from a base-64 encoded PNG file.
		 * @param base64 - the base 64-encoded PNG.
		 * @return The compressed image.
		 */
		public static CompressedImage fromBase64Png(String base64) {
			try {
				return new EncodedCompressedImage("data:image/png;base64," + base64);
			} catch (IllegalArgumentException e) {
				// Remind the caller
				throw new IllegalArgumentException("Must be a pure base64 encoded string. Cannot be an encoded text.", e);
			}
		}

		/**
		 * Retrieve a compressed image from an image.
		 * @param image - the image.
		 * @return A compressed image from an image.
		 * @throws IOException If we were unable to compress the image.
		 */
		public static CompressedImage fromPng(RenderedImage image) throws IOException {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(image, "png", output);
			return new CompressedImage("image/png", output.toByteArray());
		}

		/**
		 * Retrieve a compressed image from an encoded text.
		 * @param text - the encoded text.
		 * @return The corresponding compressed image.
		 */
		public static CompressedImage fromEncodedText(String text) {
			return new EncodedCompressedImage(text);
		}

		/**
		 * Retrieve the MIME type of the image.
		 * <p>
		 * This is image/png in vanilla Minecraft.
		 * @return The MIME type.
		 */
		public String getMime() {
			return mime;
		}

		/**
		 * Retrieve a copy of the underlying data array.
		 * @return The underlying compressed image.
		 */
		public byte[] getDataCopy() {
			return getData().clone();
		}

		/**
		 * Retrieve the underlying data, with no copying.
		 * @return The underlying data.
		 */
		protected byte[] getData() {
			return data;
		}

		/**
		 * Uncompress and return the stored image.
		 * @return The image.
		 * @throws IOException If the image data could not be decoded.
		 */
		public BufferedImage getImage() throws IOException {
			return ImageIO.read(new ByteArrayInputStream(getData()));
		}

		/**
		 * Convert the compressed image to encoded text.
		 * @return The encoded text.
		 */
		public String toEncodedText() {
			if (encoded == null) {
				final ByteBuf buffer = Unpooled.wrappedBuffer(getDataCopy());
				encoded = "data:" + getMime() + ";base64," +
						Base64.encode(buffer).toString(Charsets.UTF_8);
			}

			return encoded;
		}
	}

	/**
	 * Represents a compressed image that starts out as an encoded base 64 string.
	 * @author Kristian
	 */
	private static class EncodedCompressedImage extends CompressedImage {
		public EncodedCompressedImage(String encoded) {
			this.encoded =  Preconditions.checkNotNull(encoded, "encoded favicon cannot be NULL");
		}

		/**
		 * Ensure that we have decoded the content of the encoded text.
		 */
		protected void initialize() {
			if (mime == null || data == null) {
				decode();
			}
		}

		/**
		 * Decode the encoded text.
		 */
		protected void decode() {
			for (String segment : Splitter.on(";").split(encoded)) {
				if (segment.startsWith("data:")) {
					this.mime = segment.substring(5);
				} else if (segment.startsWith("base64,")) {
					byte[] encoded = segment.substring(7).getBytes(Charsets.UTF_8);
					ByteBuf decoded = Base64.decode(Unpooled.wrappedBuffer(encoded));

					// Read into a byte array
					byte[] data = new byte[decoded.readableBytes()];
					decoded.readBytes(data);
					this.data = data;
				} else {
					// We will ignore these segments
				}
			}
		}

		@Override
		protected byte[] getData() {
			initialize();
			return super.getData();
		}

		@Override
		public String getMime() {
			initialize();
			return super.getMime();
		}

		@Override
		public String toEncodedText() {
			return encoded;
		}
	}
}
