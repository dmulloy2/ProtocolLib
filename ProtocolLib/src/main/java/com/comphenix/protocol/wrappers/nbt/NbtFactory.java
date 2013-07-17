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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;

/**
 * Factory methods for creating NBT elements, lists and compounds.
 * 
 * @author Kristian
 */
public class NbtFactory {
	// Used to create the underlying tag
	private static Method methodCreateTag;
		
	// Item stack trickery
	private static StructureModifier<Object> itemStackModifier;
	
	/**
	 * Attempt to cast this NBT tag as a compund.
	 * @param tag - the NBT tag to cast.
	 * @return This instance as a compound.
	 * @throws UnsupportedOperationException If this is not a compound.
	 */
	public static NbtCompound asCompound(NbtBase<?> tag) {
		if (tag instanceof NbtCompound)
			return (NbtCompound) tag;
		else if (tag != null)
			throw new UnsupportedOperationException(
					"Cannot cast a " + tag.getClass() + "( " + tag.getType() + ") to TAG_COMPUND.");
		else
			throw new IllegalArgumentException("Tag cannot be NULL.");
	}
	
	/**
	 * Attempt to cast this NBT tag as a list.
	 * @param tag - the NBT tag to cast.
	 * @return This instance as a list.
	 * @throws UnsupportedOperationException If this is not a list.
	 */
	public static NbtList<?> asList(NbtBase<?> tag) {
		if (tag instanceof NbtList)
			return (NbtList<?>) tag;
		else if (tag != null)
			throw new UnsupportedOperationException(
					"Cannot cast a " + tag.getClass() + "( " + tag.getType() + ") to TAG_LIST.");
		else
			throw new IllegalArgumentException("Tag cannot be NULL.");
	}
	
	/**
	 * Get a NBT wrapper from a NBT base.
	 * <p>
	 * This may clone the content if the NbtBase is not a NbtWrapper.
	 * @param base - the base class.
	 * @return A NBT wrapper.
	 */
	@SuppressWarnings("unchecked")
	public static <T> NbtWrapper<T> fromBase(NbtBase<T> base) {
		if (base instanceof NbtWrapper) {
			return (NbtWrapper<T>) base;
		} else {
			if (base.getType() == NbtType.TAG_COMPOUND) {
				// Load into a NBT-backed wrapper
				WrappedCompound copy = WrappedCompound.fromName(base.getName());
				T value = base.getValue();
				
				copy.setValue((Map<String, NbtBase<?>>) value);
				return (NbtWrapper<T>) copy;
			
			} else if (base.getType() == NbtType.TAG_LIST) {
				// As above
				NbtList<T> copy = WrappedList.fromName(base.getName());
				
				copy.setValue((List<NbtBase<T>>) base.getValue());
				return (NbtWrapper<T>) copy;
				
			} else {
				// Copy directly
				NbtWrapper<T> copy = ofWrapper(base.getType(), base.getName());
				
				copy.setValue(base.getValue());
				return copy;
			}
		}
	}
	
	/**
	 * Set the NBT compound tag of a given item stack.
	 * <p>
	 * The item stack must be a wrapper for a CraftItemStack. Use 
	 * {@link MinecraftReflection#getBukkitItemStack(ItemStack)} if not.
	 * @param stack - the item stack.
	 * @param compound - the new NBT compound.
	 */
	public static void setItemTag(ItemStack stack, NbtCompound compound) {
		if (!MinecraftReflection.isCraftItemStack(stack))
			throw new IllegalArgumentException("Stack must be a CraftItemStack.");
		
		StructureModifier<NbtBase<?>> modifier = getStackModifier(stack);
		modifier.write(0, compound);
	}
	
