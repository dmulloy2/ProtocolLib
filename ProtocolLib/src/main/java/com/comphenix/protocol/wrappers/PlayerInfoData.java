/**
 * (c) 2014 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.google.common.base.Objects;

/**
 * @author dmulloy2
 */

public class PlayerInfoData {
	private static Constructor<?> constructor;

	protected final WrappedGameProfile profile;
	protected final int ping;
	protected final NativeGameMode gameMode;
	protected final WrappedChatComponent displayName;

	// This is the same order as the NMS class, minus the packet (which isn't a field)
	public PlayerInfoData(WrappedGameProfile profile, int ping, NativeGameMode gameMode, WrappedChatComponent displayName) {
		this.ping = ping;
		this.gameMode = gameMode;
		this.profile = profile;
		this.displayName = displayName;
	}

	public WrappedGameProfile getProfile() {
		return profile;
	}

	public int getPing() {
		return ping;
	}

	public NativeGameMode getGameMode() {
		return gameMode;
	}

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
			public Object getGeneric(Class<?> genericType, PlayerInfoData specific) {
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
							specific.ping,
							EnumWrappers.getGameModeConverter().getGeneric(EnumWrappers.getGameModeClass(), specific.gameMode),
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
					StructureModifier<Object> modifier = new StructureModifier<Object>(generic.getClass(), null, false)
							.withTarget(generic);

					StructureModifier<WrappedGameProfile> gameProfiles = modifier.withType(
							MinecraftReflection.getGameProfileClass(), BukkitConverters.getWrappedGameProfileConverter());
					WrappedGameProfile gameProfile = gameProfiles.read(0);

					StructureModifier<Integer> ints = modifier.withType(int.class);
					int ping = ints.read(0);

					StructureModifier<NativeGameMode> gameModes = modifier.withType(
							EnumWrappers.getGameModeClass(), EnumWrappers.getGameModeConverter());
					NativeGameMode gameMode = gameModes.read(0);

					StructureModifier<WrappedChatComponent> displayNames = modifier.withType(
							MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());
					WrappedChatComponent displayName = displayNames.read(0);
					
					return new PlayerInfoData(gameProfile, ping, gameMode, displayName);
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
			return profile.equals(other.profile) && ping == other.ping && gameMode == other.gameMode
					&& displayName.equals(other.displayName);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(profile, ping, gameMode, displayName);
	}

	@Override
	public String toString() {
		return String.format("PlayerInfoData { profile=%s, ping=%s, gameMode=%s, displayName=%s }",
				profile, ping, gameMode, displayName);
	}
}
