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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Server;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * Determine the current Minecraft version.
 * 
 * @author Kristian
 */
public class MinecraftVersion implements Comparable<MinecraftVersion> {
	/**
	 * Regular expression used to parse version strings.
	 */
	private static final String VERSION_PATTERN = ".*\\(MC:\\s*((?:\\d+\\.)*\\d)\\s*\\)";
	
	private final int major;
	private final int minor;
	private final int build;
	
	// The development stage
	private final String development;

	/**
	 * Determine the current Minecraft version.
	 * @param server - the Bukkit server that will be used to examine the MC version.
	 */
	public MinecraftVersion(Server server) {
		this(extractVersion(server.getVersion()));
	}

	/**
	 * Construct a version object from the format major.minor.build.
	 * @param versionOnly - the version in text form.
	 */
	public MinecraftVersion(String versionOnly) {
		String[] section = versionOnly.split("-");
		int[] numbers = parseVersion(section[0]);
		
		this.major = numbers[0];
		this.minor = numbers[1];
		this.build = numbers[2];
		this.development = section.length > 1 ? section[1] : null;
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
	 * Retrieve the version String (major.minor.build) only.
	 * @return A normal version string.
	 */
	public String getVersion() {
		if (development == null)
			return String.format("%s.%s.%s", major, minor, build);
		else
			return String.format("%s.%s.%s-%s", major, minor, build, development);
	}
	
	@Override
	public int compareTo(MinecraftVersion o) {
		if (o == null)
			return 1;
	
		return ComparisonChain.start().
					compare(major, o.major).
					compare(minor, o.minor).
					compare(build, o.build).
					// No development String means it's a release
					compare(development, o.development, Ordering.natural().nullsLast()).
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
			
			return major == other.major && 
				   minor == other.minor && 
				   build == other.build &&
				   Objects.equal(development, other.development);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(major, minor, build);
	}

	@Override
	public String toString() {
		// Convert to a String that we can parse back again
		return String.format("(MC: %s)", getVersion());
	}
	
	/**
	 * Extract the Minecraft version from CraftBukkit itself.
	 * @param server - the server object representing CraftBukkit.
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
}
