package com.comphenix.protocol.wrappers;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.entity.Player;

import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.io.netty.handler.codec.base64.Base64;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * Represents a server ping packet data.
 * @author Kristian
 */
public class WrappedServerPing extends AbstractWrapper {
	// Server ping fields
	private static Class<?> SERVER_PING = MinecraftReflection.getServerPingClass();
	private static ConstructorAccessor SERVER_PING_CONSTRUCTOR = Accessors.getConstructorAccessor(SERVER_PING);
	private static FieldAccessor DESCRIPTION = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getIChatBaseComponentClass(), true);
	private static FieldAccessor PLAYERS = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getServerPingPlayerSampleClass(), true);
	private static FieldAccessor VERSION = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getServerPingServerDataClass(), true);
	private static FieldAccessor FAVICON = Accessors.getFieldAccessor(SERVER_PING, String.class, true);
	
	// For converting to the underlying array
	private static EquivalentConverter<Iterable<? extends WrappedGameProfile>> PROFILE_CONVERT = 
		BukkitConverters.getArrayConverter(GameProfile[].class, BukkitConverters.getWrappedGameProfileConverter());
	
	// Server ping player sample fields
	private static Class<?> PLAYERS_CLASS = MinecraftReflection.getServerPingPlayerSampleClass();
	private static ConstructorAccessor PLAYERS_CONSTRUCTOR = Accessors.getConstructorAccessor(PLAYERS_CLASS, int.class, int.class);
	private static FieldAccessor[] PLAYERS_INTS = Accessors.getFieldAccessorArray(PLAYERS_CLASS, int.class, true);
	private static FieldAccessor PLAYERS_PROFILES = Accessors.getFieldAccessor(PLAYERS_CLASS, GameProfile[].class, true);
	private static FieldAccessor PLAYERS_MAXIMUM = PLAYERS_INTS[0];
	private static FieldAccessor PLAYERS_ONLINE = PLAYERS_INTS[1];
	
	// Server data fields
	private static Class<?> VERSION_CLASS = MinecraftReflection.getServerPingServerDataClass();
	private static ConstructorAccessor VERSION_CONSTRUCTOR = Accessors.getConstructorAccessor(VERSION_CLASS, String.class, int.class);
	private static FieldAccessor VERSION_NAME = Accessors.getFieldAccessor(VERSION_CLASS, String.class, true);
	private static FieldAccessor VERSION_PROTOCOL = Accessors.getFieldAccessor(VERSION_CLASS, int.class, true);
	
	// Get profile from player
	private static FieldAccessor ENTITY_HUMAN_PROFILE = Accessors.getFieldAccessor(
			MinecraftReflection.getEntityPlayerClass().getSuperclass(), GameProfile.class, true);
	
	// Inner class
	private Object players;
	private Object version;
	
	/**
	 * Construct a new server ping initialized with empty values.
	 */
	public WrappedServerPing() {
		super(MinecraftReflection.getServerPingClass());
		setHandle(SERVER_PING_CONSTRUCTOR.invoke());
		this.players = PLAYERS_CONSTRUCTOR.invoke(0, 0);
		this.version = VERSION_CONSTRUCTOR.invoke(MinecraftVersion.WORLD_UPDATE.toString(), 4);
		PLAYERS.set(handle, players);
		VERSION.set(handle, version);
	}
	
	private WrappedServerPing(Object handle) {
		super(MinecraftReflection.getServerPingClass());
		setHandle(handle);
		this.players = PLAYERS.get(handle);
		this.version = VERSION.get(handle);
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
	 * <p>
	 * <b>Warning:</b> Only the first line will be transmitted.
	 * @param description - the message.
	 */
	public void setMotD(String message) {
		setMotD(WrappedChatComponent.fromChatMessage(message)[0]);
	}
	
	/**
	 * Retrieve the compressed PNG file that is being displayed as a favicon.
	 * @return The favicon.
	 */
	public CompressedImage getFavicon() {
		return CompressedImage.fromEncodedText((String) FAVICON.get(handle));
	}
	
	/**
	 * Set the compressed PNG file that is being displayed.
	 * @param image - the new compressed image.
	 */
	public void setFavicon(CompressedImage image) {
		FAVICON.set(handle, image.toEncodedText());
	}
	
	/**
	 * Retrieve the displayed number of online players.
	 * @return The displayed number.
	 */
	public int getPlayersOnline() {
		return (Integer) PLAYERS_ONLINE.get(players);
	}
	
	/**
	 * Set the displayed number of online players.
	 * @param online - online players.
	 */
	public void setPlayersOnline(int online) {
		PLAYERS_ONLINE.set(players, online);
	}
	
	/**
	 * Retrieve the displayed maximum number of players.
	 * @return The maximum number.
	 */
	public int getPlayersMaximum() {
		return (Integer) PLAYERS_MAXIMUM.get(players);
	}
	
	/**
	 * Set the displayed maximum number of players.
	 * @param maximum - maximum player count.
	 */
	public void setPlayersMaximum(int maximum) {
		PLAYERS_MAXIMUM.set(players, maximum);
	}
	
	/**
	 * Retrieve a copy of all the logged in players.
	 * @return Logged in players.
	 */
	public ImmutableList<WrappedGameProfile> getPlayers() {
		return ImmutableList.copyOf(PROFILE_CONVERT.getSpecific(PLAYERS_PROFILES.get(players)));
	}
	
	/**
	 * Set the displayed list of logged in players.
	 * @param profile - every logged in player.
	 */
	public void setPlayers(Iterable<? extends WrappedGameProfile> profile) {
		PLAYERS_PROFILES.set(handle, PROFILE_CONVERT.getGeneric(GameProfile[].class, profile));
	}
	
	/**
	 * Set the displayed lst of logged in players.
	 * @param players - the players to display.
	 */
	public void setBukkitPlayers(Iterable<? extends Player> players) {
		List<WrappedGameProfile> profiles = Lists.newArrayList();
		
		for (Player player : players) {
			GameProfile profile = (GameProfile) ENTITY_HUMAN_PROFILE.get(BukkitUnwrapper.getInstance().unwrapItem(player));
			profiles.add(new WrappedGameProfile(profile.getId(), profile.getName()));
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
	 * Represents a compressed favicon.
	 * @author Kristian
	 */
	public static class CompressedImage {
		private final String mime;
		private final byte[] data;
		
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
		 * Retrieve a compressed image from an image.
		 * @param image - the image.
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
			String mime = null;
			byte[] data = null;
			
			for (String segment : Splitter.on(";").split(text)) {
				if (segment.startsWith("data:")) {
					mime = segment.substring(5);
				} else if (segment.startsWith("base64,")) {
					byte[] encoded = segment.substring(7).getBytes(Charsets.UTF_8);
					ByteBuf decoded = Base64.decode(Unpooled.wrappedBuffer(encoded));
					
					// Read into a byte array
					data = new byte[decoded.readableBytes()];
					decoded.readBytes(data);
				} else {
					// We will ignore these segments
				}
			}
			return new CompressedImage(mime, data);
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
			return data.clone();
		}
		
		/**
		 * Uncompress and return the stored image.
		 * @return The image.
		 * @throws IOException If the image data could not be decoded.
		 */
		public BufferedImage getImage() throws IOException {
			return ImageIO.read(new ByteArrayInputStream(data));
		}

		/**
		 * Convert the compressed image to encoded text.
		 * @return The encoded text.
		 */
		public String toEncodedText() {
			return "data:" + mime + ";base64," + Base64.encode(Unpooled.wrappedBuffer(data)).toString(Charsets.UTF_8);
		}
	}
}
