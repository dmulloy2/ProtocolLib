/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
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
package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertEquals;

import org.bukkit.Material;
import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * @author dmulloy2
 */

public class MultiBlockChangeTest {

	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}

	@Test
	public void test() {
		int x = 10;
		int y = 128;
		int z = 7;

		short location = (short) (x << 12 | z << 8 | y);

		ChunkCoordIntPair chunk = new ChunkCoordIntPair(1, 2);
		WrappedBlockData blockData = WrappedBlockData.createData(Material.STONE);
		MultiBlockChangeInfo info = new MultiBlockChangeInfo(location, blockData, chunk);

		Object generic = MultiBlockChangeInfo.getConverter(chunk).getGeneric(MinecraftReflection.getMultiBlockChangeInfoClass(), info);
		MultiBlockChangeInfo back = MultiBlockChangeInfo.getConverter(chunk).getSpecific(generic);

		assertEquals(info.getX(), back.getX());
		assertEquals(info.getY(), back.getY());
		assertEquals(info.getZ(), back.getZ());
		assertEquals(info.getData(), back.getData());
	}
}