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

package com.comphenix.protocol.wrappers.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NbtFactoryTest {

    @BeforeAll
    public static void initializeBukkit() throws IllegalAccessException {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testFromStream() {
        WrappedCompound compound = WrappedCompound.fromName("tag");
        compound.put("name", "Test Testerson");
        compound.put("age", 42);

        compound.put(NbtFactory.ofList("nicknames", "a", "b", "c"));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutput test = new DataOutputStream(buffer);
        compound.write(test);

        ByteArrayInputStream source = new ByteArrayInputStream(buffer.toByteArray());
        DataInput input = new DataInputStream(source);

        NbtCompound cloned = NbtBinarySerializer.DEFAULT.deserializeCompound(input);

        assertEquals(compound.getString("name"), cloned.getString("name"));
        assertEquals(compound.getInteger("age"), cloned.getInteger("age"));
        assertEquals(compound.getList("nicknames"), cloned.getList("nicknames"));
    }

    @Test
    @Disabled // TODO
    public void testItemTag() {
        ItemStack test = new ItemStack(Items.L);
        org.bukkit.inventory.ItemStack craftTest = MinecraftReflection.getBukkitItemStack(test);

        NbtCompound compound = NbtFactory.ofCompound("tag");
        compound.put("name", "Test Testerson");
        compound.put("age", 42);

        NbtFactory.setItemTag(craftTest, compound);

        assertEquals(compound, NbtFactory.fromItemTag(craftTest));
    }

    @Test
    public void testCreateTags() {
        for (NbtType type : NbtType.values()) {
            if (type != NbtType.TAG_END) {
                NbtFactory.ofWrapper(type, "");
            }
        }
    }
}
