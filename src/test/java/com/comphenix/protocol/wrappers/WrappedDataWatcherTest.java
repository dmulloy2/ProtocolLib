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

import java.util.UUID;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

import net.minecraft.world.entity.projectile.EntityEgg;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEgg;
import org.bukkit.entity.Entity;
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

        EntityEgg nmsEgg = new EntityEgg(null, 0, 0, 0);
        mockEntity = new CraftEgg(null, nmsEgg);
    }

    @Test
    public void testBytes() {
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mockEntity);

        WrappedWatchableObject watchable = watcher.getWatchableObject(0);
        WrappedDataWatcherObject object = watchable.getWatcherObject();

        // Make sure the serializers work
        assertEquals(object.getSerializer(), Registry.get(Byte.class));

        // Make sure we can set existing objects
        watcher.setObject(0, (byte) 21);
        assertEquals(21, (byte) watcher.getByte(0));
    }

    @Test
    public void testStrings() {
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mockEntity);

        // Make sure we can create watcher objects
        Serializer serializer = Registry.get(String.class);
        WrappedDataWatcherObject object = new WrappedDataWatcherObject(3, serializer);
        watcher.setObject(object, "Test");

        assertEquals(watcher.getString(3), "Test");
    }

    @Test
    public void testFloats() {
        WrappedDataWatcher wrapper = new WrappedDataWatcher();

        // Make sure we can add new entries
        Serializer serializer = Registry.get(Float.class);
        WrappedDataWatcherObject object = new WrappedDataWatcherObject(10, serializer);
        wrapper.setObject(object, 21.0F);

        assertTrue(wrapper.hasIndex(10));
    }

    @Test
    public void testSerializers() {
        Serializer blockPos = Registry.get(net.minecraft.core.BlockPosition.class, false);
        Serializer optionalBlockPos = Registry.get(net.minecraft.core.BlockPosition.class, true);
        assertNotSame(blockPos, optionalBlockPos);

        // assertNull(Registry.get(ItemStack.class, false)); // TODO
        assertNotNull(Registry.get(UUID.class, true));
    }

    @Test
    public void testHasIndex() {
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mockEntity);
        assertTrue(watcher.hasIndex(1));
        assertFalse(watcher.hasIndex(9999));
    }

    @Test
    public void testDeepClone() {
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(mockEntity);
        WrappedDataWatcher cloned = watcher.deepClone();

        assertEquals(watcher.size(), cloned.size());
        assertEquals(watcher.getObject(1), cloned.getObject(1));
    }
}
