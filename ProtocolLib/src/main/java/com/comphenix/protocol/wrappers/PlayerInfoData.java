/**
 * (c) 2014 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;

import com.comphenix.protocol.events.PacketContainer;
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

	protected final PacketContainer packet;
	protected final NativeGameMode gameMode;
	protected final int ping;
	protected final WrappedGameProfile profile;
	protected final WrappedChatComponent displayName;

	public PlayerInfoData(PacketContainer packet, NativeGameMode gameMode, int ping, WrappedGameProfile profile, WrappedChatComponent displayName) {
		this.packet = packet;
		this.ping = ping;
		this.gameMode = gameMode;
		this.profile = profile;
		this.displayName = displayName;
	}

	public PacketContainer getPacket() {
		return packet;
	}

	public NativeGameMode getGameMode() {
		return gameMode;
	}

	public int getPing() {
		return ping;
	}

	public WrappedGameProfile getProfile() {
		return profile;
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
						constructor = MinecraftReflection.getPlayerInfoDataClass().getConstructor(
								MinecraftReflection.getMinecraftClass("PacketPlayOutPlayerInfo"),
								EnumWrappers.getGameModeClass(),
								int.class,
								MinecraftReflection.getGameProfileClass(),
								MinecraftReflection.getIChatBaseComponentClass()
						);
					} catch (Exception e) {
						throw new RuntimeException("Cannot find PlayerInfoData constructor.", e);
					}
				}

				// Construct the underlying PlayerInfoData
				try {
					Object result = constructor.newInstance(
							specific.packet.getHandle(),
							EnumWrappers.getGameModeConverter().getGeneric(EnumWrappers.getGameModeClass(), specific.gameMode),
							specific.ping,
							specific.profile.handle,
							specific.displayName.handle
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

					StructureModifier<Integer> ints = modifier.withType(int.class);
					int ping = ints.read(0);

					StructureModifier<Object> packets = modifier.withType(
							MinecraftReflection.getMinecraftClass("PacketPlayOutPlayerInfo"));
					Object packet = packets.read(0);

					StructureModifier<NativeGameMode> gameModes = modifier.withType(
							EnumWrappers.getGameModeClass(), EnumWrappers.getGameModeConverter());
					NativeGameMode gameMode = gameModes.read(0);

					StructureModifier<WrappedGameProfile> gameProfiles = modifier.withType(
							MinecraftReflection.getGameProfileClass(), BukkitConverters.getWrappedGameProfileConverter());
					WrappedGameProfile gameProfile = gameProfiles.read(0);

					StructureModifier<WrappedChatComponent> displayNames = modifier.withType(
							MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());
					WrappedChatComponent displayName = displayNames.read(0);
					
					return new PlayerInfoData(PacketContainer.fromPacket(packet), gameMode, ping, gameProfile, displayName);
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
			return packet.equals(other.packet) && gameMode == other.gameMode && ping == other.ping
					&& profile.equals(other.profile) && displayName.equals(other.displayName);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(packet, gameMode, ping, profile, displayName);
	}

	@Override
	public String toString() {
		return String.format("PlayerInfoData[gameMode=%s,ping=%s,profile=%s,displayName=%s]", gameMode, ping, profile, displayName);
	}
}