package com.comphenix.protocol.wrappers.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Factory methods for creating NBT elements, lists and compounds.
 * 
 * @author Kristian
 */
public class NbtFactory {
	// Used to create the underlying tag
	private static Method methodCreateTag;
	
	// Used to read and write NBT
	private static Method methodWrite;
	private static Method methodLoad;
	
	/**
	 * Get a NBT wrapper from a NBT base.
	 * @param base - the base class.
	 * @return A NBT wrapper.
	 */
	@SuppressWarnings("unchecked")
	public static <T> NbtWrapper<T> fromBase(NbtBase<T> base) {
		if (base instanceof NbtElement) {
			return (NbtElement<T>) base;
		} else if (base instanceof NbtCompound) {
			return (NbtWrapper<T>) base;
		} else if (base instanceof NbtList) {
			return (NbtWrapper<T>) base;
		} else {
			if (base.getType() == NbtType.TAG_COMPOUND) {
				// Load into a NBT-backed wrapper
				NbtCompound copy = NbtCompound.fromName(base.getName());
				T value = base.getValue();
				
				copy.setValue((Map<String, NbtBase<?>>) value);
				return (NbtWrapper<T>) copy;
			
			} else if (base.getType() == NbtType.TAG_LIST) {
				// As above
				NbtList<T> copy = NbtList.fromName(base.getName());
				
				copy.setValue((List<NbtBase<T>>) base.getValue());
				return (NbtWrapper<T>) copy;
				
			} else {
				// Copy directly
				NbtWrapper<T> copy = ofType(base.getType(), base.getName());
				
				copy.setValue(base.getValue());
				return copy;
			}
		}
	}
		
	/**
	 * Initialize a NBT wrapper.
	 * @param handle - the underlying net.minecraft.server object to wrap.
	 * @return A NBT wrapper.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> NbtWrapper<T> fromNMS(Object handle) {
		NbtElement<T> partial = new NbtElement<T>(handle);
		
		// See if this is actually a compound tag
		if (partial.getType() == NbtType.TAG_COMPOUND)
			return (NbtWrapper<T>) new NbtCompound(handle);
		else if (partial.getType() == NbtType.TAG_LIST)
			return new NbtList(handle);
		else
			return partial;
	}
	
	/**
	 * Write the content of a wrapped NBT tag to a stream.
	 * @param value - the NBT tag to write.
	 * @param destination - the destination stream.
	 */
	public static <TType> void toStream(NbtBase<TType> value, DataOutput destination) {
		if (methodWrite == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			
			// Use the base class
			methodWrite = FuzzyReflection.fromClass(base).
					getMethodByParameters("writeNBT", base, DataOutput.class);
		}
		
		try {
			methodWrite.invoke(null, fromBase(value).getHandle(), destination);
		} catch (Exception e) {
			throw new FieldAccessException("Unable to write NBT " + value, e);
		}
	}

	/**
	 * Load an NBT tag from a stream.
	 * @param source - the input stream.
	 * @return An NBT tag.
	 */
	public static NbtBase<?> fromStream(DataInput source) {
		if (methodLoad == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			
			// Use the base class
			methodLoad = FuzzyReflection.fromClass(base).
					getMethodByParameters("load", base, new Class<?>[] { DataInput.class });
		}
		
		try {
			return fromNMS(methodLoad.invoke(null, source));
		} catch (Exception e) {
			throw new FieldAccessException("Unable to read NBT from " + source, e);
		}
	}
	
	public static NbtBase<String> of(String name, String value) {
		return ofType(NbtType.TAG_STRING, name, value);
	}
	
	public static NbtBase<Byte> of(String name, byte value) {
		return ofType(NbtType.TAG_BYTE, name, value);
	}
	
	public static NbtBase<Short> of(String name, short value) {
		return ofType(NbtType.TAG_SHORT, name, value);
	}
	
	public static NbtBase<Integer> of(String name, int value) {
		return ofType(NbtType.TAG_INT, name, value);
	}
	
