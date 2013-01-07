package com.comphenix.protocol.wrappers.nbt;

import java.io.DataOutput;
import java.lang.reflect.Method;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;

/**
 * Represents an arbitrary NBT tag element, composite or not.
 * <p>
 * Use {@link NbtFactory} to load or create an instance.
 * @author Kristian
 *
 * @param <TType> - type of the value field.
 */
public class NbtElement<TType> implements NbtWrapper<TType> {	
	// Structure modifier for the base class 
	private static volatile StructureModifier<Object> baseModifier;
	
	// For retrieving the current type ID
	private static volatile Method methodGetTypeID;
	
	// For cloning handles
	private static volatile Method methodClone;
	
	// Structure modifiers for the different NBT elements
	private static StructureModifier<?>[] modifiers = new StructureModifier<?>[NbtType.values().length];
	
	// The underlying NBT object
	private Object handle;
	
	// Saved type
	private NbtType type;
	
	/**
	 * Initialize a NBT wrapper for a generic element.
	 * @param handle - the NBT element to wrap.
	 */
	NbtElement(Object handle) {
		this.handle = handle;
	}
		
	/**
	 * Retrieve the modifier (with no target) that is used to read and write the NBT name.
	 * @return A modifier for accessing the NBT name.
	 */
	protected static StructureModifier<String> getBaseModifier() {
		if (baseModifier == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();

			// This will be the same for all classes, so we'll share modifier
			baseModifier = new StructureModifier<Object>(base, Object.class, false).withType(String.class);
		}
		
		return baseModifier.withType(String.class);
	}
	
	/**
	 * Retrieve a modifier (with no target) that is used to read and write the NBT value.
	 * @return The value modifier.
	 */
	protected StructureModifier<TType> getCurrentModifier() {
		NbtType type = getType();
		
		return getCurrentBaseModifier().withType(type.getValueType());
	}
	
	/**
	 * Get the object modifier (with no target) for the current underlying NBT object.
	 * @return The generic modifier.
	 */
	@SuppressWarnings("unchecked")
	protected StructureModifier<Object> getCurrentBaseModifier() {
		int index = getType().ordinal();
		StructureModifier<Object> modifier = (StructureModifier<Object>) modifiers[index];
		
		// Double checked locking
		if (modifier == null) {
			synchronized (this) {
				if (modifiers[index] == null) {
					modifiers[index] = new StructureModifier<Object>(handle.getClass(), MinecraftReflection.getNBTBaseClass(), false);
				}
				modifier = (StructureModifier<Object>) modifiers[index];
			}
		}
		
		return modifier;
	}
	
	/**
	 * Retrieve the underlying NBT tag object.
	 * @return The underlying Minecraft tag object.
	 */
	@Override
	public Object getHandle() {
		return handle;
	}
	
	@Override
	public NbtType getType() {
		if (methodGetTypeID == null) {
			// Use the base class
			methodGetTypeID = FuzzyReflection.fromClass(MinecraftReflection.getNBTBaseClass()).
				getMethodByParameters("getTypeID", byte.class, new Class<?>[0]);
		}
		if (type == null) {
			try {
				type = NbtType.getTypeFromID((Byte) methodGetTypeID.invoke(handle));
			} catch (Exception e) {
				throw new FieldAccessException("Cannot get NBT type of " + handle, e);
			}
		}
		
		return type;
	}
	
	NbtType getSubType() {
		int subID = getCurrentBaseModifier().<Byte>withType(byte.class).withTarget(handle).read(0);
		return NbtType.getTypeFromID(subID);
	}
	
	void setSubType(NbtType type) {
		byte subID = (byte) type.getRawID();
		getCurrentBaseModifier().<Byte>withType(byte.class).withTarget(handle).write(0, subID);
	}
	
	@Override
	public String getName() {
		return getBaseModifier().withTarget(handle).read(0);
	}
	
	@Override
	public void setName(String name) {
		getBaseModifier().withTarget(handle).write(0, name);
	}
	
	@Override
	public TType getValue() {
		return getCurrentModifier().withTarget(handle).read(0);
	}
	
	@Override
	public void setValue(TType newValue) {
		getCurrentModifier().withTarget(handle).write(0, newValue);
	}
	
	@Override
	public void write(DataOutput destination) {
		NbtFactory.toStream(this, destination);
	}
	
	@Override
	public NbtBase<TType> deepClone() {
		if (methodClone == null) {
			Class<?> base = MinecraftReflection.getNBTBaseClass();
			
			// Use the base class
			methodClone = FuzzyReflection.fromClass(base).
					getMethodByParameters("clone", base, new Class<?>[0]);
		}
		
		try {
			return NbtFactory.fromNMS(methodClone.invoke(handle));
		} catch (Exception e) {
			throw new FieldAccessException("Unable to clone " + handle, e);
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getName(), getType(), getValue());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NbtBase) {
			NbtBase<?> other = (NbtBase<?>) obj;
			
			// Make sure we're dealing with the same type
			if (other.getType().equals(getType())) {
				return Objects.equal(getName(), other.getName()) &&
					   Objects.equal(getValue(), other.getValue());
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String name = getName();
		
		result.append("{");
		
		if (name != null && name.length() > 0)
			result.append("name: '" + name + "', ");
		
		result.append("value: ");
		
		// Wrap quotation marks
		if (getType() == NbtType.TAG_STRING)
			result.append("'" + getValue() + "'");
		else
			result.append(getValue());
		
		result.append("}");
		return result.toString();
	}
}
