package com.comphenix.protocol.wrappers.nbt.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;

/**
 * Serializes NBT to a base-64 encoded string and back.
 * 
 * @author Kristian
 */
public class NbtTextSerializer {
	/**
	 * A default instance of this serializer.
	 */
	public static final NbtTextSerializer DEFAULT = new NbtTextSerializer();
	
	private NbtBinarySerializer binarySerializer;
	
	public NbtTextSerializer() {
		this(new NbtBinarySerializer());
	}
	
	/**
	 * Construct a serializer with a custom binary serializer.
	 * @param binary - binary serializer.
	 */
	public NbtTextSerializer(NbtBinarySerializer binary) {
		this.binarySerializer = binary;
	}

	/**
	 * Retrieve the binary serializer that is used.
	 * @return The binary serializer.
	 */
	public NbtBinarySerializer getBinarySerializer() {
		return binarySerializer;
	}

	/**
	 * Serialize a NBT tag to a base-64 encoded string.
	 * @param value - the NBT tag to serialize.
	 * @return The NBT tag in base-64 form.
	 */
	public <TType> String serialize(NbtBase<TType> value) {
		 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		 DataOutputStream dataOutput = new DataOutputStream(outputStream);
		 
		 binarySerializer.serialize(value, dataOutput);
		 
		 // Serialize that array
		 return Base64Coder.encodeLines(outputStream.toByteArray());
	}
	
	/**
	 * Deserialize a NBT tag from a base-64 encoded string.
	 * @param input - the base-64 string.
	 * @return The NBT tag contained in the string.
	 * @throws IOException If we are unable to parse the input.
	 */
	public <TType> NbtWrapper<TType> deserialize(String input) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(input));
		
		return binarySerializer.deserialize(new DataInputStream(inputStream));
	}
	
	/**
	 * Deserialize a NBT compound from a base-64 encoded string.
	 * @param input - the base-64 string.
	 * @return The NBT tag contained in the string.
	 * @throws IOException If we are unable to parse the input.
	 */
	@SuppressWarnings("rawtypes")
	public NbtCompound deserializeCompound(String input) throws IOException {
		// I always seem to override generics ...
		return (NbtCompound) (NbtBase) deserialize(input);
	}
	
	/**
	 * Deserialize a NBT list from a base-64 encoded string.
	 * @param input - the base-64 string.
	 * @return The NBT tag contained in the string.
	 * @throws IOException If we are unable to parse the input.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> NbtList<T> deserializeList(String input) throws IOException {
		return (NbtList<T>) (NbtBase) deserialize(input);
	}
}
