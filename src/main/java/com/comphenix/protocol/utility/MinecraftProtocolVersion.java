package com.comphenix.protocol.utility;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A lookup of the associated protocol version of a given Minecraft server.
 *
 * @author Kristian
 */
public final class MinecraftProtocolVersion {

    private static final NavigableMap<MinecraftVersion, Integer> LOOKUP = createLookup();

    private static NavigableMap<MinecraftVersion, Integer> createLookup() {
        TreeMap<MinecraftVersion, Integer> map = new TreeMap<>();

        // Source: http://wiki.vg/Protocol_version_numbers
        // Doesn't include pre-releases
        map.put(new MinecraftVersion(1, 0, 0), 22);
        map.put(new MinecraftVersion(1, 1, 0), 23);
        map.put(new MinecraftVersion(1, 2, 2), 28);
        map.put(new MinecraftVersion(1, 2, 4), 29);
        map.put(new MinecraftVersion(1, 3, 1), 39);
        map.put(new MinecraftVersion(1, 4, 2), 47);
        map.put(new MinecraftVersion(1, 4, 3), 48);
        map.put(new MinecraftVersion(1, 4, 4), 49);
        map.put(new MinecraftVersion(1, 4, 6), 51);
        map.put(new MinecraftVersion(1, 5, 0), 60);
        map.put(new MinecraftVersion(1, 5, 2), 61);
        map.put(new MinecraftVersion(1, 6, 0), 72);
        map.put(new MinecraftVersion(1, 6, 1), 73);
        map.put(new MinecraftVersion(1, 6, 2), 74);
        map.put(new MinecraftVersion(1, 6, 4), 78);

        // After Netty
        map.put(new MinecraftVersion(1, 7, 1), 4);
        map.put(new MinecraftVersion(1, 7, 6), 5);

        map.put(new MinecraftVersion(1, 8, 0), 47);

        map.put(new MinecraftVersion(1, 9, 0), 107);
        map.put(new MinecraftVersion(1, 9, 2), 109);
        map.put(new MinecraftVersion(1, 9, 4), 110);

        map.put(new MinecraftVersion(1, 10, 0), 210);

        map.put(new MinecraftVersion(1, 11, 0), 315);
        map.put(new MinecraftVersion(1, 11, 1), 316);

        map.put(new MinecraftVersion(1, 12, 0), 335);
        map.put(new MinecraftVersion(1, 12, 1), 338);
        map.put(new MinecraftVersion(1, 12, 2), 340);

        map.put(new MinecraftVersion(1, 13, 0), 393);
        map.put(new MinecraftVersion(1, 13, 1), 401);
        map.put(new MinecraftVersion(1, 13, 2), 404);

        map.put(new MinecraftVersion(1, 14, 0), 477);
        map.put(new MinecraftVersion(1, 14, 1), 480);
        map.put(new MinecraftVersion(1, 14, 2), 485);
        map.put(new MinecraftVersion(1, 14, 3), 490);
        map.put(new MinecraftVersion(1, 14, 4), 498);

        map.put(new MinecraftVersion(1, 15, 0), 573);
        map.put(new MinecraftVersion(1, 15, 1), 575);
        map.put(new MinecraftVersion(1, 15, 2), 578);

        map.put(new MinecraftVersion(1, 16, 0), 735);
        map.put(new MinecraftVersion(1, 16, 1), 736);
        map.put(new MinecraftVersion(1, 16, 2), 751);
        map.put(new MinecraftVersion(1, 16, 3), 753);
        map.put(new MinecraftVersion(1, 16, 4), 754);
        map.put(new MinecraftVersion(1, 16, 5), 754);

        map.put(new MinecraftVersion(1, 17, 0), 755);
        map.put(new MinecraftVersion(1, 17, 1), 756);

        map.put(new MinecraftVersion(1, 18, 0), 757);
        map.put(new MinecraftVersion(1, 18, 1), 757);
        map.put(new MinecraftVersion(1, 18, 2), 758);

        map.put(new MinecraftVersion(1, 19, 0), 759);
        map.put(new MinecraftVersion(1, 19, 1), 760);
        map.put(new MinecraftVersion(1, 19, 2), 760);
        map.put(new MinecraftVersion(1, 19, 3), 761);
        map.put(new MinecraftVersion(1, 19, 4), 762);

        map.put(new MinecraftVersion(1, 20, 0), 763);
        map.put(new MinecraftVersion(1, 20, 2), 764);
        map.put(new MinecraftVersion(1, 20, 3), 765);
        map.put(new MinecraftVersion(1, 20, 5), 766);

        map.put(new MinecraftVersion(1, 21, 0), 767);
        map.put(new MinecraftVersion(1, 21, 2), 768);
        map.put(new MinecraftVersion(1, 21, 6), 771);
        map.put(new MinecraftVersion(1, 21, 8), 772);
        map.put(new MinecraftVersion(1, 21, 9), 773);
        return map;
    }

    /**
     * Retrieve the version of the Minecraft protocol for the current version of Minecraft.
     *
     * @return The version number.
     */
    public static int getCurrentVersion() {
        return getVersion(MinecraftVersion.getCurrentVersion());
    }

    /**
     * Retrieve the version of the Minecraft protocol for this version of Minecraft.
     *
     * @param version - the version.
     * @return The version number.
     */
    public static int getVersion(MinecraftVersion version) {
        Entry<MinecraftVersion, Integer> result = LOOKUP.floorEntry(version);
        return result != null ? result.getValue() : Integer.MIN_VALUE;
    }
}
