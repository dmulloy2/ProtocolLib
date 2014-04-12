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

import net.minecraft.server.v1_7_R3.AttributeModifier;
import net.minecraft.server.v1_7_R3.AttributeSnapshot;
import net.minecraft.server.v1_7_R3.PacketPlayOutUpdateAttributes;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
// Will have to be updated for every version though
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemFactory;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.WorldType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import com.google.common.collect.Lists;

// Ensure that the CraftItemFactory is mockable
@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PowerMockIgnore({ "org.apache.log4j.*", "org.apache.logging.*", "org.bukkit.craftbukkit.libs.jline.*" })
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
		PacketContainer customPayload = new PacketContainer(PacketType.Play.Server.CUSTOM_PAYLOAD);
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
		PacketContainer spawnMob = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
		testPrimitive(spawnMob.getBytes(), 0, (byte)0, (byte)1);
	}
	
	@Test
	public void testGetShorts() {
		PacketContainer itemData = new PacketContainer(PacketType.Play.Server.TRANSACTION);
		testPrimitive(itemData.getShorts(), 0, (short)0, (short)1);
	}

	@Test
	public void testGetIntegers() {
		PacketContainer updateSign = new PacketContainer(PacketType.Play.Server.UPDATE_SIGN);
		testPrimitive(updateSign.getIntegers(), 0, (int)0, (int)1);
	}

	@Test
	public void testGetLongs() {
		PacketContainer updateTime = new PacketContainer(PacketType.Play.Server.UPDATE_TIME);
		testPrimitive(updateTime.getLongs(), 0, (long)0, (long)1);
	}

	@Test
	public void testGetFloat() {
		PacketContainer explosion = new PacketContainer(PacketType.Play.Server.EXPLOSION);
		testPrimitive(explosion.getFloat(), 0, (float)0, (float)0.8);
	}

	@Test
	public void testGetDoubles() {
		PacketContainer explosion = new PacketContainer(PacketType.Play.Server.EXPLOSION);
		testPrimitive(explosion.getDoubles(), 0, (double)0, (double)0.8);
	}

	@Test
	public void testGetStrings() {
		PacketContainer explosion = new PacketContainer(PacketType.Play.Client.CHAT);
		testPrimitive(explosion.getStrings(), 0, null, "hello");
	}

	@Test
	public void testGetStringArrays() {
		PacketContainer explosion = new PacketContainer(PacketType.Play.Server.UPDATE_SIGN);
		testObjectArray(explosion.getStringArrays(), 0, new String[0], new String[] { "hello", "world" });
	}

	@Test
	public void testGetIntegerArrays() {
		// Contains a byte array we will test
		PacketContainer mapChunkBulk = new PacketContainer(PacketType.Play.Server.MAP_CHUNK_BULK);
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
		PacketContainer windowClick = new PacketContainer(PacketType.Play.Client.WINDOW_CLICK);
		
		StructureModifier<ItemStack> items = windowClick.getItemModifier();
		ItemStack goldAxe = new ItemStack(Material.GOLD_AXE);
		
		assertNotNull(goldAxe.getType());
		assertNull(items.read(0));
		
		// Insert the goldaxe and check if it's there
		items.write(0, goldAxe);
		assertTrue("Item " + goldAxe + " != " + items.read(0), equivalentItem(goldAxe, items.read(0)));
	}
 
	@Test
	public void testGetItemArrayModifier() {
		PacketContainer windowItems = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
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
		// Not used in Netty
		if (MinecraftReflection.isUsingNetty())
			return;
		
		PacketContainer loginPacket = new PacketContainer(PacketType.Play.Server.LOGIN);
		StructureModifier<WorldType> worldAccess = loginPacket.getWorldTypeModifier();
		
		WorldType testValue = WorldType.LARGE_BIOMES;
		
		assertNull(worldAccess.read(0));
		
		// Insert and read back
		worldAccess.write(0, testValue);
		assertEquals(testValue, worldAccess.read(0));
	}

	@Test
	public void testGetNbtModifier() {
		PacketContainer updateTileEntity = new PacketContainer(PacketType.Play.Server.TILE_ENTITY_DATA);
		
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
		PacketContainer mobSpawnPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
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
		PacketContainer explosionPacket = new PacketContainer(PacketType.Play.Server.EXPLOSION);
		StructureModifier<List<ChunkPosition>> positionAccessor = explosionPacket.getPositionCollectionModifier();
		
		assertNull(positionAccessor.read(0));
		
		List<ChunkPosition> positions = Lists.newArrayList();
		positions.add(new ChunkPosition(1, 2, 3));
		positions.add(new ChunkPosition(3, 4, 5));
	
		// Insert and read back
		positionAccessor.write(0, positions);
		List<ChunkPosition> cloned = positionAccessor.read(0);
		
		assertEquals(positions, cloned);
	}

	@Test
	public void testGetWatchableCollectionModifier() {
		PacketContainer entityMetadata = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
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
	public void testGameProfiles() {
		PacketContainer spawnEntity = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
		WrappedGameProfile profile = new WrappedGameProfile("d7047a08-3150-4aa8-a2f2-7c1e2b17e298", "name");
		spawnEntity.getGameProfiles().write(0, profile);
		
		assertEquals(profile, spawnEntity.getGameProfiles().read(0));
	}
	
	@Test
	public void testChatComponents() {
		PacketContainer chatPacket = new PacketContainer(PacketType.Play.Server.CHAT);
		chatPacket.getChatComponents().write(0, 
				WrappedChatComponent.fromChatMessage("You shall not " + ChatColor.ITALIC + "pass!")[0]);
		
		assertEquals("{\"extra\":[\"You shall not \",{\"italic\":true,\"text\":\"pass!\"}],\"text\":\"\"}", 
				     chatPacket.getChatComponents().read(0).getJson());
	}
	
	@Test
	public void testSerialization() {
		PacketContainer chat = new PacketContainer(PacketType.Play.Client.CHAT);
		chat.getStrings().write(0, "Test");
		
		PacketContainer copy = (PacketContainer) SerializationUtils.clone(chat);
		
		assertEquals(PacketType.Play.Client.CHAT, copy.getType());
		assertEquals("Test", copy.getStrings().read(0));
	}
	
	@Test
	public void testAttributeList() {
		PacketContainer attribute = new PacketContainer(PacketType.Play.Server.UPDATE_ATTRIBUTES);
		attribute.getIntegers().write(0, 123); // Entity ID
		
		// Initialize some test data
		List<AttributeModifier> modifiers = Lists.newArrayList(
			new AttributeModifier(UUID.randomUUID(), "Unknown synced attribute modifier", 10, 0));
		AttributeSnapshot snapshot = new AttributeSnapshot(
				(PacketPlayOutUpdateAttributes) attribute.getHandle(), "generic.Maxhealth", 20.0, modifiers);
		
		attribute.getSpecificModifier(List.class).write(0, Lists.newArrayList(snapshot));
		PacketContainer cloned = attribute.deepClone();
		AttributeSnapshot clonedSnapshot = (AttributeSnapshot) cloned.getSpecificModifier(List.class).read(0).get(0);
		
		assertEquals(
				ToStringBuilder.reflectionToString(snapshot, ToStringStyle.SHORT_PREFIX_STYLE),
				ToStringBuilder.reflectionToString(clonedSnapshot, ToStringStyle.SHORT_PREFIX_STYLE));
	}
	
	@Test
	public void testBlocks() {
		PacketContainer blockAction = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
		blockAction.getBlocks().write(0, Material.STONE);
		
		assertEquals(Material.STONE, blockAction.getBlocks().read(0));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testPotionEffect() {
		PotionEffect effect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60, 1);
		
		// The constructor we want to call
		PacketConstructor creator = PacketConstructor.DEFAULT.withPacket(
				PacketType.Play.Server.ENTITY_EFFECT, new Class<?>[] { int.class, PotionEffect.class }); 
		PacketContainer packet = creator.createPacket(1, effect);
		
		assertEquals(1, (int) packet.getIntegers().read(0));
		assertEquals(effect.getType().getId(), (byte) packet.getBytes().read(0));
		assertEquals(effect.getAmplifier(), (byte) packet.getBytes().read(1));
		assertEquals(effect.getDuration(), (short) packet.getShorts().read(0));
	}
	
	@Test
	public void testDeepClone() {
		// Try constructing all the packets
		for (PacketType type : PacketType.values()) {
			// Whether or not this packet has been registered
			boolean registered = type.isSupported();
			
			try {
				PacketContainer constructed = new PacketContainer(type);
			
				if (!registered) {
					fail("Expected IllegalArgumentException(Packet " + type + " not registered");
				}
					
				// Initialize default values
				constructed.getModifier().writeDefaults();
				
				// Clone the packet
				PacketContainer cloned = constructed.deepClone();
				
				// Make sure they're equivalent
				StructureModifier<Object> firstMod = constructed.getModifier(), secondMod = cloned.getModifier();
				assertEquals(firstMod.size(), secondMod.size());

				if (PacketType.Status.Server.OUT_SERVER_INFO.equals(type)) {
					assertArrayEquals(SerializationUtils.serialize(constructed), SerializationUtils.serialize(cloned));

				} else {
					// Make sure all the fields are equivalent
					for (int i = 0; i < firstMod.size(); i++) {
						if (firstMod.getField(i).getType().isArray())
							assertArrayEquals(getArray(firstMod.read(i)), getArray(secondMod.read(i)));
						else
							testEquality(firstMod.read(i), secondMod.read(i));
					}
				}
				
			} catch (IllegalArgumentException e) {
				if (!registered) {
					// Check the same
					assertEquals(e.getMessage(), "The packet ID " + type + " is not registered.");
				} else {
					// Something is very wrong
					throw e;
				}
			}
		}
	}
	
	@Test
	public void testPacketType() {
		assertEquals(PacketType.Legacy.Server.SET_CREATIVE_SLOT, PacketType.findLegacy(107, Sender.SERVER));
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
