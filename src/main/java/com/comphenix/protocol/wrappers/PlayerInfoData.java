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
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.WrappedProfilePublicKey.WrappedProfileKeyData;

/**
 * Represents an immutable PlayerInfoData in the PLAYER_INFO packet.
 * @author dmulloy2
 */
public class PlayerInfoData {
    private static Constructor<?> constructor;

    private final UUID profileId;
    private final int latency;
    private final int listOrder;
    private final boolean listed;
    private final boolean showHat;
    private final NativeGameMode gameMode;
    private final WrappedGameProfile profile;
    private final WrappedChatComponent displayName;
    @Nullable
    private final WrappedRemoteChatSessionData remoteChatSessionData;
    @Nullable
    private final WrappedProfileKeyData profileKeyData;

    // This is the same order as the NMS class, minus the packet (which isn't a field)
    public PlayerInfoData(WrappedGameProfile profile, int latency, NativeGameMode gameMode, WrappedChatComponent displayName) {
        this(profile, latency, gameMode, displayName, null);
    }

    public PlayerInfoData(WrappedGameProfile profile, int latency, NativeGameMode gameMode, WrappedChatComponent displayName, WrappedProfileKeyData keyData) {
        this(profile.getUUID(), latency, true, gameMode, profile, displayName, keyData);
    }

    /**
     * Constructs a new PlayerInfoData for Minecraft 1.19 or later without signature data
     * @see PlayerInfoData#PlayerInfoData(UUID, int, boolean, NativeGameMode, WrappedGameProfile, WrappedChatComponent, WrappedRemoteChatSessionData)
     *
     * @param profileId the id of the profile (has to be non-null)
     * @param latency the latency in milliseconds
     * @param listed whether the player is listed in the tab list
     * @param gameMode the game mode
     * @param profile the game profile
     * @param displayName display name in tab list (optional)
     */
    public PlayerInfoData(UUID profileId, int latency, boolean listed, NativeGameMode gameMode, WrappedGameProfile profile, WrappedChatComponent displayName) {
        this(profileId, latency, listed, gameMode, profile, displayName, (WrappedRemoteChatSessionData) null);
    }

    /**
     * Constructs a new PlayerInfoData for Minecraft 1.19.3 or later.
     *
     * @param profileId the id of the profile (has to be non-null)
     * @param latency the latency in milliseconds
     * @param listed whether the player is listed in the tab list
     * @param gameMode the game mode
     * @param profile the game profile
     * @param displayName display name in tab list (optional)
     * @param remoteChatSession the remote chat session for this profile or null
     */
    public PlayerInfoData(UUID profileId, int latency, boolean listed, NativeGameMode gameMode, WrappedGameProfile profile, WrappedChatComponent displayName, @Nullable WrappedRemoteChatSessionData remoteChatSession) {
        this(profileId, latency, listed, gameMode, profile, displayName, false, remoteChatSession);
    }

    /**
     * Constructs a new PlayerInfoData for Minecraft 1.19. This is incompatible on 1.19.3.
     * @see PlayerInfoData#PlayerInfoData(UUID, int, boolean, NativeGameMode, WrappedGameProfile, WrappedChatComponent, WrappedRemoteChatSessionData)
     *
     * @param profileId the id of the profile (has to be non-null)
     * @param latency the latency in milliseconds
     * @param listed whether the player is listed in the tab list
     * @param gameMode the game mode
     * @param profile the game profile
     * @param displayName display name in tab list (optional)
     * @param profileKeyData the public key for the profile or null
     */
    @Deprecated
    public PlayerInfoData(UUID profileId, int latency, boolean listed, NativeGameMode gameMode, WrappedGameProfile profile, WrappedChatComponent displayName, @Nullable WrappedProfileKeyData profileKeyData) {
        this.profileId = profileId;
        this.latency = latency;
        this.listed = listed;
        this.gameMode = gameMode;
        this.profile = profile;
        this.displayName = displayName;
        this.profileKeyData = profileKeyData;
        this.remoteChatSessionData = null;
        this.showHat = false;
        this.listOrder = 0;
    }

