package com.comphenix.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Server;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
 * Determine the current Minecraft version.
 * 
 * @author Kristian
 */
class MinecraftVersion implements Comparable<MinecraftVersion> {
	/**
	 * Regular expression used to parse version strings.
	 */
	private static final String VERSION_PATTERN = ".*\\(MC:\\s*((?:\\d+\\.)*\\d)\\s*\\)";
	
	private final int major;
	private final int minor;
	private final int build;

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
		int[] numbers = parseVersion(versionOnly);
		
		this.major = numbers[0];
		this.minor = numbers[1];
		this.build = numbers[2];
	}
	
	/**
	 * Construct a version object directly.
	 * @param major - major version number.
	 * @param minor - minor version number.
	 * @param build - build version number.
	 */
	public MinecraftVersion(int major, int minor, int build) {
		this.major = major;
		this.minor = minor;
		this.build = build;
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
	 * Retrieve the version String (major.minor.build) only.
	 * @return A normal version string.
	 */
	public String getVersion() {
		return String.format("%s.%s.%s", major, minor, build);
	}
	
	@Override
	public int compareTo(MinecraftVersion o) {
		if (o == null)
			return 1;
	
		return ComparisonChain.start().
					compare(major, o.major).
					compare(minor, o.minor).
					compare(build, o.build).
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
				   build == other.build;
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
