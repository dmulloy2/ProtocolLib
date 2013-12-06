package com.comphenix.protocol.wrappers;

import net.minecraft.util.com.mojang.authlib.GameProfile;

/**
 * Represents a wrapper for a game profile.
 * @author Kristian
 */
public class WrappedGameProfile {
	private GameProfile profile;
	
	// Profile from a handle
	private WrappedGameProfile(Object profile) {
		if (profile == null)
			throw new IllegalArgumentException("Profile cannot be NULL.");
		if (!(profile instanceof GameProfile)) 
			throw new IllegalArgumentException(profile + " is not a GameProfile");
		this.profile = (GameProfile) profile;
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
	 * Retrieve the underlying game profile.
	 * @return The profile.
	 */
	public Object getHandle() {
		return profile;
	}

	/**
	 * Retrieve the UUID of the player.
	 * @return The UUID of the player, or NULL if not computed.
	 */
	public String getId() {
		return profile.getId();
	}

	/**
	 * Retrieve the name of the player.
	 * @return The player name.
	 */
	public String getName() {
		return profile.getName();
	}

	/**
	 * Construct a new game profile with the same ID, but different id.
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
		return profile.isComplete();
	}
	
	@Override
	public int hashCode() {
		return profile.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj instanceof WrappedGameProfile) {
			WrappedGameProfile other = (WrappedGameProfile) obj;
			return profile.equals(other.profile);
		}
		return false;
	}
}
