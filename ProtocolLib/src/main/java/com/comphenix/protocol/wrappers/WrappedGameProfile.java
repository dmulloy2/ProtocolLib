package com.comphenix.protocol.wrappers;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import net.minecraft.util.com.mojang.authlib.GameProfile;

/**
 * Represents a wrapper for a game profile.
 * @author Kristian
 */
public class WrappedGameProfile extends AbstractWrapper {
	// Version 1.7.2 and 1.7.8 respectively
	private static final ConstructorAccessor CREATE_STRING_STRING = Accessors.getConstructorAccessorOrNull(GameProfile.class, String.class, String.class);
	private static final FieldAccessor GET_UUID_STRING = Accessors.getFieldAcccessorOrNull(GameProfile.class, "id", String.class);
	
	// Profile from a handle
	private WrappedGameProfile(Object profile) {
		super(GameProfile.class);
		setHandle(profile);
	}
	
	/**
	 * Construct a new game profile with the given properties.
	 * @param id - the UUID of the player.
	 * @param name - the name of the player.
	 */
	public WrappedGameProfile(String id, String name) {
		super(GameProfile.class);
		
		if (CREATE_STRING_STRING != null) {
			setHandle(CREATE_STRING_STRING.invoke(id, name));
		} else {
			parseUUID(id, name);
			setHandle(new GameProfile(parseUUID(id, name), name));
		}
	}

	private UUID parseUUID(String id, String name) {
		try {
			int missing = 4 - StringUtils.countMatches(id, "-");
			
			// Lenient - add missing data
			if (missing > 0) {
				id += StringUtils.repeat("-0", missing);
			}
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Cannot construct profile [" + id + ", " + name + "]", e);
		}
	}
	
	/**
	 * Construct a new game profile with the given properties.
	 * @param uuid - the UUID of the player.
	 * @param name - the name of the player.
	 */
	public WrappedGameProfile(UUID uuid, String name) {
		super(GameProfile.class);
		
		if (CREATE_STRING_STRING != null) {
			setHandle(CREATE_STRING_STRING.invoke(uuid.toString(), name));
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
		if (GET_UUID_STRING == null)
			return (String) GET_UUID_STRING.get(handle);
		return getProfile().getId() != null ? getProfile().getId().toString() : null;
	}

	/**
	 * Retrieve the name of the player.
	 * @return The player name.
	 */
	public String getName() {
		return getProfile().getName();
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
