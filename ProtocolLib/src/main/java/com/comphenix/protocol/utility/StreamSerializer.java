package com.comphenix.protocol.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.reflect.FuzzyReflection;

/**
 * Utility methods for reading and writing Minecraft objects to streams.
 * 
 * @author Kristian
 */
public class StreamSerializer {
	// Cached methods
	private static Method readItemMethod;
	private static Method writeItemMethod;

	/**
	 * Read or deserialize an item stack from an underlying input stream.
	 * <p>
	 * To supply a byte array, wrap it in a {@link java.io.ByteArrayInputStream ByteArrayInputStream} 
	 * and {@link java.io.DataInputStream DataInputStream}. 
	 * 
	 * @param input - the target input stream.
	 * @return The resulting item stack.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 */
	public ItemStack deserializeItemStack(DataInputStream input) throws IOException {
		if (readItemMethod == null)
			readItemMethod = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).
								getMethodByParameters("readPacket", 
										MinecraftReflection.getItemStackClass(), 
										new Class<?>[] {DataInputStream.class});
		try {
			Object nmsItem = readItemMethod.invoke(null, input);
			
			// Convert back to a Bukkit item stack
			return MinecraftReflection.getBukkitItemStack(nmsItem);
			
		} catch (Exception e) {
			throw new IOException("Cannot read item stack.", e);
		}
	}
	
	/**
	 * Deserialize an item stack from a base-32 encoded string.
	 * @param input - base-32 encoded string.
	 * @return A deserialized item stack.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 */
	public ItemStack deserializeItemStack(String input) throws IOException {
		try {
			BigInteger base32 = new BigInteger(input, 32);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(base32.toByteArray());
			
			return deserializeItemStack(new DataInputStream(inputStream));
			
		} catch (NumberFormatException e) {
			throw new IOException("Input is not valid base 32.", e);
		}
	}
	
	/**
	 * Write or serialize an item stack to the given output stream.
	 * <p>
	 * To supply a byte array, wrap it in a {@link java.io.ByteArrayOutputStream ByteArrayOutputStream} 
	 * and {@link java.io.DataOutputStream DataOutputStream}. 
	 * 
	 * @param output - the target output stream.
	 * @param stack - the item stack that will be written.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public void serializeItemStack(DataOutputStream output, ItemStack stack) throws IOException {
		Object nmsItem = MinecraftReflection.getMinecraftItemStack(stack);
		
		if (writeItemMethod == null)
			writeItemMethod = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).
								getMethodByParameters("writePacket", new Class<?>[] { 
										MinecraftReflection.getItemStackClass(), 
										DataOutputStream.class });
		try {
			writeItemMethod.invoke(null, nmsItem, output);
		} catch (Exception e) {
			throw new IOException("Cannot write item stack " + stack, e);
		}
	}
	
	/**
	 * Serialize an item stack as a base-32 encoded string.
	 * @param stack - the item stack to serialize.
	 * @return A base-32 representation of the given item stack.
	 * @throws IOException If the operation fails due to reflection problems.
	 */
	public String serializeItemStack(ItemStack stack) throws IOException {
		 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		 DataOutputStream dataOutput = new DataOutputStream(outputStream);
		 
		 serializeItemStack(dataOutput, stack);
		 
		 // Serialize that array
		 return new BigInteger(1, outputStream.toByteArray()).toString(32);
	}
}
