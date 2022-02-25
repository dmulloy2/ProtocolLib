/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2015 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * @author dmulloy2
 */
public class MultiBlockChangeTest {

	// @BeforeAll
	public static void initializeBukkit() {
		BukkitInitialization.initializeAll();
	}

	// @Test
	public void test() {
		int x = 42;
		int y = 64;
		int z = 70;

		Location loc = new Location(null, x, y, z);
		ChunkCoordIntPair chunk = new ChunkCoordIntPair(x >> 4, z >> 4);
		WrappedBlockData blockData = WrappedBlockData.createData(Material.STONE);
		MultiBlockChangeInfo info = new MultiBlockChangeInfo(loc, blockData);

		// Make sure the location is correct
		assertEquals(loc, info.getLocation(null));

		MultiBlockChangeInfo[] array = {info, info};

		EquivalentConverter<MultiBlockChangeInfo[]> converter = Converters.array(
				MinecraftReflection.getMultiBlockChangeInfoClass(),
				MultiBlockChangeInfo.getConverter(chunk)
		);
		Object generic = converter.getGeneric(array);
		MultiBlockChangeInfo[] back = converter.getSpecific(generic);

		// Make sure our conversions are correct
		assertEquals(info.getX(), back[0].getX());
		assertEquals(info.getY(), back[0].getY());
		assertEquals(info.getZ(), back[0].getZ());
		assertEquals(info.getData(), back[0].getData());
	}
}
