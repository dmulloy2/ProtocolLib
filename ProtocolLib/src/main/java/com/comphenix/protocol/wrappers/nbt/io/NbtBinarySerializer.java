package com.comphenix.protocol.wrappers.nbt.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;

public class NbtBinarySerializer {
	// Used to read and write NBT
	private static Method methodWrite;
	private static Method methodLoad;
	
	/**
	 * Retrieve a default instance of the NBT binary serializer.
	 */
	public static final NbtBinarySerializer DEFAULT = new NbtBinarySerializer();
	
	/**
	 * Write the content of a wrapped NBT tag to a stream.
	 * @param value - the NBT tag to write.
	 * @param destination - the destination stream.
	 */
	public <TType> void serialize(NbtBase<TType> value, DataOutput destination) {
		if (methodWrite == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			
			// Use the base class
			methodWrite = getUtilityClass().
					getMethodByParameters("writeNBT", base, DataOutput.class);
			methodWrite.setAccessible(true);
		}
		
		try {
			methodWrite.invoke(null, NbtFactory.fromBase(value).getHandle(), destination);
		} catch (Exception e) {
			throw new FieldAccessException("Unable to write NBT " + value, e);
		}
	}

	/**
	 * Load an NBT tag from a stream.
	 * @param source - the input stream.
	 * @return An NBT tag.
	 */
	public <TType> NbtWrapper<TType> deserialize(DataInput source) {
		if (methodLoad == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			Class<?>[] params = MinecraftReflection.isUsingNetty() ? 
					new Class<?>[] { DataInput.class, int.class } : 
					new Class<?>[] { DataInput.class };
			
			// Use the base class
			methodLoad = getUtilityClass().getMethodByParameters("load", base, params);
			methodLoad.setAccessible(true);
		}
		
		try {
			Object result = null;
			
			// Invoke the correct utility method
			if (MinecraftReflection.isUsingNetty())
				result = methodLoad.invoke(null, source, 0);
			else
				result = methodLoad.invoke(null, source);
			return NbtFactory.fromNMS(result, null);
		} catch (Exception e) {
			throw new FieldAccessException("Unable to read NBT from " + source, e);
		}
	}
	
	private FuzzyReflection getUtilityClass() {
		if (MinecraftReflection.isUsingNetty()) {
			return FuzzyReflection.fromClass(MinecraftReflection.getNbtCompressedStreamToolsClass(), true);
		} else {
			return FuzzyReflection.fromClass(MinecraftReflection.getNBTBaseClass(), true);
		}
	}
	
	/**
	 * Load an NBT compound from a stream.
	 * @param source - the input stream.
	 * @return An NBT compound.
	 */
	@SuppressWarnings("rawtypes")
	public NbtCompound deserializeCompound(DataInput source) {
		// I always seem to override generics ...
		return (NbtCompound) (NbtBase) deserialize(source);
	}
	
	/**
	 * Load an NBT list from a stream.
	 * @param source - the input stream.
	 * @return An NBT list.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> NbtList<T> deserializeList(DataInput source) {
		return (NbtList<T>) (NbtBase) deserialize(source);
	}
}