    /**
     * Constructs a new PlayerInfoData for Minecraft 1.21.2 or later.
     *
     * @param profileId the id of the profile (has to be non-null)
     * @param latency the latency in milliseconds
     * @param listed whether the player is listed in the tab list
     * @param gameMode the game mode
     * @param profile the game profile
     * @param displayName display name in tab list (optional)
     * @param listOrder the priority of this entry in the tab list
     * @param remoteChatSession the remote chat session for this profile or null
     */
    public PlayerInfoData(UUID profileId, int latency, boolean listed, NativeGameMode gameMode, WrappedGameProfile profile, WrappedChatComponent displayName, int listOrder, @Nullable WrappedRemoteChatSessionData remoteChatSession) {
        this(profileId, latency, listed, gameMode, profile, displayName, false, listOrder, remoteChatSession);
    }

    /**
     * Constructs a new PlayerInfoData for Minecraft 1.21.4 or later.
     *
     * @param profileId the id of the profile (has to be non-null)
     * @param latency the latency in milliseconds
     * @param listed whether the player is listed in the tab list
     * @param gameMode the game mode
     * @param profile the game profile
     * @param displayName display name in tab list (optional)
     * @param showHat whether a hat should be shown
     * @param remoteChatSession the remote chat session for this profile or null
     */
    public PlayerInfoData(UUID profileId, int latency, boolean listed, NativeGameMode gameMode, WrappedGameProfile profile, WrappedChatComponent displayName, boolean showHat, @Nullable WrappedRemoteChatSessionData remoteChatSession) {
        this(profileId, latency, listed, gameMode, profile, displayName, showHat, 0, remoteChatSession);
    }

    /**
     * Constructs a new PlayerInfoData for Minecraft 1.21.4 or later.
     *
     * @param profileId the id of the profile (has to be non-null)
     * @param latency the latency in milliseconds
     * @param listed whether the player is listed in the tab list
     * @param gameMode the game mode
     * @param profile the game profile
     * @param displayName display name in tab list (optional)
     * @param showHat whether a hat should be shown
     * @param listOrder the priority of this entry in the tab list
     * @param remoteChatSession the remote chat session for this profile or null
     */
    public PlayerInfoData(UUID profileId, int latency, boolean listed, NativeGameMode gameMode, WrappedGameProfile profile, WrappedChatComponent displayName, boolean showHat, int listOrder, @Nullable WrappedRemoteChatSessionData remoteChatSession) {
        this.profileId = profileId;
        this.latency = latency;
        this.listed = listed;
        this.gameMode = gameMode;
        this.profile = profile;
        this.displayName = displayName;
        this.profileKeyData = null;
        this.remoteChatSessionData = remoteChatSession;
        this.showHat = showHat;
        this.listOrder = listOrder;
    }

    /**
     * Get the id of the affected profile (since 1.19.3)
     * @return the id of the profile
     */
    public UUID getProfileId() {
        if(profileId == null && profile != null) {
            return profile.getUUID(); // Ensure forward compatability
        }
        return profileId;
    }

    /**
     * Gets the GameProfile of the player represented by this data.
     * @return The GameProfile
     */
    public WrappedGameProfile getProfile() {
        return profile;
    }

    /**
     * @deprecated Replaced by {@link #getLatency()}
     */
    @Deprecated
    public int getPing() {
        return latency;
    }

    /**
     * Gets the latency between the client and the server.
     * @return The latency
     */
    public int getLatency() {
        return latency;
    }

    /**
     * Gets if the player is listed on the client (since 1.19.3)
     * @return if the player is listed
     */
    public boolean isListed() {
        return listed;
    }

    /**
     * Gets the GameMode of the player represented by this data.
     * @return The GameMode
     */
    public NativeGameMode getGameMode() {
        return gameMode;
    }

    /**
     * Gets the display name of the player represented by this data.
     * @return The display name
     */
    public WrappedChatComponent getDisplayName() {
        return displayName;
    }

    /**
     * Gets if a hat is shown (since 1.21.4)
     * @return if the hat is shown
     */
    public boolean isShowHat() {
        return showHat;
    }

    /**
     * Gets the priority of this entry in the tab list (since 1.21.2)
     * @return the priority of this entry in the tab list
     */
    public int getListOrder() {
        return listOrder;
    }

    /**
     * Returns the public key of the profile (since 1.19). Returns the public key of the remote chat session since 1.19.3
     * @return The public key of the profile.
     */
    @Nullable
    public WrappedProfileKeyData getProfileKeyData() {
        return this.profileKeyData != null ? this.profileKeyData : (this.remoteChatSessionData != null ? this.remoteChatSessionData.getProfilePublicKey() : null);
    }

