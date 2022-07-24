package com.comphenix.protocol.wrappers.nbt.io;

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
import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;

public class NbtBinarySerializer {

	/**
	 * Retrieve a default instance of the NBT binary serializer.
	 */
	public static final NbtBinarySerializer DEFAULT = new NbtBinarySerializer();
	private static final Class<?> NBT_BASE_CLASS = MinecraftReflection.getNBTBaseClass();

	// Used to read and write NBT
	private static MethodAccessor methodWrite;

	/**
	 * Method selected for loading NBT compounds.
	 */
	private static LoadMethod loadMethod;

	private static MethodAccessor getNbtLoadMethod(Class<?>... parameters) {
		Method method = getUtilityClass().getMethodByReturnTypeAndParameters("load", NBT_BASE_CLASS, parameters);
		return Accessors.getMethodAccessor(method);
	}

	private static FuzzyReflection getUtilityClass() {
		return FuzzyReflection.fromClass(MinecraftReflection.getNbtCompressedStreamToolsClass(), true);
	}

	/**
	 * Write the content of a wrapped NBT tag to a stream.
	 *
	 * @param <T>         Type
	 * @param value       - the NBT tag to write.
	 * @param destination - the destination stream.
	 */
	public <T> void serialize(NbtBase<T> value, DataOutput destination) {
		if (methodWrite == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			Method writeNBT = getUtilityClass().getMethodByParameters("writeNBT", base, DataOutput.class);

			methodWrite = Accessors.getMethodAccessor(writeNBT);
		}

		methodWrite.invoke(null, NbtFactory.fromBase(value).getHandle(), destination);
	}

	/**
	 * Load an NBT tag from a stream.
	 *
	 * @param <TType> Type
	 * @param source  - the input stream.
	 * @return An NBT tag.
	 */
	public <TType> NbtWrapper<TType> deserialize(DataInput source) {
		if (loadMethod == null) {
			// Save the selected method
			loadMethod = new LoadMethodSkinUpdate();
		}

		try {
			return NbtFactory.fromNMS(loadMethod.loadNbt(source), null);
		} catch (Exception e) {
			throw new FieldAccessException("Unable to read NBT from " + source, e);
		}
	}

	/**
	 * Load an NBT compound from a stream.
	 *
	 * @param source - the input stream.
	 * @return An NBT compound.
	 */
	public NbtCompound deserializeCompound(DataInput source) {
		// I always seem to override generics ...
		return (NbtCompound) (NbtBase<?>) this.deserialize(source);
	}

	/**
	 * Load an NBT list from a stream.
	 *
	 * @param <T>    Type
	 * @param source - the input stream.
	 * @return An NBT list.
	 */
	@SuppressWarnings("unchecked")
	public <T> NbtList<T> deserializeList(DataInput source) {
		return (NbtList<T>) (NbtBase<?>) this.deserialize(source);
	}

	private interface LoadMethod {

		/**
		 * Load an NBT compound from a given stream.
		 *
		 * @param input - the input stream.
		 * @return The loaded NBT compound.
		 */
		Object loadNbt(DataInput input);
	}

	/**
	 * Load an NBT compound from the NBTCompressedStreamTools static method since 1.7.
	 */
	private static class LoadMethodSkinUpdate implements LoadMethod {

		private final Class<?> readLimitClass = MinecraftReflection.getNBTReadLimiterClass();
		private final Object readLimiter = FuzzyReflection.fromClass(this.readLimitClass).getSingleton();
		private final MethodAccessor accessor = getNbtLoadMethod(DataInput.class, int.class, this.readLimitClass);

		@Override
		public Object loadNbt(DataInput input) {
			return this.accessor.invoke(null, input, 0, this.readLimiter);
		}
	}
}
