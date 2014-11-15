/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
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

package com.comphenix.protocol.utility;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Server;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * Determine the current Minecraft version.
 * 
 * @author Kristian
 */
public class MinecraftVersion implements Comparable<MinecraftVersion>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Regular expression used to parse version strings.
	 */
	private static final String VERSION_PATTERN = ".*\\(.*MC.\\s*([a-zA-z0-9\\-\\.]+)\\s*\\)";
	
	/**
	 * Version 1.7.8 - the update that changed the skin format (and distribution - R.I.P. player disguise)
	 */
	public static final MinecraftVersion SKIN_UPDATE = new MinecraftVersion("1.7.8");
	
	/**
	 * Version 1.7.2 - the update that changed the world.
	 */
	public static final MinecraftVersion WORLD_UPDATE = new MinecraftVersion("1.7.2");
	
	/**
	 * Version 1.6.1 - the horse update.
	 */
	public static final MinecraftVersion HORSE_UPDATE = new MinecraftVersion("1.6.1");
	
	/**
	 * Version 1.5.0 - the redstone update.
	 */
	public static final MinecraftVersion REDSTONE_UPDATE = new MinecraftVersion("1.5.0");
	
	/**
	 * Version 1.4.2 - the scary update (Wither Boss).
	 */
	public static final MinecraftVersion SCARY_UPDATE = new MinecraftVersion("1.4.2");
	
	private final int major;
	private final int minor;
	private final int build;

	// The development stage
	private final String development; 
	
	// Snapshot?
	private final SnapshotVersion snapshot;
	
	/**
	 * Determine the current Minecraft version.
	 * @param server - the Bukkit server that will be used to examine the MC version.
	 */
	public MinecraftVersion(Server server) {
		this(extractVersion(server.getVersion()));
	}

	/**
	 * Construct a version object from the format major.minor.build, or the snapshot format.
	 * @param versionOnly - the version in text form.
	 */
	public MinecraftVersion(String versionOnly) {
		this(versionOnly, true);
	}
	
	/**
	 * Construct a version format from the standard release version or the snapshot verison.
	 * @param versionOnly - the version.
	 * @param parseSnapshot - TRUE to parse the snapshot, FALSE otherwise.
	 */
	private MinecraftVersion(String versionOnly, boolean parseSnapshot) {
		String[] section = versionOnly.split("-");
		SnapshotVersion snapshot = null;
		int[] numbers = new int[3];
		
		try {
			numbers = parseVersion(section[0]);
						
		} catch (NumberFormatException cause) {
			// Skip snapshot parsing
			if (!parseSnapshot)
				throw cause;
			
			try {
				// Determine if the snapshot is newer than the current release version
				snapshot = new SnapshotVersion(section[0]);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

				MinecraftVersion latest = new MinecraftVersion(ProtocolLibrary.MAXIMUM_MINECRAFT_VERSION, false);
				boolean newer = snapshot.getSnapshotDate().compareTo(
						        format.parse(ProtocolLibrary.MINECRAFT_LAST_RELEASE_DATE)) > 0;
						        
		        numbers[0] = latest.getMajor();
		        numbers[1] = latest.getMinor() + (newer ? 1 : -1);
		        numbers[2] = 0;
			} catch (Exception e) {
				throw new IllegalStateException("Cannot parse " + section[0], e);
			}
		}
		
		this.major = numbers[0];
		this.minor = numbers[1];
		this.build = numbers[2];
		this.development = section.length > 1 ? section[1] : (snapshot != null ? "snapshot" : null);
		this.snapshot = snapshot;
	}
	
	/**
	 * Construct a version object directly.
	 * @param major - major version number.
	 * @param minor - minor version number.
	 * @param build - build version number.
	 */
	public MinecraftVersion(int major, int minor, int build) {
		this(major, minor, build, null);
	}
	
	/**
	 * Construct a version object directly.
	 * @param major - major version number.
	 * @param minor - minor version number.
	 * @param build - build version number.
	 * @param development - development stage.
	 */
	public MinecraftVersion(int major, int minor, int build, String development) {
		this.major = major;
		this.minor = minor;
		this.build = build;
		this.development = development;
		this.snapshot = null;
	}

	private int[] parseVersion(String version) {
		String[] elements = version.split("\\.");
		int[] numbers = new int[3];
		
		// Make sure it's even a valid version 
		if (elements.length < 1)
			throw new IllegalStateException("Corrupt MC version: " + version);
	
		// The String 1 or 1.2 is interpreted as 1.0.0 and 1.2.0 respectively.
		for (int i = 0; i < Math.min(numbers.length, elements.length); i++)
			numbers[i] = Integer.parseInt(elements[i].trim());
		return numbers;
	}
	
	/**
	 * Major version number
	 * @return Current major version number.
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Minor version number
	 * @return Current minor version number.
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * Build version number
	 * @return Current build version number.
	 */
	public int getBuild() {
		return build;
	}
	
	/**
	 * Retrieve the development stage.
	 * @return Development stage, or NULL if this is a release.
	 */
	public String getDevelopmentStage() {
		return development;
	}
	
	/**
	 * Retrieve the snapshot version, or NULL if this is a release.
	 * @return The snapshot version.
	 */
	public SnapshotVersion getSnapshot() {
		return snapshot;
	}
	
	/**
	 * Determine if this version is a snapshot.
	 * @return The snapshot version.
	 */
	public boolean isSnapshot() {
		return snapshot != null;
	}
	
	/**
	 * Retrieve the version String (major.minor.build) only.
	 * @return A normal version string.
	 */
	public String getVersion() {
		if (getDevelopmentStage() == null)
			return String.format("%s.%s.%s", getMajor(), getMinor(), getBuild());
		else
			return String.format("%s.%s.%s-%s%s", getMajor(), getMinor(), getBuild(), 
					getDevelopmentStage(), isSnapshot() ? snapshot : "");
	}
	
	@Override
	public int compareTo(MinecraftVersion o) {
		if (o == null)
			return 1;
	
		return ComparisonChain.start().
					compare(getMajor(), o.getMajor()).
					compare(getMinor(), o.getMinor()).
					compare(getBuild(), o.getBuild()).
					// No development String means it's a release
					compare(getDevelopmentStage(), o.getDevelopmentStage(), Ordering.natural().nullsLast()).
					compare(getSnapshot(), o.getSnapshot(), Ordering.natural().nullsFirst()).
					result();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		
		if (obj instanceof MinecraftVersion) {
			MinecraftVersion other = (MinecraftVersion) obj;
			
			return getMajor() == other.getMajor() && 
				   getMinor() == other.getMinor() && 
				   getBuild() == other.getBuild() &&
				   Objects.equal(getDevelopmentStage(), other.getDevelopmentStage());
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getMajor(), getMinor(), getBuild());
	}

	@Override
	public String toString() {
		// Convert to a String that we can parse back again
		return String.format("(MC: %s)", getVersion());
	}
	
	/**
	 * Extract the Minecraft version from CraftBukkit itself.
	 * @param text - the server version in text form.
	 * @return The underlying MC version.
	 * @throws IllegalStateException If we could not parse the version string.
	 */
	public static String extractVersion(String text) {
		Pattern versionPattern = Pattern.compile(VERSION_PATTERN);
		Matcher version = versionPattern.matcher(text);
		
		if (version.matches() && version.group(1) != null) {
			return version.group(1);
		} else {
			throw new IllegalStateException("Cannot parse version String '" + text + "'");
		}
	}
	
	/**
	 * Parse the given server version into a Minecraft version.
	 * @param serverVersion - the server version.
	 * @return The resulting Minecraft version.
	 */
	public static MinecraftVersion fromServerVersion(String serverVersion) {
		return new MinecraftVersion(extractVersion(serverVersion));
	}
}
