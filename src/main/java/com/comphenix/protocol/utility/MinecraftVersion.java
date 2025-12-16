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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * Determine the current Minecraft version.
 *
 * @author Kristian
 */
public final class MinecraftVersion implements Comparable<MinecraftVersion>, Serializable {
    /**
     * Version 1.21.11 - mounts of mayhem
     */
    public static final MinecraftVersion v1_21_11 = new MinecraftVersion("1.21.11");

    /**
     * Version 1.21.10 - hotfix for 1.21.9
     */
    public static final MinecraftVersion v1_21_10 = new MinecraftVersion("1.21.10");

    /**
     * Version 1.21.9 - the copper age drop
     */
    public static final MinecraftVersion v1_21_9 = new MinecraftVersion("1.21.9");

    /**
     * Version 1.21.6 - chase the skies
     */
    public static final MinecraftVersion v1_21_6 = new MinecraftVersion("1.21.6");

    /**
     * Version 1.21.5 - spring to life drop
     */
    public static final MinecraftVersion v1_21_5 = new MinecraftVersion("1.21.5");

    /**
     * Version 1.21.4 - the garden awakens drop
     */
    public static final MinecraftVersion v1_21_4 = new MinecraftVersion("1.21.4");

    /**
     * Version 1.21.2 - the bundles of bravery drop
     */
    public static final MinecraftVersion v1_21_2 = new MinecraftVersion("1.21.2");

    /**
     * Version 1.21.0 - the tricky trials update
     */
    public static final MinecraftVersion v1_21_0 = new MinecraftVersion("1.21.0");

    /**
     * Version 1.20.5 - the cookie and transfer packet update
     */
    public static final MinecraftVersion v1_20_5 = new MinecraftVersion("1.20.5");

	/**
	 * Version 1.20.4 - the decorated pot update
	 */
	public static final MinecraftVersion v1_20_4 = new MinecraftVersion("1.20.4");

    /**
     * Version 1.20.2 - the update that added the configuration protocol phase.
     */
    public static final MinecraftVersion CONFIG_PHASE_PROTOCOL_UPDATE = new MinecraftVersion("1.20.2");
    /**
     * Version 1.20 - the trails and tails update
     */
    public static final MinecraftVersion TRAILS_AND_TAILS = new MinecraftVersion("1.20");

    /**
     * Version 1.19.4 - the rest of the feature preview
     */
    public static final MinecraftVersion FEATURE_PREVIEW_2 = new MinecraftVersion("1.19.4");

    /**
     * Version 1.19.3 - introducing feature preview
     */
    public static final MinecraftVersion FEATURE_PREVIEW_UPDATE = new MinecraftVersion("1.19.3");
    /**
     * Version 1.19 - the wild update
     */
    public static final MinecraftVersion WILD_UPDATE = new MinecraftVersion("1.19");
    /**
     * Version 1.18 - caves and cliffs part 2
     */
    public static final MinecraftVersion CAVES_CLIFFS_2 = new MinecraftVersion("1.18");
    /**
     * Version 1.17 - caves and cliffs part 1
     */
    public static final MinecraftVersion CAVES_CLIFFS_1 = new MinecraftVersion("1.17");
    /**
     * Version 1.16.4
     */
    public static final MinecraftVersion NETHER_UPDATE_4 = new MinecraftVersion("1.16.4");
    /**
     * Version 1.16.2 - breaking change to the nether update
     */
    public static final MinecraftVersion NETHER_UPDATE_2 = new MinecraftVersion("1.16.2");
    /**
     * Version 1.16.0 - the nether update
     */
    public static final MinecraftVersion NETHER_UPDATE = new MinecraftVersion("1.16");
    /**
     * Version 1.15 - the bee update
     */
    public static final MinecraftVersion BEE_UPDATE = new MinecraftVersion("1.15");
    /**
     * Version 1.14 - village and pillage update.
     */
    public static final MinecraftVersion VILLAGE_UPDATE = new MinecraftVersion("1.14");
    /**
     * Version 1.13 - update aquatic.
     */
    public static final MinecraftVersion AQUATIC_UPDATE = new MinecraftVersion("1.13");
    /**
     * Version 1.12 - the world of color update.
     */
    public static final MinecraftVersion COLOR_UPDATE = new MinecraftVersion("1.12");
    /**
     * Version 1.11 - the exploration update.
     */
    public static final MinecraftVersion EXPLORATION_UPDATE = new MinecraftVersion("1.11");
    /**
     * Version 1.10 - the frostburn update.
     */
    public static final MinecraftVersion FROSTBURN_UPDATE = new MinecraftVersion("1.10");
    /**
     * Version 1.9 - the combat update.
     */
    public static final MinecraftVersion COMBAT_UPDATE = new MinecraftVersion("1.9");
    /**
     * Version 1.8 - the "bountiful" update.
     */
    public static final MinecraftVersion BOUNTIFUL_UPDATE = new MinecraftVersion("1.8");
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

