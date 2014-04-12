package com.comphenix.protocol.wrappers.nbt.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;

public class NbtBinarySerializer {
	private static final Class<?> NBT_BASE_CLASS = MinecraftReflection.getNBTBaseClass();
	
	private interface LoadMethod {
		/**
		 * Load an NBT compound from a given stream.
		 * @param input - the input stream.
		 * @return The loaded NBT compound.
		 */
		public abstract Object loadNbt(DataInput input);
	}
	
	/**
	 * Load an NBT compound from the NBTBase static method pre-1.7.2.
	 */
	private static class LoadMethodNbtClass implements LoadMethod {
		private MethodAccessor accessor = getNbtLoadMethod(DataInput.class);
		
		@Override
		public Object loadNbt(DataInput input) {
			return accessor.invoke(null, input);
		}
	}
	
	/**
	 * Load an NBT compound from the NBTCompressedStreamTools static method in 1.7.2 - 1.7.5
	 */
	private static class LoadMethodWorldUpdate implements LoadMethod {
		private MethodAccessor accessor = getNbtLoadMethod(DataInput.class, int.class);
		
		@Override
		public Object loadNbt(DataInput input) {
			return accessor.invoke(null, input, 0);
		}
	}

	/**
	 * Load an NBT compound from the NBTCompressedStreamTools static method in 1.7.8
	 */
	private static class LoadMethodSkinUpdate implements LoadMethod {
		private Class<?> readLimitClass = MinecraftReflection.getNBTReadLimiterClass();
		private Object readLimiter = FuzzyReflection.fromClass(readLimitClass).getSingleton();
		private MethodAccessor accessor = getNbtLoadMethod(DataInput.class, int.class, readLimitClass);
		
		@Override
		public Object loadNbt(DataInput input) {
			return accessor.invoke(null, input, 0, readLimiter);
		}
	}
	
	// Used to read and write NBT
	private static Method methodWrite;
	
	/**
	 * Method selected for loading NBT compounds.
	 */
	private static LoadMethod loadMethod;
	
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
		LoadMethod method = loadMethod;
		
		if (loadMethod == null) {
			if (MinecraftReflection.isUsingNetty()) {
				try {
					method = new LoadMethodWorldUpdate();
				} catch (IllegalArgumentException e) {
					// Cannot find that method - must be in 1.7.8
					method = new LoadMethodSkinUpdate();
				}
			} else {
				method = new LoadMethodNbtClass();
			}
			
			// Save the selected method
			loadMethod = method;
		}
		
		try {
			return NbtFactory.fromNMS(method.loadNbt(source), null);
		} catch (Exception e) {
			throw new FieldAccessException("Unable to read NBT from " + source, e);
		}
	}
	
	private static MethodAccessor getNbtLoadMethod(Class<?>... parameters) {
		return Accessors.getMethodAccessor(getUtilityClass().getMethodByParameters("load", NBT_BASE_CLASS, parameters), true);
	}
	
	private static FuzzyReflection getUtilityClass() {
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
