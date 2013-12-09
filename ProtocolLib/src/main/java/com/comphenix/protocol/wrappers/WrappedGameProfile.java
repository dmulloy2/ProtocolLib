package com.comphenix.protocol.wrappers;

import net.minecraft.util.com.mojang.authlib.GameProfile;

/**
 * Represents a wrapper for a game profile.
 * @author Kristian
 */
public class WrappedGameProfile extends AbstractWrapper {
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
		this(new GameProfile(id, name));
	}
	
	/**
	 * Construct a wrapper around an existing game profile.
	 * @param profile - the underlying profile.
	 */
	public static WrappedGameProfile fromHandle(Object handle) {
		return new WrappedGameProfile(handle);
	}

	/**
	 * Retrieve the UUID of the player.
	 * @return The UUID of the player, or NULL if not computed.
	 */
	public String getId() {
		return getProfile().getId();
	}

	/**
	 * Retrieve the name of the player.
	 * @return The player name.
	 */
	public String getName() {
		return getProfile().getName();
	}
	
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
	public int hashCode() {
		return getProfile().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof WrappedGameProfile) {
			WrappedGameProfile other = (WrappedGameProfile) obj;
			return getProfile().equals(other.getProfile());
		}
		return false;
	}
}
