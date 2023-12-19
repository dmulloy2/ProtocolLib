package com.comphenix.protocol.wrappers.ping;

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
import com.comphenix.protocol.wrappers.AbstractWrapper;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a server ping packet data.
 * @author Kristian
 */
public final class LegacyServerPing extends AbstractWrapper implements ServerPingImpl {
    private static final Class<?> GAME_PROFILE = MinecraftReflection.getGameProfileClass();

    // For converting to the underlying array
    private static final EquivalentConverter<Iterable<? extends WrappedGameProfile>> PROFILE_CONVERT =
        BukkitConverters.getArrayConverter(GAME_PROFILE, BukkitConverters.getWrappedGameProfileConverter());

    // Get profile from player
    private static final FieldAccessor ENTITY_HUMAN_PROFILE = Accessors.getFieldAccessor(
        MinecraftReflection.getEntityPlayerClass().getSuperclass(), GAME_PROFILE, true);

    private static final Class<?> GAME_PROFILE_ARRAY = MinecraftReflection.getArrayClass(GAME_PROFILE);

    // Server ping fields
    private static final Class<?> SERVER_PING = MinecraftReflection.getServerPingClass();
    private static final ConstructorAccessor SERVER_PING_CONSTRUCTOR = Accessors.getConstructorAccessor(SERVER_PING);
    private static final FieldAccessor DESCRIPTION = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getIChatBaseComponentClass(), true);
    private static final FieldAccessor PLAYERS = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getServerPingPlayerSampleClass(), true);
    private static final FieldAccessor VERSION = Accessors.getFieldAccessor(SERVER_PING, MinecraftReflection.getServerPingServerDataClass(), true);
    private static final FieldAccessor FAVICON = Accessors.getFieldAccessor(SERVER_PING, String.class, true);
    private static final FieldAccessor[] BOOLEAN_ACCESSORS = Accessors.getFieldAccessorArray(SERVER_PING, boolean.class, true);

    // Server ping player sample fields
    private static final Class<?> PLAYERS_CLASS = MinecraftReflection.getServerPingPlayerSampleClass();
    private static final ConstructorAccessor PLAYERS_CONSTRUCTOR = Accessors.getConstructorAccessor(PLAYERS_CLASS, int.class, int.class);
    private static final FieldAccessor[] PLAYERS_INTS = Accessors.getFieldAccessorArray(PLAYERS_CLASS, int.class, true);
    private static final FieldAccessor PLAYERS_PROFILES = Accessors.getFieldAccessor(PLAYERS_CLASS, GAME_PROFILE_ARRAY, true);
    private static final FieldAccessor PLAYERS_MAXIMUM = PLAYERS_INTS[0];
    private static final FieldAccessor PLAYERS_ONLINE = PLAYERS_INTS[1];

    // Server ping serialization
    private static final Class<?> GSON_CLASS = MinecraftReflection.getMinecraftGsonClass();
    private static final MethodAccessor GSON_TO_JSON = Accessors.getMethodAccessor(GSON_CLASS, "toJson", Object.class);
    private static final MethodAccessor GSON_FROM_JSON = Accessors.getMethodAccessor(GSON_CLASS, "fromJson", String.class, Class.class);
    private static final FieldAccessor PING_GSON = Accessors.getMemorizing(Accessors.getFieldAccessor(
    PacketType.Status.Server.SERVER_INFO.getPacketClass(), GSON_CLASS, true
    ));

    // Server data fields
    private static final Class<?> VERSION_CLASS = MinecraftReflection.getServerPingServerDataClass();
    private static final ConstructorAccessor VERSION_CONSTRUCTOR = Accessors.getConstructorAccessor(VERSION_CLASS, String.class, int.class);
    private static final FieldAccessor VERSION_NAME = Accessors.getFieldAccessor(VERSION_CLASS, String.class, true);
    private static final FieldAccessor VERSION_PROTOCOL = Accessors.getFieldAccessor(VERSION_CLASS, int.class, true);


    // Inner class
    private Object players; // may be NULL
    private Object version;

    /**
     * Construct a new server ping initialized with a zero player count, and zero maximum.
     * <p>
     * Note that the version string is set to 1.9.4.
     */
    public LegacyServerPing() {
        super(MinecraftReflection.getServerPingClass());
        setHandle(SERVER_PING_CONSTRUCTOR.invoke());
        resetPlayers();
        resetVersion();
    }

    public LegacyServerPing(Object handle) {
        super(MinecraftReflection.getServerPingClass());
        setHandle(handle);
        this.players = PLAYERS.get(handle);
        this.version = VERSION.get(handle);
    }

    /**
     * Set the player count and player maximum to the default values.
     */
    public void resetPlayers() {
        players = PLAYERS_CONSTRUCTOR.invoke(0, 0);
        PLAYERS.set(handle, players);
    }

    /**
     * Reset the version string to the default state.
     */
    public void resetVersion() {
        MinecraftVersion minecraftVersion = MinecraftVersion.getCurrentVersion();
        version = VERSION_CONSTRUCTOR.invoke(minecraftVersion.toString(), MinecraftProtocolVersion.getCurrentVersion());
        VERSION.set(handle, version);
    }

    /**
     * Construct a wrapped server ping from a native NMS object.
     * @param handle - the native object.
     * @return The wrapped server ping object.
     */
    public static LegacyServerPing fromHandle(Object handle) {
        return new LegacyServerPing(handle);
    }

    /**
     * Construct a wrapper server ping from an encoded JSON string.
     * @param json - the JSON string.
     * @return The wrapped server ping.
     */
    public static LegacyServerPing fromJson(String json) {
        return fromHandle(GSON_FROM_JSON.invoke(PING_GSON.get(null), json, SERVER_PING));
    }

    /**
     * Retrieve the message of the day.
     * @return The message of the day.
     */
    @Override
    public WrappedChatComponent getMotD() {
        return WrappedChatComponent.fromHandle(DESCRIPTION.get(handle));
    }

    /**
     * Set the message of the day.
     * @param description - message of the day.
     */
    @Override
    public void setMotD(WrappedChatComponent description) {
        DESCRIPTION.set(handle, description != null ? description.getHandle() : null);
    }

    /**
     * Retrieve the compressed PNG file that is being displayed as a favicon.
     * @return The favicon, or NULL if no favicon will be displayed.
     */
    @Override
    public WrappedServerPing.CompressedImage getFavicon() {

        String favicon = (String) FAVICON.get(handle);
        return (favicon != null) ? WrappedServerPing.CompressedImage.fromEncodedText(favicon) : null;
    }

    /**
     * Set the compressed PNG file that is being displayed.
     * @param image - the new compressed image or NULL if no favicon should be displayed.
     */
    @Override
    public void setFavicon(WrappedServerPing.CompressedImage image) {
        FAVICON.set(handle, (image != null) ? image.toEncodedText() : null);
    }

    /**
     * Retrieve whether chat preview is enabled on the server.
     * @return whether chat preview is enabled on the server.
     * @since 1.19
     * @deprecated Removed in 1.19.3
     */
    @Deprecated
    public boolean isChatPreviewEnabled() {
        return (Boolean) BOOLEAN_ACCESSORS[0].get(handle);
    }

    /**
     * Sets whether chat preview is enabled on the server.
     * @param chatPreviewEnabled true if enabled, false otherwise.
     * @since 1.19
     * @deprecated Removed in 1.19.3
     */
    @Deprecated
    public void setChatPreviewEnabled(boolean chatPreviewEnabled) {
        BOOLEAN_ACCESSORS[0].set(handle, chatPreviewEnabled);
    }

    /**
     * Sets whether the server enforces secure chat.
     * @return whether the server enforces secure chat.
     * @since 1.19.1
     */
    @Override
    public boolean isEnforceSecureChat() {
        int index = MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove() ? 0 : 1;
        return (Boolean) BOOLEAN_ACCESSORS[index].get(handle);
    }

    /**
     * Sets whether the server enforces secure chat.
     * @param enforceSecureChat true if enabled, false otherwise.
     * @since 1.19.1
     */
    @Override
    public void setEnforceSecureChat(boolean enforceSecureChat) {
        int index = MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove() ? 0 : 1;
        BOOLEAN_ACCESSORS[index].set(handle, enforceSecureChat);
    }

    /**
     * Retrieve the displayed number of online players.
     * @return The displayed number.
     * @throws IllegalStateException If the player count has been hidden via {@link #setPlayersVisible(boolean)}.
     * @see #setPlayersOnline(int)
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void setPlayersVisible(boolean visible) {
        if (arePlayersVisible() != visible) {
            if (visible) {
                // Recreate the count and maximum
                Server server = Bukkit.getServer();
                setPlayersMaximum(server.getMaxPlayers());
                setPlayersOnline(Bukkit.getOnlinePlayers().size());
            } else {
                PLAYERS.set(handle, players = null);
            }
        }
    }

    @Override
    public String getJson() {
        return (String) GSON_TO_JSON.invoke(PING_GSON.get(null), getHandle());
    }

    /**
     * Determine if the player count and maximum is visible.
     * <p>
     * If not, the client will display ??? in the same location.
     * @return TRUE if the player statistics is visible, FALSE otherwise.
     */
    @Override
    public boolean arePlayersVisible() {
        return players != null;
    }

    /**
     * Retrieve a copy of all the logged in players.
     * @return Logged in players or an empty list if no player names will be displayed.
     */
    @Override
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
     * @param playerSample - every logged in player.
     */
    @Override
    public void setPlayers(Iterable<? extends WrappedGameProfile> playerSample) {
        if (players == null)
            resetPlayers();

        PLAYERS_PROFILES.set(players, PROFILE_CONVERT.getGeneric(playerSample));
    }

    /**
     * Retrieve the version name of the current server.
     * @return The version name.
     */
    @Override
    public String getVersionName() {
        return (String) VERSION_NAME.get(version);
    }

    /**
     * Set the version name of the current server.
     * @param name - the new version name.
     */
    @Override
    public void setVersionName(String name) {
        VERSION_NAME.set(version, name);
    }

    /**
     * Retrieve the protocol number.
     * @return The protocol.
     */
    @Override
    public int getVersionProtocol() {
        return (Integer) VERSION_PROTOCOL.get(version);
    }

    /**
     * Set the version protocol
     * @param protocol - the protocol number.
     */
    @Override
    public void setVersionProtocol(int protocol) {
        VERSION_PROTOCOL.set(version, protocol);
    }

    /**
     * Retrieve a deep copy of the current wrapper object.
     * @return The current object.
     */
    public LegacyServerPing deepClone() {
        LegacyServerPing copy = new LegacyServerPing();
        WrappedChatComponent motd = getMotD();

        copy.setPlayers(getPlayers());
        copy.setFavicon(getFavicon());
        copy.setMotD(motd != null ? motd : null);
        copy.setVersionName(getVersionName());
        copy.setVersionProtocol(getVersionProtocol());

        if (arePlayersVisible()) {
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
}