    /**
     * The latest release version of minecraft.
     */
    public static final MinecraftVersion LATEST = v1_21_11;

    // used when serializing
    private static final long serialVersionUID = -8695133558996459770L;

    /**
     * Regular expression used to parse version strings.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-.]+).*");

    /**
     * The current version of minecraft, lazy initialized by MinecraftVersion.currentVersion()
     */
    private static MinecraftVersion currentVersion;

    private final int major;
    private final int minor;
    private final int build;
    // The development stage
    private final String development;

    // Snapshot?
    private final SnapshotVersion snapshot;
    private volatile Boolean atCurrentOrAbove;

    /**
     * Determine the current Minecraft version.
     *
     * @param server - the Bukkit server that will be used to examine the MC version.
     */
    public MinecraftVersion(Server server) {
        this(extractVersion(server.getVersion()));
    }

    /**
     * Construct a version object from the format major.minor.build, or the snapshot format.
     *
     * @param versionOnly - the version in text form.
     */
    public MinecraftVersion(String versionOnly) {
        this(versionOnly, true);
    }

    /**
     * Construct a version format from the standard release version or the snapshot verison.
     *
     * @param versionOnly   - the version.
     * @param parseSnapshot - TRUE to parse the snapshot, FALSE otherwise.
     */
    private MinecraftVersion(String versionOnly, boolean parseSnapshot) {
        String[] section = versionOnly.split("-");
        SnapshotVersion snapshot = null;
        int[] numbers = new int[3];

        try {
            numbers = this.parseVersion(section[0]);
        } catch (NumberFormatException cause) {
            // Skip snapshot parsing
            if (!parseSnapshot) {
                throw cause;
            }

            try {
                // Determine if the snapshot is newer than the current release version
                snapshot = new SnapshotVersion(section[0]);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                MinecraftVersion latest = new MinecraftVersion(ProtocolLibrary.MAXIMUM_MINECRAFT_VERSION, false);
                boolean newer = snapshot.getSnapshotDate().compareTo(
                        format.parse(ProtocolLibrary.MINECRAFT_LAST_RELEASE_DATE)) > 0;

                numbers[0] = latest.getMajor();
                numbers[1] = latest.getMinor() + (newer ? 1 : -1);
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
     *
     * @param major - major version number.
     * @param minor - minor version number.
     * @param build - build version number.
     */
    public MinecraftVersion(int major, int minor, int build) {
        this(major, minor, build, null);
    }

    /**
     * Construct a version object directly.
     *
     * @param major       - major version number.
     * @param minor       - minor version number.
     * @param build       - build version number.
     * @param development - development stage.
     */
    public MinecraftVersion(int major, int minor, int build, String development) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.development = development;
        this.snapshot = null;
    }

    /**
     * Extract the Minecraft version from CraftBukkit itself.
     *
     * @param text - the server version in text form.
     * @return The underlying MC version.
     * @throws IllegalStateException If we could not parse the version string.
     */
    public static String extractVersion(String text) {
        Matcher version = VERSION_PATTERN.matcher(text);

        if (version.matches() && version.group(1) != null) {
            return version.group(1);
        } else {
            throw new IllegalStateException("Cannot parse version String '" + text + "'");
        }
    }

    /**
     * Parse the given server version into a Minecraft version.
     *
     * @param serverVersion - the server version.
     * @return The resulting Minecraft version.
     */
    public static MinecraftVersion fromServerVersion(String serverVersion) {
        return new MinecraftVersion(extractVersion(serverVersion));
    }

    public static MinecraftVersion getCurrentVersion() {
        if (currentVersion == null) {
            currentVersion = fromServerVersion(Bukkit.getVersion());
        }

        return currentVersion;
    }

    public static void setCurrentVersion(MinecraftVersion version) {
        currentVersion = version;
    }

    private static boolean atOrAbove(MinecraftVersion version) {
        return getCurrentVersion().isAtLeast(version);
    }

    private int[] parseVersion(String version) {
        String[] elements = version.split("\\.");
        int[] numbers = new int[3];

        // Make sure it's even a valid version
        if (elements.length < 1) {
            throw new IllegalStateException("Corrupt MC version: " + version);
        }

        // The String 1 or 1.2 is interpreted as 1.0.0 and 1.2.0 respectively.
        for (int i = 0; i < Math.min(numbers.length, elements.length); i++) {
            numbers[i] = Integer.parseInt(elements[i].trim());
        }
        return numbers;
    }

    /**
     * Major version number
     *
     * @return Current major version number.
     */
    public int getMajor() {
        return this.major;
    }

    /**
     * Minor version number
     *
     * @return Current minor version number.
     */
    public int getMinor() {
        return this.minor;
    }

    /**
     * Build version number
     *
     * @return Current build version number.
     */
    public int getBuild() {
        return this.build;
    }

    /**
     * Retrieve the development stage.
     *
     * @return Development stage, or NULL if this is a release.
     */
    public String getDevelopmentStage() {
        return this.development;
    }

    /**
     * Retrieve the snapshot version, or NULL if this is a release.
     *
     * @return The snapshot version.
     */
    public SnapshotVersion getSnapshot() {
        return this.snapshot;
    }

    /**
     * Determine if this version is a snapshot.
     *
     * @return The snapshot version.
     */
    public boolean isSnapshot() {
        return this.snapshot != null;
    }

    /**
     * Checks if this version is at or above the current version the server is running.
     *
     * @return true if this version is equal or newer than the server version, false otherwise.
     */
    public boolean atOrAbove() {
        if (this.atCurrentOrAbove == null) {
            this.atCurrentOrAbove = atOrAbove(this);
        }

        return this.atCurrentOrAbove;
    }

    /**
     * Retrieve the version String (major.minor.build) only.
     *
     * @return A normal version string.
     */
    public String getVersion() {
        if (this.getDevelopmentStage() == null) {
            return String.format("%s.%s.%s", this.getMajor(), this.getMinor(), this.getBuild());
        } else {
            return String.format("%s.%s.%s-%s%s", this.getMajor(), this.getMinor(), this.getBuild(),
                    this.getDevelopmentStage(), this.isSnapshot() ? this.snapshot : "");
        }
    }

    @Override
    public int compareTo(MinecraftVersion o) {
        if (o == null) {
            return 1;
        }

        return ComparisonChain.start()
                .compare(this.getMajor(), o.getMajor())
                .compare(this.getMinor(), o.getMinor())
                .compare(this.getBuild(), o.getBuild())
                .compare(this.getDevelopmentStage(), o.getDevelopmentStage(), Ordering.natural().nullsLast())
                .compare(this.getSnapshot(), o.getSnapshot(), Ordering.natural().nullsFirst())
                .result();
    }

    public boolean isAtLeast(MinecraftVersion other) {
        if (other == null) {
            return false;
        }

        return this.compareTo(other) >= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        if (obj instanceof MinecraftVersion) {
            MinecraftVersion other = (MinecraftVersion) obj;

            return this.getMajor() == other.getMajor() &&
                    this.getMinor() == other.getMinor() &&
                    this.getBuild() == other.getBuild() &&
                    Objects.equals(this.getDevelopmentStage(), other.getDevelopmentStage());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getMajor(), this.getMinor(), this.getBuild());
    }

    @Override
    public String toString() {
        // Convert to a String that we can parse back again
        return String.format("(MC: %s)", this.getVersion());
    }
}
