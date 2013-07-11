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

package com.comphenix.protocol.events;

import static org.junit.Assert.*;
import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_6_R2.AttributeModifier;
import net.minecraft.server.v1_6_R2.AttributeSnapshot;
import net.minecraft.server.v1_6_R2.Packet44UpdateAttributes;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
// Will have to be updated for every version though
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemFactory;

import org.bukkit.Material;
import org.bukkit.WorldType;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

// Ensure that the CraftItemFactory is mockable
@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest(CraftItemFactory.class)
public class PacketContainerTest {
	// Helper converters
	private EquivalentConverter<WrappedDataWatcher> watchConvert = BukkitConverters.getDataWatcherConverter();
	private EquivalentConverter<ItemStack> itemConvert = BukkitConverters.getItemStackConverter();
	
	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializeItemMeta();
	}
	
	private <T> void testPrimitive(StructureModifier<T> modifier, int index, T initialValue, T testValue) {
		// Check initial value
		assertEquals(initialValue, modifier.read(index));

		// Test assignment
		modifier.write(index, testValue);
		assertEquals(testValue, modifier.read(0));
	}
	
	private <T> void testObjectArray(StructureModifier<T[]> modifier, int index, T[] initialValue, T[] testValue) {
		// Check initial value
		assertNull(modifier.read(index));
		modifier.writeDefaults();

		// Test initial
		assertArrayEquals(initialValue, modifier.read(index));
		
		// Test assignment
		modifier.write(index, testValue);
		assertArrayEquals(testValue, modifier.read(0));
	}

	@Test
	public void testGetByteArrays() {
		// Contains a byte array we will test
		PacketContainer customPayload = new PacketContainer(Packets.Server.CUSTOM_PAYLOAD);
		StructureModifier<byte[]> bytes = customPayload.getByteArrays();
		byte[] testArray = new byte[] { 1, 2, 3 };
		
		// It's NULL at first
		assertArrayEquals(null, (byte[]) bytes.read(0));
		customPayload.getModifier().writeDefaults();
		
		// Then it should create an empty array
		assertArrayEquals(new byte[0], (byte[]) bytes.read(0));
		
		// Check and see if we can write to it
		bytes.write(0, testArray);
		assertArrayEquals(testArray, (byte[]) bytes.read(0));
	}
	
	@Test
	public void testGetBytes() {
		PacketContainer spawnMob = new PacketContainer(Packets.Server.MOB_SPAWN);
		testPrimitive(spawnMob.getBytes(), 0, (byte)0, (byte)1);
	}
	
	@Test
	public void testGetShorts() {
		PacketContainer itemData = new PacketContainer(Packets.Server.ITEM_DATA);
		testPrimitive(itemData.getShorts(), 0, (short)0, (short)1);
	}

	@Test
	public void testGetIntegers() {
		PacketContainer updateSign = new PacketContainer(Packets.Server.UPDATE_SIGN);
		testPrimitive(updateSign.getIntegers(), 0, (int)0, (int)1);
	}

	@Test
	public void testGetLongs() {
		PacketContainer updateTime = new PacketContainer(Packets.Server.UPDATE_TIME);
		testPrimitive(updateTime.getLongs(), 0, (long)0, (long)1);
	}

	@Test
	public void testGetFloat() {
		PacketContainer explosion = new PacketContainer(Packets.Server.EXPLOSION);
		testPrimitive(explosion.getFloat(), 0, (float)0, (float)0.8);
	}

	@Test
	public void testGetDoubles() {
		PacketContainer explosion = new PacketContainer(Packets.Server.EXPLOSION);
		testPrimitive(explosion.getDoubles(), 0, (double)0, (double)0.8);
	}

	@Test
	public void testGetStrings() {
		PacketContainer explosion = new PacketContainer(Packets.Server.CHAT);
		testPrimitive(explosion.getStrings(), 0, null, "hello");
	}

	@Test
	public void testGetStringArrays() {
		PacketContainer explosion = new PacketContainer(Packets.Server.UPDATE_SIGN);
		testObjectArray(explosion.getStringArrays(), 0, new String[0], new String[] { "hello", "world" });
	}

	@Test
	public void testGetIntegerArrays() {
		// Contains a byte array we will test
		PacketContainer mapChunkBulk = new PacketContainer(Packets.Server.MAP_CHUNK_BULK);
		StructureModifier<int[]> integers = mapChunkBulk.getIntegerArrays();
		int[] testArray = new int[] { 1, 2, 3 };
		
		// Pre and post conditions
		assertArrayEquals(null, (int[]) integers.read(0));
		mapChunkBulk.getModifier().writeDefaults();
		assertArrayEquals(new int[0], (int[]) integers.read(0));
		
		integers.write(0, testArray);
		assertArrayEquals(testArray, (int[]) integers.read(0));
	}

	@Test
	public void testGetItemModifier() {
		PacketContainer windowClick = new PacketContainer(Packets.Client.WINDOW_CLICK);
		
		StructureModifier<ItemStack> items = windowClick.getItemModifier();
		ItemStack goldAxe = new ItemStack(Material.GOLD_AXE);
		
		assertNull(items.read(0));
		
		// Insert the goldaxe and check if it's there
		items.write(0, goldAxe);
		assertTrue(equivalentItem(goldAxe, items.read(0)));
	}
 
	@Test
	public void testGetItemArrayModifier() {
		PacketContainer windowItems = new PacketContainer(Packets.Server.WINDOW_ITEMS);
		StructureModifier<ItemStack[]> itemAccess = windowItems.getItemArrayModifier();
		
		ItemStack[] itemArray = new ItemStack[] { 
				new ItemStack(Material.GOLD_AXE),
				new ItemStack(Material.DIAMOND_AXE)
		};
		
		assertNull(itemAccess.read(0));
		
		// Insert and check that it was succesful
		itemAccess.write(0, itemArray);
		
		// Read back array
		ItemStack[] comparision = itemAccess.read(0);
		assertEquals(itemArray.length, comparision.length);
		
		// Check that it is equivalent
		for (int i = 0; i < itemArray.length; i++) {
			assertTrue(String.format("Array element %s is not the same: %s != %s",
						i, itemArray[i], comparision[i]), equivalentItem(itemArray[i], comparision[i]));
		}
	}
	
	private boolean equivalentItem(ItemStack first, ItemStack second) {
		if (first == null) {
			return second == null;
		} else if (second == null) {
			return false;
		} else {
			return first.getType().equals(second.getType());
		}
	}
	
	@Test
	public void testGetWorldTypeModifier() {
		PacketContainer loginPacket = new PacketContainer(Packets.Server.LOGIN);
		StructureModifier<WorldType> worldAccess = loginPacket.getWorldTypeModifier();
		
		WorldType testValue = WorldType.LARGE_BIOMES;
		
		assertNull(worldAccess.read(0));
		
		// Insert and read back
		worldAccess.write(0, testValue);
		assertEquals(testValue, worldAccess.read(0));
	}

	@Test
	public void testGetNbtModifier() {
		PacketContainer updateTileEntity = new PacketContainer(132);
		
		NbtCompound compound = NbtFactory.ofCompound("test");
		compound.put("test", "name");
		compound.put(NbtFactory.ofList("ages", 1, 2, 3));
		
		updateTileEntity.getNbtModifier().write(0, compound);
		
		NbtCompound result = (NbtCompound) updateTileEntity.getNbtModifier().read(0);
		
		assertEquals(compound.getString("test"), result.getString("test"));
		assertEquals(compound.getList("ages"), result.getList("ages"));
	}
	
	@Test
	public void testGetDataWatcherModifier() {
		PacketContainer mobSpawnPacket = new PacketContainer(Packets.Server.MOB_SPAWN);
		StructureModifier<WrappedDataWatcher> watcherAccessor = mobSpawnPacket.getDataWatcherModifier();
				
		WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
		dataWatcher.setObject(1, 100);
		dataWatcher.setObject(2, 125);
		
		assertNull(watcherAccessor.read(0));
		
		// Insert and read back
		watcherAccessor.write(0, dataWatcher);
		assertEquals(dataWatcher, watcherAccessor.read(0));
	}

	// Unfortunately, it might be too difficult to mock this one
	//
	//  @Test
	//  public void testGetEntityModifier() { } 

	// No packet expose this type directly.
	//
	//  @Test
	//  public void testGetPositionModifier() { }

	@Test
	public void testGetPositionCollectionModifier() {
		PacketContainer explosionPacket = new PacketContainer(Packets.Server.EXPLOSION);
		StructureModifier<List<ChunkPosition>> positionAccessor = explosionPacket.getPositionCollectionModifier();
		
		assertNull(positionAccessor.read(0));
		
		List<ChunkPosition> positions = Lists.newArrayList();
		positions.add(new ChunkPosition(1, 2, 3));
		positions.add(new ChunkPosition(3, 4, 5));
		
		// Insert and read back
		positionAccessor.write(0, positions);
		assertEquals(positions, positionAccessor.read(0));
	}

	@Test
	public void testGetWatchableCollectionModifier() {
		PacketContainer entityMetadata = new PacketContainer(Packets.Server.ENTITY_METADATA);
		StructureModifier<List<WrappedWatchableObject>> watchableAccessor = 
				entityMetadata.getWatchableCollectionModifier();
		
		assertNull(watchableAccessor.read(0));
		
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(1, 10);
		watcher.setObject(8, 10);
		
		List<WrappedWatchableObject> list = watcher.getWatchableObjects();
		
		// Insert and read back
		watchableAccessor.write(0, list);
		assertEquals(list, watchableAccessor.read(0));
	}
	
	@Test
	public void testSerialization() {
		PacketContainer chat = new PacketContainer(3);
		chat.getStrings().write(0, "Test");
		
		PacketContainer copy = (PacketContainer) SerializationUtils.clone(chat);
		
		assertEquals(3, copy.getID());
		assertEquals("Test", copy.getStrings().read(0));
	}
	
	@Test
	public void testAttributeList() {
		PacketContainer attribute = new PacketContainer(Packets.Server.UPDATE_ATTRIBUTES);
		attribute.getIntegers().write(0, 123); // Entity ID
		
		// Initialize some test data
		List<AttributeModifier> modifiers = Lists.newArrayList(
			new AttributeModifier(UUID.randomUUID(), "Unknown synced attribute modifier", 10, 0));
		AttributeSnapshot snapshot = new AttributeSnapshot(
				(Packet44UpdateAttributes) attribute.getHandle(), "generic.Maxhealth", 20.0, modifiers);
		
		attribute.getSpecificModifier(List.class).write(0, Lists.newArrayList(snapshot));
		PacketContainer cloned = attribute.deepClone();
		AttributeSnapshot clonedSnapshot = (AttributeSnapshot) cloned.getSpecificModifier(List.class).read(0).get(0);
		
		assertEquals(
				ToStringBuilder.reflectionToString(snapshot, ToStringStyle.SHORT_PREFIX_STYLE),
				ToStringBuilder.reflectionToString(clonedSnapshot, ToStringStyle.SHORT_PREFIX_STYLE));
	}

	
	
	@Test
	public void testDeepClone() {
		// Try constructing all the packets
		for (Integer id : Iterables.concat(
				Packets.getClientRegistry().values(), 
				Packets.getServerRegistry().values() )) {

			// Whether or not this packet has been registered
			boolean registered = Packets.Server.isSupported(id) || 
								 Packets.Client.isSupported(id);
			
			try {
				PacketContainer constructed = new PacketContainer(id);
			
				if (!registered) {
					fail("Expected IllegalArgumentException(Packet " + id + " not registered");
				}
					
				// Make sure these packets contains fields as well
				assertTrue("Constructed packet with no known fields (" + id + ")", 
						constructed.getModifier().size() > 0);
				
				// Initialize default values
				constructed.getModifier().writeDefaults();
				
				// Clone the packet
				PacketContainer cloned = constructed.deepClone();
				
				// Make sure they're equivalent
				StructureModifier<Object> firstMod = constructed.getModifier(), secondMod = cloned.getModifier();
				assertEquals(firstMod.size(), secondMod.size());

				// Make sure all the fields are equivalent
				for (int i = 0; i < firstMod.size(); i++) {
					if (firstMod.getField(i).getType().isArray())
						assertArrayEquals(getArray(firstMod.read(i)), getArray(secondMod.read(i)));
					else
						testEquality(firstMod.read(i), secondMod.read(i));
				}
				
			} catch (IllegalArgumentException e) {
				if (!registered) {
					// Check the same
					assertEquals(e.getMessage(), "The packet ID " + id + " is not registered.");
				} else {
					// Something is very wrong
					throw e;
				}
			}
		}
	}
	
	// Convert to objects that support equals()
	private void testEquality(Object a, Object b) {
		if (a != null && b != null) {
			if (MinecraftReflection.isDataWatcher(a)) {
				a = watchConvert.getSpecific(a);
				b = watchConvert.getSpecific(b);
			} else if (MinecraftReflection.isItemStack(a)) {
				a = itemConvert.getSpecific(a);
				b = itemConvert.getSpecific(b);
			}
		}
		
		assertEquals(a, b);
	}
	
	/**
	 * Get the underlying array as an object array.
	 * @param val - array wrapped as an Object.
	 * @return An object array.
	 */
	private Object[] getArray(Object val) {
		if (val instanceof Object[])
			return (Object[]) val;
		if (val == null)
			return null;

		int arrlength = Array.getLength(val);
		Object[] outputArray = new Object[arrlength];
 
		for (int i = 0; i < arrlength; ++i)
			outputArray[i] = Array.get(val, i);
		return outputArray;
	}
}
