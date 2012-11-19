package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;

import net.minecraft.server.ItemStack;
import net.minecraft.server.WatchableObject;

/**
 * Represents a watchable object.
 * 
 * @author Kristian
 */
public class WrappedWatchableObject {

	// Whether or not the reflection machinery has been initialized
	private static boolean hasInitialized;
	
	// The field containing the value itself
	private static StructureModifier<Object> baseModifier;
	
	protected WatchableObject handle;
	protected StructureModifier<Object> modifier;
	
	// Type of the stored value
	private Class<?> typeClass;
	
	public WrappedWatchableObject(WatchableObject handle) {
		initialize();
		this.handle = handle;
		this.modifier = baseModifier.withTarget(handle);
	}
	
	/**
	 * Retrieves the underlying watchable object.
	 * @return The underlying watchable object.
	 */
	public WatchableObject getHandle() {
		return handle;
	}
	
	/**
	 * Initialize reflection machinery.
	 */
	private static void initialize() {
		if (!hasInitialized) {
			hasInitialized = true;
			baseModifier = new StructureModifier<Object>(WatchableObject.class, null, false);
		}
	}
	
	/**
	 * Retrieve the correct super type of the current value.
	 * @return Super type.
	 * @throws FieldAccessException Unable to read values.
	 */
	public Class<?> getType() throws FieldAccessException {
		if (typeClass == null) {
			typeClass = WrappedDataWatcher.getTypeClass(getTypeID());
			
			if (typeClass == null) {
				throw new IllegalStateException("Unrecognized data type: " + getTypeID());
			}
		}
		
		return typeClass;
	}
	
	/**
	 * Retrieve the index of this watchable object. This is used to identify a value.
	 * @return Object index.
	 * @throws FieldAccessException Reflection failed.
	 */
	public int getIndex() throws FieldAccessException {
		return modifier.<Integer>withType(int.class).read(1);
	}
	
	/**
	 * Set the the index of this watchable object.
	 * @param index - the new object index.
	 * @throws FieldAccessException Reflection failed.
	 */
	public void setIndex(int index) throws FieldAccessException {
		modifier.<Integer>withType(int.class).write(1, index);
	}
	
	/**
	 * Retrieve the type ID of a watchable object.
	 * @return Type ID that identifies the type of the value.
	 * @throws FieldAccessException Reflection failed.
	 */
	public int getTypeID() throws FieldAccessException {
		return modifier.<Integer>withType(int.class).read(0);
	}
	
	/**
	 * Set the type ID of a watchable object.
	 * @param id - the new ID.
	 * @throws FieldAccessException Reflection failed.
	 */
	public void setTypeID(int id) throws FieldAccessException {
		modifier.<Integer>withType(int.class).write(0, id);
	}
	
	/**
	 * Update the value field.
	 * @param newValue - new value.
	 * @throws FieldAccessException Unable to use reflection.
	 */
	public void setValue(Object newValue) throws FieldAccessException {
		setValue(newValue, true);
	}
	
	/**
	 * Update the value field.
	 * @param newValue - new value.
	 * @param updateClient - whether or not to update listening clients.
	 * @throws FieldAccessException Unable to use reflection.
	 */
	public void setValue(Object newValue, boolean updateClient) throws FieldAccessException {
		// Verify a few quick things
		if (newValue == null)
			throw new IllegalArgumentException("Cannot watch a NULL value.");
		if (!getType().isAssignableFrom(newValue.getClass()))
			throw new IllegalArgumentException("Object " + newValue +  " must be of type " + getType().getName());
		
		// See if we should update the client to
		if (updateClient)
			setDirtyState(true);
		
		// Use the modifier to set the value
		modifier.withType(Object.class).write(0, newValue);
	}
	
	/**
	 * Read the value field.
	 * @return The watched value.
	 * @throws FieldAccessException Unable to use reflection. 
	 */
	public Object getValue() throws FieldAccessException {
		return modifier.withType(Object.class).read(0);
	}
	
	/**
	 * Set whether or not the value must be synchronized with the client.
	 * @param dirty - TRUE if the value should be synchronized, FALSE otherwise.
	 * @throws FieldAccessException Unable to use reflection.
	 */
	public void setDirtyState(boolean dirty) throws FieldAccessException {
		modifier.<Boolean>withType(boolean.class).write(0, dirty);
	}
	
	/**
	 * Retrieve whether or not the value must be synchronized with the client.
	 * @return TRUE if the value should be synchronized, FALSE otherwise.
	 * @throws FieldAccessException Unable to use reflection.
	 */
	public boolean getDirtyState() throws FieldAccessException {
		return modifier.<Boolean>withType(boolean.class).read(0);
	}
	
	/**
	 * Clone the current wrapped watchable object, along with any contained objects.
	 * @return A deep clone of the current watchable object.
	 * @throws FieldAccessException If we're unable to use reflection.
	 */
	public WrappedWatchableObject deepClone() throws FieldAccessException {
		WrappedWatchableObject clone = new WrappedWatchableObject(DefaultInstances.DEFAULT.getDefault(WatchableObject.class));
		
		clone.setDirtyState(getDirtyState());
		clone.setIndex(getIndex());
		clone.setTypeID(getTypeID());
		clone.setValue(getClonedValue(), false);
		return clone;
	}
	
	// Helper
	private Object getClonedValue() throws FieldAccessException {
		Object value = getValue();
		
		// Only a limited set of references types are supported
		if (value instanceof net.minecraft.server.ChunkPosition) {
			EquivalentConverter<ChunkPosition> converter = ChunkPosition.getConverter();
			return converter.getGeneric(net.minecraft.server.ChunkPosition.class, converter.getSpecific(value));
		} else if (value instanceof ItemStack) {
			return ((ItemStack) value).cloneItemStack();
		} else {
			// A string or primitive wrapper, which are all immutable.
			return value;
		}
	}
}
