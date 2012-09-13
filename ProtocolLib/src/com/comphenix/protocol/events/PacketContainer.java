package com.comphenix.protocol.events;

import org.bukkit.WorldType;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.EquivalentConverter;
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
	 * Retrieves the ID of this packet.
	 * @return Packet ID.
	 */
	public int getID() {
		return id;
	}
}
