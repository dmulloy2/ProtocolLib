package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.ping.LegacyServerPing;
import com.comphenix.protocol.wrappers.ping.ServerPingImpl;
import com.comphenix.protocol.wrappers.ping.ServerPingRecord;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a server ping packet data.
 * @author Kristian
 */
public class WrappedServerPing implements ClonableWrapper {
    private static final Class<?> GAME_PROFILE = MinecraftReflection.getGameProfileClass();

    // Get profile from player
    private static final FieldAccessor ENTITY_HUMAN_PROFILE = Accessors.getFieldAccessor(
        MinecraftReflection.getEntityPlayerClass().getSuperclass(), GAME_PROFILE, true);

    private final ServerPingImpl impl;

    /**
     * Construct a new server ping initialized with a zero player count, and zero maximum.
     * <p>
     * Note that the version string is set to 1.9.4.
     */
    public WrappedServerPing() {
        this.impl = newImpl();
    }

    private WrappedServerPing(Object handle) {
        this.impl = newImpl(handle);
    }

    private static ServerPingImpl newImpl() {
        if (MinecraftVersion.FEATURE_PREVIEW_2.atOrAbove()) {
            return new ServerPingRecord();
        }

        return new LegacyServerPing();
    }

    private static ServerPingImpl newImpl(Object handle) {
        if (MinecraftVersion.FEATURE_PREVIEW_2.atOrAbove()) {
            return new ServerPingRecord(handle);
        }

        return new LegacyServerPing(handle);
    }

    /**
     * Set the player count and player maximum to the default values.
     */
    private void resetPlayers() {
        impl.resetPlayers();
    }

    /**
     * Reset the version string to the default state.
     */
    private void resetVersion() {
        impl.resetVersion();
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
        if(MinecraftVersion.FEATURE_PREVIEW_2.atOrAbove()) {
            return new WrappedServerPing(ServerPingRecord.fromJson(json).getHandle());
        }
        return new WrappedServerPing(LegacyServerPing.fromJson(json));
    }

    /**
     * Retrieve the message of the day.
     * @return The message of the day.
     */
    public WrappedChatComponent getMotD() {
        return impl.getMotD();
    }

    /**
     * Set the message of the day.
     * @param description - message of the day.
     */
    public void setMotD(WrappedChatComponent description) {
        impl.setMotD(description);
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
        return impl.getFavicon();
    }

    /**
     * Set the compressed PNG file that is being displayed.
     * @param image - the new compressed image or NULL if no favicon should be displayed.
     */
    public void setFavicon(CompressedImage image) {
        impl.setFavicon(image);
    }

    /**
     * Retrieve whether chat preview is enabled on the server.
     * @return whether chat preview is enabled on the server.
     * @since 1.19
     * @deprecated Removed in 1.19.3
     */
    @Deprecated
    public boolean isChatPreviewEnabled() {
        return impl.isChatPreviewEnabled();
    }

    /**
     * Sets whether chat preview is enabled on the server.
     * @param chatPreviewEnabled true if enabled, false otherwise.
     * @since 1.19
     * @deprecated Removed in 1.19.3
     */
    @Deprecated
    public void setChatPreviewEnabled(boolean chatPreviewEnabled) {
        impl.setChatPreviewEnabled(chatPreviewEnabled);
    }

    /**
     * Sets whether the server enforces secure chat.
     * @return whether the server enforces secure chat.
     * @since 1.19.1
     */
    public boolean isEnforceSecureChat() {
        return impl.isEnforceSecureChat();
    }

    /**
     * Sets whether the server enforces secure chat.
     * @param enforceSecureChat true if enabled, false otherwise.
     * @since 1.19.1
     */
    public void setEnforceSecureChat(boolean enforceSecureChat) {
        impl.setEnforceSecureChat(enforceSecureChat);
    }

    /**
     * Retrieve the displayed number of online players.
     * @return The displayed number.
     * @throws IllegalStateException If the player count has been hidden via {@link #setPlayersVisible(boolean)}.
     * @see #setPlayersOnline(int)
     */
    public int getPlayersOnline() {
        return impl.getPlayersOnline();
    }

    /**
     * Set the displayed number of online players.
     * <p>
     * As of 1.7.2, this is completely unrestricted, and can be both positive and
     * negative, as well as higher than the player maximum.
     * @param online - online players.
     */
    public void setPlayersOnline(int online) {
        impl.setPlayersOnline(online);
    }

    /**
     * Retrieve the displayed maximum number of players.
     * @return The maximum number.
     * @throws IllegalStateException If the player maximum has been hidden via {@link #setPlayersVisible(boolean)}.
     * @see #setPlayersMaximum(int)
     */
    public int getPlayersMaximum() {
        return impl.getPlayersMaximum();
    }

    /**
     * Set the displayed maximum number of players.
     * <p>
     * The 1.7.2 accepts any value as a player maximum, positive or negative. It even permits a player maximum that
     * is less than the player count.
     * @param maximum - maximum player count.
     */
    public void setPlayersMaximum(int maximum) {
        impl.setPlayersMaximum(maximum);
    }

    /**
     * Set whether or not the player count and player maximum is visible.
     * <p>
     * Note that this may set the current player count and maximum to their respective real values.
     * @param visible - TRUE if it should be visible, FALSE otherwise.
     */
    public void setPlayersVisible(boolean visible) {
        if (isPlayersVisible() != visible) {
            impl.setPlayersVisible(visible);
        }
    }

    /**
     * Determine if the player count and maximum is visible.
     * <p>
     * If not, the client will display ??? in the same location.
     * @return TRUE if the player statistics is visible, FALSE otherwise.
     */
    public boolean isPlayersVisible() {
        return impl.arePlayersVisible();
    }

    /**
     * Retrieve a copy of all the logged in players.
     * @return Logged in players or an empty list if no player names will be displayed.
     */
    public ImmutableList<WrappedGameProfile> getPlayers() {
        return impl.getPlayers();
    }

    /**
     * Set the displayed list of logged in players.
     * @param profile - every logged in player.
     */
    public void setPlayers(Iterable<? extends WrappedGameProfile> profile) {
        if (!isPlayersVisible())
            resetPlayers();
        impl.setPlayers(profile);
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
        return impl.getVersionName();
    }

    /**
     * Set the version name of the current server.
     * @param name - the new version name.
     */
    public void setVersionName(String name) {
        impl.setVersionName(name);
    }

    /**
     * Retrieve the protocol number.
     * @return The protocol.
     */
    public int getVersionProtocol() {
        return impl.getVersionProtocol();
    }

    /**
     * Set the version protocol
     * @param protocol - the protocol number.
     */
    public void setVersionProtocol(int protocol) {
        impl.setVersionProtocol(protocol);
    }

    @Override
    public Object getHandle() {
        return impl.getHandle();
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
        return impl.getJson();
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
                        Base64.encode(buffer).toString(StandardCharsets.UTF_8);
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
                    byte[] encoded = segment.substring(7).getBytes(StandardCharsets.UTF_8);
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

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof WrappedServerPing)) {
            return false;
        }
        return getHandle().equals(((WrappedServerPing) obj).getHandle());
    }

    @Override
    public int hashCode() {
        return getHandle().hashCode();
    }
}