	public static NbtBase<Long> of(String name, long value) {
		return ofType(NbtType.TAG_LONG, name, value);
	}
	
	public static NbtBase<Float> of(String name, float value) {
		return ofType(NbtType.TAG_FLOAT, name, value);
	}
	
	public static NbtBase<Double> of(String name, double value) {
		return ofType(NbtType.TAG_DOUBlE, name, value);
	}
	
	public static NbtBase<byte[]> of(String name, byte[] value) {
		return ofType(NbtType.TAG_BYTE_ARRAY, name, value);
	}
	
	public static NbtBase<int[]> of(String name, int[] value) {
		return ofType(NbtType.TAG_INT_ARRAY, name, value);
	}
	
	/**
	 * Construct a new NBT compound wrapper initialized with a given list of NBT values.
	 * @param name - the name of the compound wrapper. 
	 * @param list - the list of elements to add.
	 * @return The new wrapped NBT compound.
	 */
	public static NbtCompound ofCompound(String name, Collection<? extends NbtBase<?>> list) {
		return NbtCompound.fromList(name, list);
	}
	
	/**
	 * Construct a new NBT compound wrapper.
	 * @param name - the name of the compound wrapper. 
	 * @return The new wrapped NBT compound.
	 */
	public static NbtCompound ofCompound(String name) {
		return NbtCompound.fromName(name);
	}
	
	/**
	 * Construct a NBT list of out an array of values.
	 * @param name - name of this list.
	 * @param elements - elements to add.
	 * @return The new filled NBT list.
	 */
	public static <T> NbtList<T> ofList(String name, T... elements) {
		return NbtList.fromArray(name, elements);
	}
	
	/**
	 * Create a new NBT wrapper from a given type.
	 * @param type - the NBT type.
	 * @param name - the name of the NBT tag.
	 * @return The new wrapped NBT tag.
	 * @throws FieldAccessException If we're unable to create the underlying tag.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> NbtWrapper<T> ofType(NbtType type, String name) {
		if (type == null)
			throw new IllegalArgumentException("type cannot be NULL.");
		if (type == NbtType.TAG_END)
			throw new IllegalArgumentException("Cannot create a TAG_END.");
		
		if (methodCreateTag == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			
			// Use the base class
			methodCreateTag = FuzzyReflection.fromClass(base).
				getMethodByParameters("createTag", base, new Class<?>[] { byte.class, String.class });
		}
		
		try {
			Object handle = methodCreateTag.invoke(null, (byte) type.getRawID(), name);
			
			if (type == NbtType.TAG_COMPOUND)
				return (NbtWrapper<T>) new NbtCompound(handle);
			else if (type == NbtType.TAG_LIST)
				return (NbtWrapper<T>) new NbtList(handle);
			else
				return new NbtElement<T>(handle);
			
		} catch (Exception e) {
			// Inform the caller
			throw new FieldAccessException(
					String.format("Cannot create NBT element %s (type: %s)", name, type),
					e);
		}
	}
	
	/**
	 * Create a new NBT wrapper from a given type.
	 * @param type - the NBT type.
	 * @param name - the name of the NBT tag.
	 * @param value - the value of the new tag.
	 * @return The new wrapped NBT tag.
	 * @throws FieldAccessException If we're unable to create the underlying tag.
	 */
	public static <T> NbtWrapper<T> ofType(NbtType type, String name, T value) {
		NbtWrapper<T> created = ofType(type, name);
		
		// Update the value
		created.setValue(value);
		return created;
	}
	
	/**
	 * Create a new NBT wrapper from a given type.
	 * @param type - type of the NBT value.
	 * @param name - the name of the NBT tag.
	 * @param value - the value of the new tag.
	 * @return The new wrapped NBT tag.
	 * @throws FieldAccessException If we're unable to create the underlying tag.
	 * @throws IllegalArgumentException If the given class type is not valid NBT.
	 */
	public static <T> NbtWrapper<T> ofType(Class<?> type, String name, T value) {
		return ofType(NbtType.getTypeFromClass(type), name, value);
	}
}
