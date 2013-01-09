package com.comphenix.protocol.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

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
	 * Deserialize an item stack from a base-64 encoded string.
	 * @param input - base-64 encoded string.
	 * @return A deserialized item stack.
	 * @throws IOException If the operation failed due to reflection or corrupt data.
	 */
	public ItemStack deserializeItemStack(String input) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(input));
		
		return deserializeItemStack(new DataInputStream(inputStream));
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
	 * Serialize an item stack as a base-64 encoded string.
	 * @param stack - the item stack to serialize.
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
