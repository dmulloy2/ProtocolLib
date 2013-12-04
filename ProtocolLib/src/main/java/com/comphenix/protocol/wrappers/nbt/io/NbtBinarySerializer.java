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
			methodWrite = FuzzyReflection.fromClass(base).
					getMethodByParameters("writeNBT", base, DataOutput.class);
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
			
			// Use the base class
			methodLoad = FuzzyReflection.fromClass(base).
					getMethodByParameters("load", base, new Class<?>[] { DataInput.class });
		}
		
		try {
			return NbtFactory.fromNMS(methodLoad.invoke(null, source), null);
		} catch (Exception e) {
			throw new FieldAccessException("Unable to read NBT from " + source, e);
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
