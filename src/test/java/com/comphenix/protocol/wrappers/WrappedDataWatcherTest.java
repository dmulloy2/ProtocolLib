/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2016 dmulloy2
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

import java.util.List;
import java.util.Optional;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftEgg;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author dmulloy2
 */
public class WrappedDataWatcherTest {
    private static Entity mockEntity;

    @BeforeAll
    public static void prepare() {
        BukkitInitialization.initializeAll();

        ThrownEgg nmsEgg = new ThrownEgg(null, 0, 0, 0, net.minecraft.world.item.ItemStack.EMPTY);
        mockEntity = new CraftEgg(null, nmsEgg);
    }

    @Test
    public void testFromEntity() {
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mockEntity);

        WrappedWatchableObject watchable = watcher.getWatchableObject(0);
        WrappedDataWatcherObject object = watchable.getWatcherObject();

        // Make sure the serializers work
        assertEquals(object.getSerializer(), Registry.get(Byte.class));

        // Make sure we can set existing objects
        watcher.setByte(0, (byte) 21, false);
        assertEquals((byte) 21, watcher.getByte(0));

        assertTrue(watcher.hasIndex(1));
        assertFalse(watcher.hasIndex(9999));
    }

    @Test
    public void testPrimitives() {
        WrappedDataWatcher watcher = new WrappedDataWatcher();

        watcher.setByte(0, (byte) 21, false);
        assertEquals((byte) 21, watcher.getByte(0));

        watcher.setInteger(1, 37, false);
        assertEquals(37, watcher.getInteger(1));

        watcher.setFloat(2, 42.1F, false);
        assertEquals(42.1F, watcher.getFloat(2));

        watcher.setLong(3, 69L, false);
        assertEquals(69L, watcher.getLong(3));

        watcher.setBoolean(4, true, false);
        assertTrue(watcher.getBoolean(4));

        assertTrue(watcher.hasIndex(4));
        assertFalse(watcher.hasIndex(5));
        assertEquals(5, watcher.size());

        List<WrappedDataValue> dataValues = watcher.toDataValueCollection();
        assertEquals(5, dataValues.size());

        assertEquals((byte) 21, dataValues.get(0).getValue());
        assertEquals((byte) 21, dataValues.get(0).getRawValue());

        assertEquals(37, dataValues.get(1).getValue());
        assertEquals(37, dataValues.get(1).getRawValue());

        assertEquals(42.1F, dataValues.get(2).getValue());
        assertEquals(42.1F, dataValues.get(2).getRawValue());

        assertEquals(69L, dataValues.get(3).getValue());
        assertEquals(69L, dataValues.get(3).getRawValue());

        assertEquals(true, dataValues.get(4).getValue());
        assertEquals(true, dataValues.get(4).getRawValue());
    }

    @Test
    public void testStrings() {
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mockEntity);

        watcher.setString(0, "Test", false);

        assertEquals("Test", watcher.getString(0));

        WrappedDataValue wdv = watcher.toDataValueCollection().get(0);
        assertEquals("Test", wdv.getValue());
        assertEquals("Test", wdv.getRawValue());

        assertEquals(0, wdv.getIndex());
        assertNotNull(wdv.getSerializer());
        assertFalse(wdv.getSerializer().isOptional());
    }

    @Test
    public void testChatComponent() {
        WrappedDataWatcher wrapper = new WrappedDataWatcher();
        wrapper.setChatComponent(0, WrappedChatComponent.fromText("Test"), false);
        wrapper.setOptionalChatComponent(1, Optional.of(WrappedChatComponent.fromText("Test")), false);

        WrappedChatComponent expected = WrappedChatComponent.fromText("Test");
        assertEquals(expected, wrapper.getChatComponent(0));
        assertTrue(wrapper.getOptionalChatComponent(1).isPresent());
        assertEquals(expected, wrapper.getOptionalChatComponent(1).get());

        List<WrappedDataValue> wdvs = wrapper.toDataValueCollection();
        WrappedDataValue wdv1 = wdvs.get(0);
        assertEquals(expected, wdv1.getValue());

        WrappedDataValue wdv2 = wdvs.get(1);
        assertEquals(expected, ((Optional) wdv2.getValue()).get());

        assertEquals("literal{Test}", wdv1.getRawValue().toString());
    }

    @Test
    public void testItemStacks() {
        WrappedDataWatcher wrapper = new WrappedDataWatcher();
        wrapper.setItemStack(0, new ItemStack(Material.ACACIA_FENCE), false);
        assertEquals(Material.ACACIA_FENCE, wrapper.getItemStack(0).getType());

        assertEquals(MinecraftReflection.getItemStackClass(),
            wrapper.getWatchableObject(0).getRawValue().getClass());

        WrappedDataValue wdv = wrapper.toDataValueCollection().get(0);
        assertEquals(Material.ACACIA_FENCE, ((ItemStack) wdv.getValue()).getType());

        Object raw = wdv.getRawValue();
        assertEquals(MinecraftReflection.getItemStackClass(), raw.getClass());
    }

    @Test
    public void testMinecraftObjects() {
        WrappedDataWatcher watcher = new WrappedDataWatcher();

        watcher.setPosition(0, new BlockPosition(1, 2, 3), false);
        assertEquals(new BlockPosition(1, 2, 3), watcher.getPosition(0));

        watcher.setOptionalPosition(1, Optional.of(new BlockPosition(4, 5, 6)), false);
        assertEquals(Optional.of(new BlockPosition(4, 5, 6)), watcher.getOptionalPosition(1));

        watcher.setDirection(2, EnumWrappers.Direction.EAST, false);
        assertEquals(EnumWrappers.Direction.EAST, watcher.getDirection(2));

        watcher.setBlockState(3, WrappedBlockData.createData(Material.ACACIA_FENCE), false);
        assertEquals(Material.ACACIA_FENCE, watcher.getBlockState(3).getType());

        watcher.setOptionalBlockState(4, Optional.of(WrappedBlockData.createData(Material.ACACIA_FENCE)), false);
        assertEquals(Optional.of(WrappedBlockData.createData(Material.ACACIA_FENCE)), watcher.getOptionalBlockState(4));

        watcher.setParticle(5, WrappedParticle.create(Particle.CAMPFIRE_COSY_SMOKE, null), false);
        assertEquals(Particle.CAMPFIRE_COSY_SMOKE, watcher.getParticle(5).getParticle());

        watcher.setParticle(6, WrappedParticle.create(Particle.BLOCK, WrappedBlockData.createData(Material.AZALEA)), false);
        assertEquals(Material.AZALEA, ((WrappedParticle<WrappedBlockData>) watcher.getParticle(6)).getData().getType());

        assertEquals(7, watcher.size());

        List<WrappedDataValue> wdvs = watcher.toDataValueCollection();
        assertEquals(7, wdvs.size());

        assertEquals(new BlockPosition(1, 2, 3), wdvs.get(0).getValue());
        assertEquals(Optional.of(new BlockPosition(4, 5, 6)), wdvs.get(1).getValue());
        assertEquals(EnumWrappers.Direction.EAST, wdvs.get(2).getValue());
        assertEquals(Material.ACACIA_FENCE, ((WrappedBlockData) wdvs.get(3).getValue()).getType());
        assertEquals(Particle.CAMPFIRE_COSY_SMOKE, ((WrappedParticle) wdvs.get(5).getValue()).getParticle());
    }

    // @Test
    // TODO: CompoundTag has been removed as a data watcher type (replaced with data components)
    public void testNBT() {
        WrappedDataWatcher watcher = new WrappedDataWatcher();

        NbtBase<Integer> nbt = NbtFactory.of("testTag", 17);
        NbtCompound compound = NbtFactory.ofCompound("testCompound", List.of(nbt));
        watcher.setNBTCompound(0, compound, false);

        NbtCompound roundTrip = watcher.getNBTCompound(0);
        assertEquals(17, roundTrip.getInteger("testTag"));

        List<WrappedDataValue> wdvs = watcher.toDataValueCollection();
        Object rawValue = wdvs.get(0).getRawValue();
        assertEquals(MinecraftReflection.getNBTCompoundClass(), rawValue.getClass());
    }

    @Test
    public void testDeepClone() {
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mockEntity);
        WrappedDataWatcher cloned = watcher.deepClone();

        assertEquals(watcher.size(), cloned.size());

        int size = watcher.size();
        for (int i = 0; i < size; i++) {
            assertEquals(watcher.getObject(i), cloned.getObject(i));
        }
    }
}
