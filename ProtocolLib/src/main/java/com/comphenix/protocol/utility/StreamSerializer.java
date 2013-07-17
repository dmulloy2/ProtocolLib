package com.comphenix.protocol.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

/**
 * Utility methods for reading and writing Minecraft objects to streams.
 * 
 * @author Kristian
 */
public class StreamSerializer {
	// Cached methods
	private static Method READ_ITEM_METHOD;
	private static Method WRITE_ITEM_METHOD;

	private static Method READ_NBT_METHOD;
	private static Method WRITE_NBT_METHOD;
	
	private static Method READ_STRING_METHOD;
	private static Method WRITE_STRING_METHOD;
	
	/**
	 * Read or deserialize an item stack from an underlying input stream.
	 * <p>
	 * To supply a byte array, wrap it in a {@link java.io.ByteArrayInputStream ByteArrayInputStream} 
	 * and {@link java.io.DataInputStream DataInputStream}. 
	 * 
	 * @param input - the target input stream.
	 * @return The resulting item stack, or NULL if the serialized item stack was NULL.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 */
	public ItemStack deserializeItemStack(@Nonnull DataInputStream input) throws IOException {
		if (input == null)
			throw new IllegalArgumentException("Input stream cannot be NULL.");
		if (READ_ITEM_METHOD == null) {
			READ_ITEM_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(1).
					parameterDerivedOf(DataInput.class).
					returnDerivedOf(MinecraftReflection.getItemStackClass()).
					build());
		}
		try {
			Object nmsItem = READ_ITEM_METHOD.invoke(null, input);
			
			// Convert back to a Bukkit item stack
			if (nmsItem != null)
				return MinecraftReflection.getBukkitItemStack(nmsItem);
			else
				return null;
			
		} catch (Exception e) {
			throw new IOException("Cannot read item stack.", e);
		}
	}

	/**
	 * Read or deserialize an NBT compound from a input stream.	
	 * @param input - the target input stream.
	 * @return The resulting compound, or NULL.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 */
	public NbtCompound deserializeCompound(@Nonnull DataInputStream input) throws IOException {
		if (input == null)
			throw new IllegalArgumentException("Input stream cannot be NULL.");
		if (READ_NBT_METHOD == null) {
			READ_NBT_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(1).
					parameterDerivedOf(DataInput.class).
					returnDerivedOf(MinecraftReflection.getNBTBaseClass()).
					build());
		}
		try {
			Object nmsCompound = READ_NBT_METHOD.invoke(null, input);
			
			// Convert back to an NBT Compound
			if (nmsCompound != null)
				return NbtFactory.fromNMSCompound(nmsCompound);
			else
				return null;
			
		} catch (Exception e) {
			throw new IOException("Cannot read item stack.", e);
		}
	}
	
	/**
	 * Deserialize a string using the standard Minecraft UTF-16 encoding.
	 * <p>
	 * Note that strings cannot exceed 32767 characters, regardless if maximum lenght.
	 * @param input - the input stream.
	 * @param maximumLength - the maximum lenght of the string.
	 * @return The deserialized string.
	 * @throws IOException
	 */
	public String deserializeString(@Nonnull DataInputStream input, int maximumLength) throws IOException {
		if (input == null)
			throw new IllegalArgumentException("Input stream cannot be NULL.");
		if (maximumLength > 32767)
			throw new IllegalArgumentException("Maximum lenght cannot exceed 32767 characters.");
		if (maximumLength < 0)
			throw new IllegalArgumentException("Maximum lenght cannot be negative.");
		
		if (READ_STRING_METHOD == null) {
			READ_STRING_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(2).
					parameterDerivedOf(DataInput.class, 0).
					parameterExactType(int.class, 1).
					returnTypeExact(String.class).
					build());
		}
		
		try {
			// Convert back to a Bukkit item stack
			return (String) READ_STRING_METHOD.invoke(null, input, maximumLength);
		} catch (Exception e) {
			throw new IOException("Cannot read Minecraft string.", e);
		}
	}
	
	/**
	 * Deserialize an item stack from a base-64 encoded string.
	 * @param input - base-64 encoded string.
	 * @return A deserialized item stack, or NULL if the serialized ItemStack was also NULL.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 */
	public ItemStack deserializeItemStack(@Nonnull String input) throws IOException {
		if (input == null)
			throw new IllegalArgumentException("Input text cannot be NULL.");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(input));
		
		return deserializeItemStack(new DataInputStream(inputStream));
	}
	
	/**
	 * Write or serialize an item stack to the given output stream.
	 * <p>
	 * To supply a byte array, wrap it in a {@link java.io.ByteArrayOutputStream ByteArrayOutputStream} 
	 * and {@link java.io.DataOutputStream DataOutputStream}. 
	 * <p>
	 * Note: An ItemStack can be written to a stream even if it's NULL.
	 * 
	 * @param output - the target output stream.
	 * @param stack - the item stack that will be written, or NULL to represent air/nothing.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public void serializeItemStack(@Nonnull DataOutputStream output, ItemStack stack) throws IOException {
		if (output == null)
			throw new IllegalArgumentException("Output stream cannot be NULL.");
		
		// Get the NMS version of the ItemStack
		Object nmsItem = MinecraftReflection.getMinecraftItemStack(stack);
		
		if (WRITE_ITEM_METHOD == null)
			WRITE_ITEM_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(2).
					parameterDerivedOf(MinecraftReflection.getItemStackClass(), 0).
					parameterDerivedOf(DataOutput.class, 1).
					build());
		try {
			WRITE_ITEM_METHOD.invoke(null, nmsItem, output);
		} catch (Exception e) {
			throw new IOException("Cannot write item stack " + stack, e);
		}
	}
	
	/**
	 * Write or serialize a NBT compound to the given output stream.
	 * <p>
	 * Note: An NBT compound can be written to a stream even if it's NULL.
	 * 
	 * @param output - the target output stream.
	 * @param stack - the NBT compound to be serialized, or NULL to represent nothing.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public void serializeCompound(@Nonnull DataOutputStream output, NbtCompound compound) throws IOException {
		if (output == null)
			throw new IllegalArgumentException("Output stream cannot be NULL.");
		
		// Get the NMS version of the compound
		Object handle = compound != null ? NbtFactory.fromBase(compound).getHandle() : null;
		
		if (WRITE_NBT_METHOD == null) {
			WRITE_NBT_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(2).
					parameterDerivedOf(MinecraftReflection.getNBTBaseClass(), 0).
					parameterDerivedOf(DataOutput.class, 1).
					returnTypeVoid().
					build());
			WRITE_NBT_METHOD.setAccessible(true);
		}
		
		try {
			WRITE_NBT_METHOD.invoke(null, handle, output);
		} catch (Exception e) {
			throw new IOException("Cannot write compound " + compound, e);
		}
	}
	
	/**
	 * Deserialize a string using the standard Minecraft UTF-16 encoding.
	 * <p>
	 * Note that strings cannot exceed 32767 characters, regardless if maximum lenght.
	 * @param input - the input stream.
	 * @param maximumLength - the maximum lenght of the string.
	 * @return
	 * @throws IOException
	 */
	public void serializeString(@Nonnull DataOutputStream output, String text) throws IOException {
		if (output == null)
			throw new IllegalArgumentException("output stream cannot be NULL.");
		if (text == null)
			throw new IllegalArgumentException("text cannot be NULL.");
		
		if (WRITE_STRING_METHOD == null) {
			WRITE_STRING_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(2).
					parameterExactType(String.class, 0).
					parameterDerivedOf(DataOutput.class, 1).
					returnTypeVoid().
					build());
		}
		
		try {
			// Convert back to a Bukkit item stack
			WRITE_STRING_METHOD.invoke(null, text, output);
		} catch (Exception e) {
			throw new IOException("Cannot read Minecraft string.", e);
		}
	}
	
	/**
	 * Serialize an item stack as a base-64 encoded string.
	 * <p>
	 * Note: An ItemStack can be written to the serialized text even if it's NULL.
	 * 
	 * @param stack - the item stack to serialize, or NULL to represent air/nothing.
	 * @return A base-64 representation of the given item stack.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public String serializeItemStack(ItemStack stack) throws IOException {
		 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		 DataOutputStream dataOutput = new DataOutputStream(outputStream);
		 
		 serializeItemStack(dataOutput, stack);
		 
		 // Serialize that array
		return Base64Coder.encodeLines(outputStream.toByteArray());
	}
}
