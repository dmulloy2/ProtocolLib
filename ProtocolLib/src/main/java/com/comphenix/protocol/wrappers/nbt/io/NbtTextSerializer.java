package com.comphenix.protocol.wrappers.nbt.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;

/**
 * Serializes NBT to a base N (default 32) encoded string and back.
 * 
 * @author Kristian
 */
public class NbtTextSerializer {
	/**
	 * The default radix to use while converting to text.
	 */
	public static final int STANDARD_BASE = 32;
	
	/**
	 * A default instance of this serializer.
	 */
	public static final NbtTextSerializer DEFAULT = new NbtTextSerializer();
	
	private NbtBinarySerializer binarySerializer;
	private int baseRadix;
	
	public NbtTextSerializer() {
		this(new NbtBinarySerializer(), STANDARD_BASE);
	}
	
	/**
	 * Construct a serializer with a custom binary serializer and base radix.
	 * @param binary - binary serializer.
	 * @param baseRadix - base radix in the range 2 - 32.
	 */
	public NbtTextSerializer(NbtBinarySerializer binary, int baseRadix) {
		this.binarySerializer = binary;
		this.baseRadix = baseRadix;
	}

	/**
	 * Retrieve the binary serializer that is used.
	 * @return The binary serializer.
	 */
	public NbtBinarySerializer getBinarySerializer() {
		return binarySerializer;
	}

	/**
	 * Retrieve the base radix.
	 * @return The base radix.
	 */
	public int getBaseRadix() {
		return baseRadix;
	}

	/**
	 * Serialize a NBT tag to a String.
	 * @param value - the NBT tag to serialize.
	 * @return The NBT tag in base N form.
	 */
	public <TType> String serialize(NbtBase<TType> value) {
		 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		 DataOutputStream dataOutput = new DataOutputStream(outputStream);
		 
		 binarySerializer.serialize(value, dataOutput);
		 
		 // Serialize that array
		 return new BigInteger(1, outputStream.toByteArray()).toString(baseRadix);
	}
	
	/**
	 * Deserialize a NBT tag from a base N encoded string.
	 * @param input - the base N string.
	 * @return The NBT tag contained in the string.
	 * @throws IOException If we are unable to parse the input.
	 */
	public <TType> NbtWrapper<TType> deserialize(String input) throws IOException {
		try {
			BigInteger baseN = new BigInteger(input, baseRadix);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(baseN.toByteArray());
			
			return binarySerializer.deserialize(new DataInputStream(inputStream));
			
		} catch (NumberFormatException e) {
			throw new IOException("Input is not valid base " + baseRadix + ".", e);
		}
	}
	
	/**
	 * Deserialize a NBT compound from a base N encoded string.
	 * @param input - the base N string.
	 * @return The NBT tag contained in the string.
	 * @throws IOException If we are unable to parse the input.
	 */
	@SuppressWarnings("rawtypes")
	public NbtCompound deserializeCompound(String input) throws IOException {
		// I always seem to override generics ...
		return (NbtCompound) (NbtBase) deserialize(input);
	}
	
	/**
	 * Deserialize a NBT list from a base N encoded string.
	 * @param input - the base N string.
	 * @return The NBT tag contained in the string.
	 * @throws IOException If we are unable to parse the input.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> NbtList<T> deserializeList(String input) throws IOException {
		return (NbtList<T>) (NbtBase) deserialize(input);
	}
}