	/**
	 * Construct a wrapper for an NBT tag stored (in memory) in an item stack. This is where
	 * auxillary data such as enchanting, name and lore is stored. It doesn't include the items
	 * material, damage value or count.
	 * <p>
	 * The item stack must be a wrapper for a CraftItemStack. Use 
	 * {@link MinecraftReflection#getBukkitItemStack(ItemStack)} if not.
	 * @param stack - the item stack.
	 * @return A wrapper for its NBT tag.
	 */
	public static NbtWrapper<?> fromItemTag(ItemStack stack) {
		if (!MinecraftReflection.isCraftItemStack(stack))
			throw new IllegalArgumentException("Stack must be a CraftItemStack.");
		
		StructureModifier<NbtBase<?>> modifier = getStackModifier(stack);
		NbtBase<?> result = modifier.read(0);
		
		// Create the tag if it doesn't exist
		if (result == null) {
			result = NbtFactory.ofCompound("tag");
			modifier.write(0, result);
		}
		return fromBase(result);
	}
	
	/**
	 * Retrieve a structure modifier that automatically marshalls between NBT wrappers and their NMS counterpart.
	 * @param stack - the stack that will store the NBT compound.
	 * @return The structure modifier.
	 */
	private static StructureModifier<NbtBase<?>> getStackModifier(ItemStack stack) {
		Object nmsStack = MinecraftReflection.getMinecraftItemStack(stack);
		
		if (itemStackModifier == null) {
			itemStackModifier = new StructureModifier<Object>(nmsStack.getClass(), Object.class, false);
		}
		
		// Use the first and best NBT tag
		return itemStackModifier.
				withTarget(nmsStack).
				withType(MinecraftReflection.getNBTBaseClass(), 
						 BukkitConverters.getNbtConverter());
	}
	
	/**
	 * Initialize a NBT wrapper.
	 * @param handle - the underlying net.minecraft.server object to wrap.
	 * @return A NBT wrapper.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> NbtWrapper<T> fromNMS(Object handle) {
		WrappedElement<T> partial = new WrappedElement<T>(handle);
		
		// See if this is actually a compound tag
		if (partial.getType() == NbtType.TAG_COMPOUND)
			return (NbtWrapper<T>) new WrappedCompound(handle);
		else if (partial.getType() == NbtType.TAG_LIST)
			return new WrappedList(handle);
		else
			return partial;
	}
		
	/**
	 * Retrieve the NBT compound from a given NMS handle.
	 * @param handle - the underlying net.minecraft.server object to wrap.
	 * @return A NBT compound wrapper
	 */
	public static NbtCompound fromNMSCompound(@Nonnull Object handle) {
		if (handle == null)
			throw new IllegalArgumentException("handle cannot be NULL.");
		return (NbtCompound) NbtFactory.<Map<String, NbtBase<?>>>fromNMS(handle);
	}
	
	/**
	 * Constructs a NBT tag of type string.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<String> of(String name, String value) {
		return ofWrapper(NbtType.TAG_STRING, name, value);
	}
	
	/**
	 * Constructs a NBT tag of type byte.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<Byte> of(String name, byte value) {
		return ofWrapper(NbtType.TAG_BYTE, name, value);
	}
	
	/**
	 * Constructs a NBT tag of type short.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<Short> of(String name, short value) {
		return ofWrapper(NbtType.TAG_SHORT, name, value);
	}
	
	/**
	 * Constructs a NBT tag of type int.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<Integer> of(String name, int value) {
		return ofWrapper(NbtType.TAG_INT, name, value);
	}
	
	/**
	 * Constructs a NBT tag of type long.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<Long> of(String name, long value) {
		return ofWrapper(NbtType.TAG_LONG, name, value);
	}
	
	/**
	 * Constructs a NBT tag of type float.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<Float> of(String name, float value) {
		return ofWrapper(NbtType.TAG_FLOAT, name, value);
	}
	
	/**
	 * Constructs a NBT tag of type double.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<Double> of(String name, double value) {
		return ofWrapper(NbtType.TAG_DOUBLE, name, value);
	}
	
	/**
	 * Constructs a NBT tag of type byte array.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<byte[]> of(String name, byte[] value) {
		return ofWrapper(NbtType.TAG_BYTE_ARRAY, name, value);
	}
	
	/**
	 * Constructs a NBT tag of type int array.
	 * @param name - name of the tag.
	 * @param value - value of the tag. 
	 * @return The constructed NBT tag.
	 */
	public static NbtBase<int[]> of(String name, int[] value) {
		return ofWrapper(NbtType.TAG_INT_ARRAY, name, value);
	}
	
