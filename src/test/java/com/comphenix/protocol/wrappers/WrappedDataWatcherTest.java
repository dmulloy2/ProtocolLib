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

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import net.minecraft.world.entity.projectile.EntityEgg;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEgg;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author dmulloy2
 */
public class WrappedDataWatcherTest {

    @BeforeAll
    public static void prepare() {
        BukkitInitialization.initializeAll();
    }

    @Test
    @Disabled // TODO -- need to fix data watchers
    public void testBytes() {
        // Create a fake lightning strike and get its watcher
        EntityEgg nmsEgg = new EntityEgg(null, 0, 0, 0);
        CraftEntity craftEgg = new CraftEgg(null, nmsEgg);
        WrappedDataWatcher wrapper = WrappedDataWatcher.getEntityWatcher(craftEgg);

        WrappedWatchableObject watchable = wrapper.getWatchableObject(0);
        WrappedDataWatcherObject object = watchable.getWatcherObject();

        // Make sure the serializers work
        assertEquals(object.getSerializer(), Registry.get(Byte.class));

        // Make sure we can set existing objects
        wrapper.setObject(0, (byte) 21);
        assertEquals(21, (byte) wrapper.getByte(0));
    }

    @Test
    @Disabled // TODO -- need to fix data watchers
    public void testStrings() {
        WrappedDataWatcher wrapper = new WrappedDataWatcher();

        // Make sure we can create watcher objects
        Serializer serializer = Registry.get(String.class);
        WrappedDataWatcherObject object = new WrappedDataWatcherObject(3, serializer);
        wrapper.setObject(object, "Test");

        assertEquals(wrapper.getString(3), "Test");
    }

    @Test
    @Disabled // TODO -- need to fix data watchers
    public void testFloats() {
        WrappedDataWatcher wrapper = new WrappedDataWatcher();

        // Make sure we can add new entries
        Serializer serializer = Registry.get(Float.class);
        WrappedDataWatcherObject object = new WrappedDataWatcherObject(10, serializer);
        wrapper.setObject(object, 21.0F);

        assertTrue(wrapper.hasIndex(10));
    }

    @Test
    @Disabled // TODO -- need to fix data watchers
    public void testSerializers() {
        Serializer blockPos = Registry.get(net.minecraft.core.BlockPosition.class, false);
        Serializer optionalBlockPos = Registry.get(net.minecraft.core.BlockPosition.class, true);
        assertNotSame(blockPos, optionalBlockPos);

        // assertNull(Registry.get(ItemStack.class, false));
        assertNotNull(Registry.get(UUID.class, true));
    }

    @Test
    @Disabled // TODO -- need to fix data watchers
    public void testHasIndex() {
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        Serializer serializer = Registry.get(Integer.class);

        assertFalse(watcher.hasIndex(0));
        watcher.setObject(0, serializer, 1);
        assertTrue(watcher.hasIndex(0));
    }

    @Test
    @Disabled // TODO -- need to fix data watchers
    public void testDeepClone() {
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(0, Registry.get(Integer.class), 1);

        WrappedDataWatcher cloned = watcher.deepClone();
        assertEquals(1, cloned.asMap().size());
        assertEquals(1, (Object) cloned.getInteger(0));
    }
}
