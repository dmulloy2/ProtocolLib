package com.comphenix.protocol.wrappers;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.collection.ConvertedMultimap;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;

/**
 * Represents a wrapper for a game profile.
 * @author Kristian
 */
public class WrappedGameProfile extends AbstractWrapper {
	// Version 1.7.2 and 1.7.8 respectively
	private static final ConstructorAccessor CREATE_STRING_STRING = Accessors.getConstructorAccessorOrNull(GameProfile.class, String.class, String.class);
	private static final FieldAccessor GET_UUID_STRING = Accessors.getFieldAcccessorOrNull(GameProfile.class, "id", String.class);
	
	// Fetching game profile
	private static FieldAccessor PLAYER_PROFILE;
	private static FieldAccessor OFFLINE_PROFILE;
	
	// Property map
	private Multimap<String, WrappedSignedProperty> propertyMap;
	
	// Profile from a handle
	private WrappedGameProfile(Object profile) {
		super(GameProfile.class);
		setHandle(profile);
	}
	
	/**
	 * Retrieve the associated game profile of a player.
	 * <p>
	 * Note that this may not exist in the current Minecraft version.
	 * @param player - the player.
	 * @return The game profile.
	 */
	public static WrappedGameProfile fromPlayer(Player player) {
		FieldAccessor accessor = PLAYER_PROFILE;
		Object nmsPlayer = BukkitUnwrapper.getInstance().unwrapItem(player);
		
		if (accessor == null) {
			accessor = Accessors.getFieldAccessor(MinecraftReflection.getEntityHumanClass(), GameProfile.class, true);
			PLAYER_PROFILE = accessor;
		}
		return WrappedGameProfile.fromHandle(PLAYER_PROFILE.get(nmsPlayer));
	}
	
	/**
	 * Retrieve the associated game profile of an offline player.
	 * <p>
	 * Note that this may not exist in the current Minecraft version.
	 * @param player - the offline player.
	 * @return The game profile.
	 */
	public static WrappedGameProfile fromOfflinePlayer(OfflinePlayer player) {
		FieldAccessor accessor = OFFLINE_PROFILE;
		
		if (accessor == null) {
			accessor = Accessors.getFieldAccessor(player.getClass(), GameProfile.class, true);
			OFFLINE_PROFILE = accessor;
		}
		return WrappedGameProfile.fromHandle(OFFLINE_PROFILE.get(player));
	}
	
	/**
	 * Construct a new game profile with the given properties.
	 * <p>
	 * Note that this constructor is very lenient when parsing UUIDs for backwards compatibility reasons. 
	 * Thus - "", " ", "0" and "0-0-0-0" are all equivalent to the the UUID "00000000-0000-0000-0000-000000000000".
	 * @param id - the UUID of the player.
	 * @param name - the name of the player.
	 */
	public WrappedGameProfile(String id, String name) {
		super(GameProfile.class);
		
		if (CREATE_STRING_STRING != null) {
			setHandle(CREATE_STRING_STRING.invoke(id, name));
		} else {
			setHandle(new GameProfile(parseUUID(id, name), name));
		}
	}

	private static UUID parseUUID(String id, String name) {
		if (id == null)
			return null;
		
		try {
			// Interpret as zero
			if (StringUtils.isBlank(id))
				id = "0";
			int missing = 4 - StringUtils.countMatches(id, "-");
			
			// Lenient - add missing data
			if (missing > 0) {
				if (id.length() < 12) {
					id += StringUtils.repeat("-0", missing);
				} else if (id.length() >= 32) {
					StringBuilder builder = new StringBuilder(id);
					int position = 8; // Initial position
					
					while (missing > 0 && position < builder.length()) {
						builder.insert(position, "-");
						position += 5; // 4 in length, plus the hyphen
						missing--;
					}
					id = builder.toString();
				} else {
					throw new IllegalArgumentException("Invalid partial UUID: " + id);
				}
			}
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Cannot construct profile [" + id + ", " + name + "]", e);
		}
	}
	
	/**
	 * Construct a new game profile with the given properties.
	 * <p>
	 * Note that at least one of the parameters must be non-null.
	 * @param uuid - the UUID of the player, or NULL.
	 * @param name - the name of the player, or NULL.
	 */
	public WrappedGameProfile(UUID uuid, String name) {
		super(GameProfile.class);
		
		if (CREATE_STRING_STRING != null) {
			setHandle(CREATE_STRING_STRING.invoke(uuid != null ? uuid.toString() : null, name));
		} else {
			setHandle(new GameProfile(uuid, name));
		}
	}
	
	/**
	 * Construct a wrapper around an existing game profile.
	 * @param profile - the underlying profile, or NULL.
	 */
	public static WrappedGameProfile fromHandle(Object handle) {
		if (handle == null) 
			return null;
		return new WrappedGameProfile(handle);
	}

	/**
	 * Retrieve the UUID of the player.
	 * @return The UUID of the player, or NULL if not computed.
	 */
	public String getId() {
		if (GET_UUID_STRING != null)
			return (String) GET_UUID_STRING.get(handle);
		final GameProfile profile = getProfile();
		return profile.getId() != null ? profile.getId().toString() : null;
	}

	/**
	 * Retrieve the name of the player.
	 * @return The player name.
	 */
	public String getName() {
		return getProfile().getName();
	}
	
	/**
	 * Retrieve the property map of signed values.
	 * @return Property map.
	 */
	public Multimap<String, WrappedSignedProperty> getProperties() {
		Multimap<String, WrappedSignedProperty> result = propertyMap;

		if (result == null) {
			result = new ConvertedMultimap<String, Property, WrappedSignedProperty>(
					GuavaWrappers.getBukkitMultimap(getProfile().getProperties())) {
				@Override
				protected Property toInner(WrappedSignedProperty outer) {
					return (Property) outer.handle;
				}
				
				@Override
				protected Object toInnerObject(Object outer) {
					if (outer instanceof WrappedSignedProperty) {
						return toInner((WrappedSignedProperty) outer);
					}
					return outer;
				}
				
				@Override
				protected WrappedSignedProperty toOuter(Property inner) {
					return WrappedSignedProperty.fromHandle(inner);
				}
			};
			propertyMap = result;
		}
		return result;
	}
	
	/**
	 * Retrieve the underlying GameProfile.
	 * @return The GameProfile.
	 */
	private GameProfile getProfile() {
		return (GameProfile) handle;
	}

	/**
	 * Construct a new game profile with the same ID, but different name.
	 * @param name - the new name of the profile to create.
	 * @return The new game profile.
	 */
	public WrappedGameProfile withName(String name) {
		return new WrappedGameProfile(getId(), name);
	}
	
	/**
	 * Construct a new game profile with the same name, but different id.
	 * @param id - the new id of the profile to create.
	 * @return The new game profile.
	 */
	public WrappedGameProfile withId(String id) {
		return new WrappedGameProfile(id, getName());
	}
	
	/**
	 * Determine if the game profile contains both an UUID and a name.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean isComplete() {
		return getProfile().isComplete();
	}
	
	@Override
	public String toString() {
		return String.valueOf(getProfile());
	}
	
	@Override
	public int hashCode() {
		// Mojang's hashCode() is broken, it doesn't handle NULL id or name. So we implement our own
		return Objects.hashCode(getId(), getName());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof WrappedGameProfile) {
			WrappedGameProfile other = (WrappedGameProfile) obj;
			return Objects.equal(getProfile(), other.getProfile());
		}
		return false;
	}
}