	/**
	 * Construct a new NBT compound initialized with a given list of NBT values.
	 * @param name - the name of the compound wrapper. 
	 * @param list - the list of elements to add.
	 * @return The new wrapped NBT compound.
	 */
	public static NbtCompound ofCompound(String name, Collection<? extends NbtBase<?>> list) {
		return WrappedCompound.fromList(name, list);
	}
	
	/**
	 * Construct a new NBT compound wrapper.
	 * @param name - the name of the compound wrapper. 
	 * @return The new wrapped NBT compound.
	 */
	public static NbtCompound ofCompound(String name) {
		return WrappedCompound.fromName(name);
	}
	
	/**
	 * Construct a NBT list of out an array of values.
	 * @param name - name of this list.
	 * @param elements - elements to add.
	 * @return The new filled NBT list.
	 */
	public static <T> NbtList<T> ofList(String name, T... elements) {
		return WrappedList.fromArray(name, elements);
	}
	
	/**
	 * Construct a NBT list of out a list of values.
	 * @param name - name of this list.
	 * @param elements - elements to add.
	 * @return The new filled NBT list.
	 */
	public static <T> NbtList<T> ofList(String name, Collection<? extends T> elements) {
		return WrappedList.fromList(name, elements);
	}
	
	/**
	 * Create a new NBT wrapper from a given type.
	 * @param type - the NBT type.
	 * @param name - the name of the NBT tag.
	 * @return The new wrapped NBT tag.
	 * @throws FieldAccessException If we're unable to create the underlying tag.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> NbtWrapper<T> ofWrapper(NbtType type, String name) {
		if (type == null)
			throw new IllegalArgumentException("type cannot be NULL.");
		if (type == NbtType.TAG_END)
			throw new IllegalArgumentException("Cannot create a TAG_END.");
		
		if (methodCreateTag == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			
			// Use the base class
			methodCreateTag = FuzzyReflection.fromClass(base).
				getMethodByParameters("createTag", base, new Class<?>[] { byte.class, String.class });
		}
		
		try {
			Object handle = methodCreateTag.invoke(null, (byte) type.getRawID(), name);
			
			if (type == NbtType.TAG_COMPOUND)
				return (NbtWrapper<T>) new WrappedCompound(handle);
			else if (type == NbtType.TAG_LIST)
				return (NbtWrapper<T>) new WrappedList(handle);
			else
				return new WrappedElement<T>(handle);
			
		} catch (Exception e) {
			// Inform the caller
			throw new FieldAccessException(
					String.format("Cannot create NBT element %s (type: %s)", name, type),
					e);
		}
	}
	
	/**
	 * Create a new NBT wrapper from a given type.
	 * @param type - the NBT type.
	 * @param name - the name of the NBT tag.
	 * @param value - the value of the new tag.
	 * @return The new wrapped NBT tag.
	 * @throws FieldAccessException If we're unable to create the underlying tag.
	 */
	public static <T> NbtWrapper<T> ofWrapper(NbtType type, String name, T value) {
		NbtWrapper<T> created = ofWrapper(type, name);
		
		// Update the value
		created.setValue(value);
		return created;
	}
	
	/**
	 * Create a new NBT wrapper from a given type.
	 * @param type - type of the NBT value.
	 * @param name - the name of the NBT tag.
	 * @param value - the value of the new tag.
	 * @return The new wrapped NBT tag.
	 * @throws FieldAccessException If we're unable to create the underlying tag.
	 * @throws IllegalArgumentException If the given class type is not valid NBT.
	 */
	public static <T> NbtWrapper<T> ofWrapper(Class<?> type, String name, T value) {
		return ofWrapper(NbtType.getTypeFromClass(type), name, value);
	}
}
