package com.comphenix.protocol.utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

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
}
