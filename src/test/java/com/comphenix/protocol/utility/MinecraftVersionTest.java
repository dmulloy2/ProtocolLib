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

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinecraftVersionTest {
    @BeforeAll
    public static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testComparison() {
        MinecraftVersion within = new MinecraftVersion(1, 2, 5);
        MinecraftVersion outside = new MinecraftVersion(1, 7, 0);

        MinecraftVersion lower = new MinecraftVersion(1, 0, 0);
        MinecraftVersion highest = new MinecraftVersion(1, 4, 5);

        MinecraftVersion atLeast = new MinecraftVersion(1, 8, 8);

        // Make sure this is valid
        assertTrue(lower.compareTo(within) < 0 && within.compareTo(highest) < 0);
        assertFalse(outside.compareTo(within) < 0 && outside.compareTo(highest) < 0);
        assertTrue(atLeast.isAtLeast(MinecraftVersion.BOUNTIFUL_UPDATE));
    }

    @Test
    void testCurrent() {
        assertEquals(MinecraftVersion.LATEST, MinecraftVersion.getCurrentVersion());
    }

    @Test
    void testCurrentProtocol() {
        // MIN_VALUE is returned when the current version is not supported
        assertNotEquals(MinecraftProtocolVersion.getCurrentVersion(), Integer.MIN_VALUE);
    }

    @Test
    void testParsing() {
        assertEquals("1.4.3", MinecraftVersion.extractVersion("CraftBukkit R3.0 (MC: 1.4.3)"));
        assertEquals("1.10.01", MinecraftVersion.extractVersion("CraftBukkit Test Beta 1 (MC: 1.10.01 )"));
        assertEquals("2.3.4", MinecraftVersion.extractVersion("Hello (MC: 2.3.4)"));

        assertEquals(new MinecraftVersion(1, 7, 10), MinecraftVersion.fromServerVersion("git-Cauldron-Reloaded-1.7.10-1.1388.1.0 (MC: 1.7.10)"));
        assertEquals(new MinecraftVersion(1, 8, 8), MinecraftVersion.fromServerVersion("git-Bukkit-18fbb24 (MC: 1.8.8)"));
    }
}
