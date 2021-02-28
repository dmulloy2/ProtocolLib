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

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.google.common.base.Objects;

/**
 * Represents an immutable PlayerInfoData in the PLAYER_INFO packet.
 * @author dmulloy2
 */
public class PlayerInfoData {
	private static Constructor<?> constructor;

	private final int latency;
	private final NativeGameMode gameMode;
	private final WrappedGameProfile profile;
	private final WrappedChatComponent displayName;

	// This is the same order as the NMS class, minus the packet (which isn't a field)
	public PlayerInfoData(WrappedGameProfile profile, int latency, NativeGameMode gameMode, WrappedChatComponent displayName) {
		this.profile = profile;
		this.latency = latency;
		this.gameMode = gameMode;
		this.displayName = displayName;
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
	 * Used to convert between NMS PlayerInfoData and the wrapper instance.
	 * @return A new converter.
	 */
	public static EquivalentConverter<PlayerInfoData> getConverter() {
		return new EquivalentConverter<PlayerInfoData>() {
			@Override
			public Object getGeneric(PlayerInfoData specific) {
				if (constructor == null) {
					try {
						// public PlayerInfoData(Packet, GameProfile, int, GameMode, ChatComponent)
						constructor = MinecraftReflection.getPlayerInfoDataClass().getConstructor(
								MinecraftReflection.getMinecraftClass("PacketPlayOutPlayerInfo"),
								MinecraftReflection.getGameProfileClass(),
								int.class,
								EnumWrappers.getGameModeClass(),
								MinecraftReflection.getIChatBaseComponentClass()
						);
					} catch (Exception e) {
						throw new RuntimeException("Cannot find PlayerInfoData constructor.", e);
					}
				}

				// Attempt to construct the underlying PlayerInfoData

				try {
					// public PlayerInfoData(null, GameProfile, ping, GameMode, ChatComponent)
					// The packet isn't a field, so it can be null
					Object result = constructor.newInstance(
							null,
							specific.profile.handle,
							specific.latency,
							EnumWrappers.getGameModeConverter().getGeneric(specific.gameMode),
							specific.displayName != null ? specific.displayName.handle : null
					);
					return result;
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
					
					return new PlayerInfoData(gameProfile, latency, gameMode, displayName);
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
					&& displayName.equals(other.displayName);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(latency, gameMode, profile, displayName);
	}

	@Override
	public String toString() {
		return String.format("PlayerInfoData[latency=%s, gameMode=%s, profile=%s, displayName=%s]",
				latency, gameMode, profile, displayName);
	}
}
