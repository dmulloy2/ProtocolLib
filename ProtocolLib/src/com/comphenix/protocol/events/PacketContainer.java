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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;

import net.minecraft.server.Packet;

/**
 * Represents a Minecraft packet indirectly.
 * 
 * @author Kristian
 */
public class PacketContainer {

	protected Packet handle;
	protected int id;
	
	// Current structure modifier
	protected StructureModifier<Object> structureModifier;
		
	// Check whether or not certain classes exists
	private static boolean hasWorldType = false;
	
	// The getEntity method
	private static Method getEntity;
	
	static {
		try {
			Class.forName("net.minecraft.server.WorldType");
			hasWorldType = true;
		} catch (ClassNotFoundException e) {
		}
	}
	
	/**
	 * Creates a packet container for a new packet.
	 * @param id - ID of the packet to create.
	 */
	public PacketContainer(int id) {
		this(id, StructureCache.newPacket(id));
	}
	
	/**
	 * Creates a packet container for an existing packet.
	 * @param id - ID of the given packet.
	 * @param handle - contained packet.
	 */
	public PacketContainer(int id, Packet handle) {
		this(id, handle, StructureCache.getStructure(id).withTarget(handle));
	}
	
	/**
	 * Creates a packet container for an existing packet.
	 * @param id - ID of the given packet.
	 * @param handle - contained packet.
	 * @param structure - structure modifier.
	 */
	public PacketContainer(int id, Packet handle, StructureModifier<Object> structure) {
		if (handle == null)
			throw new IllegalArgumentException("handle cannot be null.");
		
		this.id = id;
		this.handle = handle;
		this.structureModifier = structure;
	}
	
	/**
	 * Retrieves the underlying Minecraft packet. 
	 * @return Underlying Minecraft packet.
	 */
	public Packet getHandle() {
		return handle;
	}
	
	/**
	 * Retrieves the generic structure modifier for this packet.
	 * @return Structure modifier.
	 */
	public StructureModifier<Object> getModifier() {
		return structureModifier;
	}
	
	/**
	 * Retrieves a read/write structure for every field with the given type.
	 * @param primitiveType - the type to find.
	 * @return A modifier for this specific type.
	 */
	public <T> StructureModifier<T> getSpecificModifier(Class<T> primitiveType) {
		return structureModifier.withType(primitiveType);
	}
	
	/**
	 * Retrieves a read/write structure for ItemStack.
	 * <p>
	 * This modifier will automatically marshall between the Bukkit ItemStack and the
	 * internal Minecraft ItemStack.
	 * @return A modifier for ItemStack fields.
	 */
	public StructureModifier<ItemStack> getItemModifier() {
		// Convert from and to the Bukkit wrapper
		return structureModifier.<ItemStack>withType(net.minecraft.server.ItemStack.class, new EquivalentConverter<ItemStack>() {
			public Object getGeneric(ItemStack specific) {
				return ((CraftItemStack) specific).getHandle();
			}
			
			@Override
			public ItemStack getSpecific(Object generic) {
				return new CraftItemStack((net.minecraft.server.ItemStack) generic);
			}
		});
	}
	
	/**
	 * Retrieves a read/write structure for arrays of ItemStacks.
	 * <p>
	 * This modifier will automatically marshall between the Bukkit ItemStack and the
	 * internal Minecraft ItemStack.
	 * @return A modifier for ItemStack array fields.
	 */
	public StructureModifier<ItemStack[]> getItemArrayModifier() {
		// Convert to and from the Bukkit wrapper
		return structureModifier.<ItemStack[]>withType(net.minecraft.server.ItemStack[].class, new EquivalentConverter<ItemStack[]>() {
			public Object getGeneric(ItemStack[] specific) {
				net.minecraft.server.ItemStack[] result = new net.minecraft.server.ItemStack[specific.length];
				
				// Unwrap every item
				for (int i = 0; i < result.length; i++) {
					result[i] = ((CraftItemStack) specific[i]).getHandle();
				}
				return result;
			}
			
			@Override
			public ItemStack[] getSpecific(Object generic) {
				net.minecraft.server.ItemStack[] input = (net.minecraft.server.ItemStack[]) generic;
				ItemStack[] result = new ItemStack[input.length];
				
				// Add the wrapper
				for (int i = 0; i < result.length; i++) {
					result[i] = new CraftItemStack(input[i]);
				}
				return result;
			}
		});
	}
	
	/**
	 * Retrieves a read/write structure for the world type enum.
	 * <p>
	 * This modifier will automatically marshall between the Bukkit world type and the
	 * internal Minecraft world type.
	 * @return A modifier for world type fields.
	 */
	public StructureModifier<WorldType> getWorldTypeModifier() {
	
		if (!hasWorldType) {
			// We couldn't find the Minecraft equivalent
			return structureModifier.withType(null);
		}
		
		// Convert to and from the Bukkit wrapper
		return structureModifier.<WorldType>withType(net.minecraft.server.WorldType.class, new EquivalentConverter<WorldType>() {
			@Override
			public Object getGeneric(WorldType specific) {
				return net.minecraft.server.WorldType.getType(specific.getName());
			}
			
			@Override
			public WorldType getSpecific(Object generic) {
				net.minecraft.server.WorldType type = (net.minecraft.server.WorldType) generic;
				return WorldType.getByName(type.name());
			}
		});
	}
	
	/**
	 * Retrieves a read/write structure for entity objects.
	 * <p>
	 * Note that entities are transmitted by integer ID, and the type may not be enough
	 * to distinguish between entities and other values. Thus, this structure modifier
	 * MAY return null or invalid entities for certain fields. Using the correct index 
	 * is essential.
	 * 
	 * @return A modifier entity types.
	 */
	public StructureModifier<Entity> getEntityModifier(World world) {
	
		final Object worldServer = ((CraftWorld) world).getHandle();
		final Class<?> nmsEntityClass = net.minecraft.server.Entity.class;
		
		if (getEntity == null)
			getEntity = FuzzyReflection.fromObject(worldServer).getMethodByParameters(
					"getEntity", nmsEntityClass, new Class[] { int.class });
		
		// Convert to and from the Bukkit wrapper
		return structureModifier.<Entity>withType(int.class, new EquivalentConverter<Entity>() {
			@Override
			public Object getGeneric(Entity specific) {
				// Simple enough
				return specific.getEntityId();
			}
			
			@Override
			public Entity getSpecific(Object generic) {
				try {
					net.minecraft.server.Entity nmsEntity = (net.minecraft.server.Entity) 
							getEntity.invoke(worldServer, generic);
					
					// Attempt to get the Bukkit entity
					if (nmsEntity != null) {
						return nmsEntity.getBukkitEntity();
					} else {
						return null;
					}
					
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Incorrect arguments detected.", e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Cannot read field due to a security limitation.", e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Error occured in Minecraft method.", e.getCause());
				}
			}
		});
	}
	
	/**
	 * Retrieves the ID of this packet.
	 * @return Packet ID.
	 */
	public int getID() {
		return id;
	}
}