    /**
     * Returns the remoteChatSessionData (since 1.19.3)
     * @return The remote chat sesion data or null
     */
    @Nullable
    public WrappedRemoteChatSessionData getRemoteChatSessionData() {
        return this.remoteChatSessionData;
    }

    /**
     * Used to convert between NMS PlayerInfoData and the wrapper instance.
     * @return A new converter.
     */
    public static EquivalentConverter<PlayerInfoData> getConverter() {
        return new EquivalentConverter<>() {
            private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

            @Override
            public Object getGeneric(PlayerInfoData specific) {
                if (constructor == null) {
                    try {
                        List<Class<?>> args = new ArrayList<>();
                        if (!MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
                            args.add(PacketType.Play.Server.PLAYER_INFO.getPacketClass());
                        }

                        if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                            args.add(UUID.class);
                        }

                        args.add(MinecraftReflection.getGameProfileClass());
                        if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                            args.add(boolean.class);
                        }

                        args.add(int.class);
                        args.add(EnumWrappers.getGameModeClass());
                        args.add(MinecraftReflection.getIChatBaseComponentClass());

                        if (MinecraftVersion.v1_21_4.atOrAbove()) {
                            args.add(boolean.class);
                        }
                        if (MinecraftVersion.v1_21_2.atOrAbove()) {
                            args.add(int.class);
                        }

                        if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                            args.add(MinecraftReflection.getRemoteChatSessionDataClass());
                        } else if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                            args.add(MinecraftReflection.getProfilePublicKeyDataClass());
                        }

                        constructor = MinecraftReflection.getPlayerInfoDataClass().getConstructor(args.toArray(EMPTY_CLASS_ARRAY));
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot find PlayerInfoData constructor.", e);
                    }
                }

                // Attempt to construct the underlying PlayerInfoData

                try {
                    Object gameMode = Converters.ignoreNull(EnumWrappers.getGameModeConverter()).getGeneric(specific.gameMode);
                    Object displayName = specific.displayName != null ? specific.displayName.handle : null;
                    Object profile = specific.profile != null ? specific.profile.handle : null;
                    Object remoteChatSessionData = specific.remoteChatSessionData != null ? BukkitConverters.getWrappedRemoteChatSessionDataConverter().getGeneric(specific.remoteChatSessionData) : null;

                    Object[] args;

                    if (MinecraftVersion.v1_21_4.atOrAbove()) {
                        args = new Object[] {
                            specific.profileId,
                            profile,
                            specific.listed,
                            specific.latency,
                            gameMode,
                            displayName,
                            specific.showHat,
                            specific.listOrder,
                            remoteChatSessionData
                        };
                    } else if (MinecraftVersion.v1_21_2.atOrAbove()) {
                        args = new Object[] {
                            specific.profileId,
                            profile,
                            specific.listed,
                            specific.latency,
                            gameMode,
                            displayName,
                            specific.listOrder,
                            remoteChatSessionData
                        };
                    } else if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                        args = new Object[] {
                            specific.profileId,
                            profile,
                            specific.listed,
                            specific.latency,
                            gameMode,
                            displayName,
                            remoteChatSessionData
                        };
                    } else if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                        args = new Object[] {
                            profile,
                            specific.latency,
                            gameMode,
                            displayName,
                            specific.profileKeyData == null ? null : specific.profileKeyData.handle
                        };
                    } else if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
                        args = new Object[] {
                            profile,
                            specific.latency,
                            gameMode,
                            displayName
                        };
                    } else {
                        args = new Object[] {
                            null,
                            profile,
                            specific.latency,
                            gameMode,
                            displayName
                        };
                    }

                    return constructor.newInstance(args);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to construct PlayerInfoData.", e);
                }
            }

            @Override
            public PlayerInfoData getSpecific(Object generic) {
                if (MinecraftReflection.isPlayerInfoData(generic)) {
                    StructureModifier<Object> modifier = new StructureModifier<>(generic.getClass(), null, false)
                        .withTarget(generic);

                    StructureModifier<WrappedGameProfile> gameProfiles = modifier.withType(
                        MinecraftReflection.getGameProfileClass(), BukkitConverters.getWrappedGameProfileConverter());
                    WrappedGameProfile gameProfile = gameProfiles.read(0);

                    StructureModifier<Integer> ints = modifier.withType(int.class);
                    int latency = ints.read(0);

                    StructureModifier<NativeGameMode> gameModes = modifier.withType(
                        EnumWrappers.getGameModeClass(), Converters.ignoreNull(EnumWrappers.getGameModeConverter()));
                    NativeGameMode gameMode = gameModes.read(0);

                    StructureModifier<WrappedChatComponent> displayNames = modifier.withType(
                        MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());
                    WrappedChatComponent displayName = displayNames.read(0);

                    if (MinecraftVersion.v1_21_4.atOrAbove()) {
                        return new PlayerInfoData(modifier.<UUID>withType(UUID.class).read(0),
                            latency,
                            modifier.<Boolean>withType(boolean.class).read(0),
                            gameMode,
                            gameProfile,
                            displayName,
                            modifier.<Boolean>withType(boolean.class).read(1),
                            ints.read(1),
                            modifier.withType(MinecraftReflection.getRemoteChatSessionDataClass(), BukkitConverters.getWrappedRemoteChatSessionDataConverter()).read(0)
                        );
                    }
                    if (MinecraftVersion.v1_21_2.atOrAbove()) {
                        return new PlayerInfoData(modifier.<UUID>withType(UUID.class).read(0),
                            latency,
                            modifier.<Boolean>withType(boolean.class).read(0),
                            gameMode,
                            gameProfile,
                            displayName,
                            ints.read(1),
                            modifier.withType(MinecraftReflection.getRemoteChatSessionDataClass(), BukkitConverters.getWrappedRemoteChatSessionDataConverter()).read(0)
                        );
                    }
                    if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
                        return new PlayerInfoData(modifier.<UUID>withType(UUID.class).read(0),
                            latency,
                            modifier.<Boolean>withType(boolean.class).read(0),
                            gameMode,
                            gameProfile,
                            displayName,
                            modifier.withType(MinecraftReflection.getRemoteChatSessionDataClass(), BukkitConverters.getWrappedRemoteChatSessionDataConverter()).read(0)
                        );
                    }
                    WrappedProfileKeyData key = null;
                    if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
                        StructureModifier<WrappedProfileKeyData> keyData = modifier.withType(
                            MinecraftReflection.getProfilePublicKeyDataClass(), BukkitConverters.getWrappedPublicKeyDataConverter());
                        key = keyData.read(0);
                    }

                    return new PlayerInfoData(gameProfile, latency, gameMode, displayName, key);
                }

                // Otherwise, return null
                return null;
            }

            // Thanks Java Generics!
            @Override
            public Class<PlayerInfoData> getSpecificType() {
                return PlayerInfoData.class;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        // Fast checks
        if (this == obj) return true;
        if (obj == null) return false;

        // Only compare objects of similar type
        if (obj instanceof PlayerInfoData) {
            PlayerInfoData other = (PlayerInfoData) obj;
            return Objects.equals(profile, other.profile)
                    && Objects.equals(profileId, other.profileId)
                    && latency == other.latency
                    && gameMode == other.gameMode
                    && Objects.equals(displayName, other.displayName)
                    && listed == other.listed
                    && Objects.equals(remoteChatSessionData, other.remoteChatSessionData)
                    && Objects.equals(profileKeyData, other.profileKeyData);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latency, gameMode, profile, displayName, profileKeyData, remoteChatSessionData, listed);
    }

    @Override
    public String toString() {
        if(MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
            return String.format("PlayerInfoData[latency=%s, listed=%b, gameMode=%s, profile=%s, displayName=%s, remoteChatSession=%s]",
                    latency, listed, gameMode, profile, displayName, remoteChatSessionData);
        }
        if(MinecraftVersion.WILD_UPDATE.atOrAbove()) {
            return String.format("PlayerInfoData[latency=%s, listed=%b, gameMode=%s, profile=%s, displayName=%s, profilePublicKey=%s]",
                    latency, listed, gameMode, profile, displayName, profileKeyData);
        }
        return String.format("PlayerInfoData[latency=%s, gameMode=%s, profile=%s, displayName=%s]",
            latency, gameMode, profile, displayName);
    }
}
