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

import com.comphenix.protocol.wrappers.WrappedProfilePublicKey.WrappedProfileKeyData;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;

/**
 * Represents an immutable PlayerInfoData in the PLAYER_INFO packet.
 * @author dmulloy2
 */
public class PlayerInfoData {
	private static Constructor<?> constructor;

	private final UUID profileId;
	private final int latency;
	private final boolean listed;
	private final NativeGameMode gameMode;
	private final WrappedGameProfile profile;
	private final WrappedChatComponent displayName;
	private final WrappedProfileKeyData profileKeyData;

	// This is the same order as the NMS class, minus the packet (which isn't a field)
	public PlayerInfoData(WrappedGameProfile profile, int latency, NativeGameMode gameMode, WrappedChatComponent displayName) {
		this(profile, latency, gameMode, displayName, null);
	}

	public PlayerInfoData(WrappedGameProfile profile, int latency, NativeGameMode gameMode, WrappedChatComponent displayName, WrappedProfileKeyData keyData) {
		this(profile.getUUID(), latency, true, gameMode, profile, displayName, keyData);
	}

	public PlayerInfoData(UUID profileId, int latency, boolean listed, NativeGameMode gameMode, WrappedGameProfile profile, WrappedChatComponent displayName, WrappedProfileKeyData profileKeyData) {
		this.profileId = profileId;
		this.latency = latency;
		this.listed = listed;
		this.gameMode = gameMode;
		this.profile = profile;
		this.displayName = displayName;
		this.profileKeyData = profileKeyData;
	}

	/**
	 * Get the id of the affected profile (since 1.19.3)
	 * @return the id of the profile
	 */
	public UUID getProfileId() {
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
	 * Gets if the player is listed on the client.
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
	 * Gets the profile key data of the player represented by this data, null if not present.
	 * @return The profile key data
	 */
	public WrappedProfileKeyData getProfileKeyData() {
		return this.profileKeyData;
	}

	/**
	 * Used to convert between NMS PlayerInfoData and the wrapper instance.
	 * @return A new converter.
	 */
	public static EquivalentConverter<PlayerInfoData> getConverter() {
		return new EquivalentConverter<PlayerInfoData>() {
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

						if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
							args.add(MinecraftReflection.getRemoteChatSessionClass());
						} else if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
							args.add(MinecraftReflection.getProfilePublicKeyDataClass());
						}

						constructor = MinecraftReflection.getPlayerInfoDataClass().getConstructor(args.toArray(new Class<?>[0]));
					} catch (Exception e) {
						throw new RuntimeException("Cannot find PlayerInfoData constructor.", e);
					}
				}

				// Attempt to construct the underlying PlayerInfoData

				try {
					Object gameMode = EnumWrappers.getGameModeConverter().getGeneric(specific.gameMode);
					Object displayName = specific.displayName != null ? specific.displayName.handle : null;

					if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
						return constructor.newInstance(
								specific.profileId,
								specific.profile.handle,
								specific.listed,
								specific.latency,
								gameMode,
								displayName,
								 null); // TODO: do we want to support this?
					} else if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
						return constructor.newInstance(
								specific.profile.handle,
								specific.latency,
								gameMode,
								displayName,
								specific.profileKeyData == null ? null : specific.profileKeyData.handle);
					} else if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
						return constructor.newInstance(specific.profile.handle, specific.latency, gameMode, displayName);
					} else {
						return constructor.newInstance(null, specific.profile.handle, specific.latency, gameMode, displayName);
					}
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
							EnumWrappers.getGameModeClass(), EnumWrappers.getGameModeConverter());
					NativeGameMode gameMode = gameModes.read(0);

					StructureModifier<WrappedChatComponent> displayNames = modifier.withType(
							MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());
					WrappedChatComponent displayName = displayNames.read(0);

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
			return profile.equals(other.profile) && latency == other.latency && gameMode == other.gameMode
					&& Objects.equals(displayName, other.displayName);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(latency, gameMode, profile, displayName);
	}

	@Override
	public String toString() {
		return String.format("PlayerInfoData[latency=%s, gameMode=%s, profile=%s, displayName=%s]",
				latency, gameMode, profile, displayName);
	}
}
