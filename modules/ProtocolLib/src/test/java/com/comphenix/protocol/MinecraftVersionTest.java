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

package com.comphenix.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.comphenix.protocol.utility.MinecraftVersion;

public class MinecraftVersionTest {
	@Test
	public void testComparision() {
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
	
	/* @Test
	public void testSnapshotVersion() {
		MinecraftVersion version = MinecraftVersion.fromServerVersion("git-Spigot-1119 (MC: 13w39b)");
		assertEquals(version.getSnapshot(), new SnapshotVersion("13w39b"));
	} */

	@Test
	public void testParsing() {
		assertEquals(MinecraftVersion.extractVersion("CraftBukkit R3.0 (MC: 1.4.3)"), "1.4.3");
		assertEquals(MinecraftVersion.extractVersion("CraftBukkit Test Beta 1 (MC: 1.10.01 )"), "1.10.01");
		assertEquals(MinecraftVersion.extractVersion("Hello (MC: 2.3.4)"), "2.3.4");
		
		assertEquals(MinecraftVersion.fromServerVersion("git-Cauldron-Reloaded-1.7.10-1.1388.1.0 (MC: 1.7.10)"), new MinecraftVersion(1, 7, 10));
		assertEquals(MinecraftVersion.fromServerVersion("git-Bukkit-18fbb24 (MC: 1.8.8)"), new MinecraftVersion(1, 8, 8));
	}
}
