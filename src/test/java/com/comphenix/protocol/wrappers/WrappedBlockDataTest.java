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

import com.comphenix.protocol.BukkitInitialization;

import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.craftbukkit.v1_21_R7.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R7.block.impl.CraftStainedGlassPane;
import org.bukkit.craftbukkit.v1_21_R7.util.CraftMagicNumbers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author dmulloy2
 */

public class WrappedBlockDataTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testMaterialCreation() {
        Material type = Material.BLUE_WOOL;

        WrappedBlockData wrapper = WrappedBlockData.createData(type);

        assertEquals(wrapper.getType(), type);
        //assertEquals(wrapper.getData(), data);

        Object generic = BukkitConverters.getWrappedBlockDataConverter().getGeneric(wrapper);
        WrappedBlockData back = BukkitConverters.getWrappedBlockDataConverter().getSpecific(generic);

        assertEquals(wrapper.getType(), back.getType());
        assertEquals(wrapper.getData(), back.getData());
    }

    @Test
    public void testDataCreation() {
        BlockState nmsData = CraftMagicNumbers.getBlock(Material.CYAN_STAINED_GLASS_PANE).defaultBlockState();
        GlassPane data = (GlassPane) CraftBlockData.fromData(nmsData);
        data.setFace(BlockFace.EAST, true);

        WrappedBlockData wrapper = WrappedBlockData.createData(data);
        assertEquals(wrapper.getType(), Material.CYAN_STAINED_GLASS_PANE);

        GlassPane back = new CraftStainedGlassPane((BlockState) wrapper.getHandle());
        assertEquals(back.hasFace(BlockFace.EAST), data.hasFace(BlockFace.EAST));
        assertEquals(back.hasFace(BlockFace.SOUTH), data.hasFace(BlockFace.SOUTH));
    }
}
