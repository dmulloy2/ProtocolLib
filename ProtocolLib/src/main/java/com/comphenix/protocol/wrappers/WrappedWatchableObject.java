/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;

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
	
	// Used to create new watchable objects
	private static Constructor<?> watchableConstructor;
	
	// The watchable object class type
	private static Class<?> watchableObjectClass;
	
	protected Object handle;
	protected StructureModifier<Object> modifier;
	
	// Type of the stored value
	private Class<?> typeClass;
	
	/**
	 * Wrap a given raw Minecraft watchable object.
	 * @param handle - the raw watchable object to wrap.
	 */
	public WrappedWatchableObject(Object handle) {
		load(handle);
	}
	
	/**
	 * Construct a watchable object from an index and a given value.
	 * @param index - the index.
	 * @param value - non-null value of specific types.
	 */
	public WrappedWatchableObject(int index, Object value) {
		if (value == null)
			throw new IllegalArgumentException("Value cannot be NULL.");
		
		// Get the correct type ID
		Integer typeID = WrappedDataWatcher.getTypeID(value.getClass());
		
		if (typeID != null) {
			if (watchableConstructor == null) {
				try {
					watchableConstructor = MinecraftReflection.getWatchableObjectClass().
											getConstructor(int.class, int.class, Object.class);
				} catch (Exception e) {
					throw new RuntimeException("Cannot get the WatchableObject(int, int, Object) constructor.", e); 
				}
			}
			
			// Create the object
			try {
				load(watchableConstructor.newInstance(typeID, index, getUnwrapped(value)));
			} catch (Exception e) {
				throw new RuntimeException("Cannot construct underlying WatchableObject.", e);
			}
		} else {
			throw new IllegalArgumentException("Cannot watch the type " + value.getClass());
		}
	}
	
	// Wrap a NMS object
	private void load(Object handle) {
		initialize();
		this.handle = handle;
		this.modifier = baseModifier.withTarget(handle);
		
		// Make sure the type is correct
		if (!watchableObjectClass.isAssignableFrom(handle.getClass())) {
			throw new ClassCastException("Cannot cast the class " + handle.getClass().getName() +
										 " to " + watchableObjectClass.getName());
		}
	}
	
	/**
	 * Retrieves the underlying watchable object.
	 * @return The underlying watchable object.
	 */
	public Object getHandle() {
		return handle;
	}
	
	/**
	 * Initialize reflection machinery.
	 */
	private static void initialize() {
		if (!hasInitialized) {
			hasInitialized = true;
			watchableObjectClass = MinecraftReflection.getWatchableObjectClass();
			baseModifier = new StructureModifier<Object>(watchableObjectClass, null, false);
		}
	}
	
	/**
	 * Retrieve the correct super type of the current value.
	 * @return Super type.
	 * @throws FieldAccessException Unable to read values.
	 */
	public Class<?> getType() throws FieldAccessException {
		return getWrappedType(getTypeRaw());
	}
	
	/**
	 * Retrieve the correct super type of the current value, given the raw NMS object.
	 * @return Super type.
	 * @throws FieldAccessException Unable to read values.
	 */
	private Class<?> getTypeRaw() throws FieldAccessException {
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
	 * <p>
	 * <table border=1>
	 * <tbody>
	 * <tr>
	 * <th>Type ID</th>
	 * <th>Data Type</th>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>Byte</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>Short</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>Int</td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td>Float</td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td>{@link String}</td>
	 * </tr>
	 * <tr>
	 * <td>5</td>
	 * <td>{@link org.bukkit.inventory.ItemStack ItemStack}</td>
	 * </tr>
	 * <tr>
	 * <td>6<sup>*</sup></td>
	 * <td>{@link com.comphenix.protocol.wrappers.ChunkPosition ChunkPosition}</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * @return Type ID that identifies the type of the value.
	 * @throws FieldAccessException Reflection failed.
	 */
	public int getTypeID() throws FieldAccessException {
		return modifier.<Integer>withType(int.class).read(0);
	}
	
	/**
	 * Set the type ID of a watchable object.
	 * @see {@link #getTypeID()} for more information.
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
		modifier.withType(Object.class).write(0, getUnwrapped(newValue));
	}
	
	/**
	 * Read the underlying value field.
	 * @return The underlying value.
	 */
	private Object getNMSValue() {
		return modifier.withType(Object.class).read(0);
	}
	
	/**
	 * Read the value field.
	 * @return The watched value.
	 * @throws FieldAccessException Unable to use reflection. 
	 */
	public Object getValue() throws FieldAccessException {
		return getWrapped(modifier.withType(Object.class).read(0));
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
	 * Retrieve the wrapped object value, if needed.
	 * @param value - the raw NMS object to wrap.
	 * @return The wrapped object.
	 */
	@SuppressWarnings("rawtypes")
	static Object getWrapped(Object value) {
    	// Handle the special cases
    	if (MinecraftReflection.isItemStack(value)) {
    		return BukkitConverters.getItemStackConverter().getSpecific(value);
    	} else if (MinecraftReflection.isChunkCoordinates(value)) {
    		return new WrappedChunkCoordinate((Comparable) value);
    	} else {
    		return value;
    	}
	}
	
	/**
	 * Retrieve the wrapped type, if needed.
	 * @param wrapped - the wrapped class type.
	 * @return The wrapped class type.
	 */
	static Class<?> getWrappedType(Class<?> unwrapped) {
		if (unwrapped.equals(MinecraftReflection.getChunkPositionClass()))
			return ChunkPosition.class;
		else if (unwrapped.equals(MinecraftReflection.getChunkCoordinatesClass()))
			return WrappedChunkCoordinate.class;
		else if (unwrapped.equals(MinecraftReflection.getItemStackClass())) 
			return ItemStack.class;
		else
			return unwrapped;
	}
	
	/**
	 * Retrieve the raw NMS value.
	 * @param wrapped - the wrapped position.
	 * @return The raw NMS object.
	 */
	static Object getUnwrapped(Object wrapped) {
    	// Convert special cases
		if (wrapped instanceof ChunkPosition)
			return ChunkPosition.getConverter().getGeneric(
				MinecraftReflection.getChunkPositionClass(), (ChunkPosition) wrapped);
		else if (wrapped instanceof WrappedChunkCoordinate)
    		return ((WrappedChunkCoordinate) wrapped).getHandle();
    	else if (wrapped instanceof ItemStack)
    		return BukkitConverters.getItemStackConverter().getGeneric(
    				MinecraftReflection.getItemStackClass(), (ItemStack) wrapped);
    	else
    		return wrapped;	
	}
	
	/**
	 * Retrieve the unwrapped type, if needed.
	 * @param wrapped - the unwrapped class type.
	 * @return The unwrapped class type.
	 */
	static Class<?> getUnwrappedType(Class<?> wrapped) {
		if (wrapped.equals(ChunkPosition.class))
			return MinecraftReflection.getChunkPositionClass(); 
		else if (wrapped.equals(WrappedChunkCoordinate.class))
			return MinecraftReflection.getChunkCoordinatesClass();
		else
			return wrapped;
	}
	
	/**
	 * Clone the current wrapped watchable object, along with any contained objects.
	 * @return A deep clone of the current watchable object.
	 * @throws FieldAccessException If we're unable to use reflection.
	 */
	public WrappedWatchableObject deepClone() throws FieldAccessException {
		WrappedWatchableObject clone = new WrappedWatchableObject(
				DefaultInstances.DEFAULT.getDefault(MinecraftReflection.getWatchableObjectClass()));
		
		clone.setDirtyState(getDirtyState());
		clone.setIndex(getIndex());
		clone.setTypeID(getTypeID());
		clone.setValue(getClonedValue(), false);
		return clone;
	}
	
	// Helper
	Object getClonedValue() throws FieldAccessException {
		Object value = getNMSValue();
		
		// Only a limited set of references types are supported
		if (MinecraftReflection.isChunkPosition(value)) {
			EquivalentConverter<ChunkPosition> converter = ChunkPosition.getConverter();
			return converter.getGeneric(MinecraftReflection.getChunkPositionClass(), converter.getSpecific(value));
		} else if (MinecraftReflection.isItemStack(value)) {
			return MinecraftReflection.getMinecraftItemStack(MinecraftReflection.getBukkitItemStack(value).clone());
		} else {
			// A string or primitive wrapper, which are all immutable.
			return value;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		// Quick checks
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		
		if (obj instanceof WrappedWatchableObject) {
			WrappedWatchableObject other = (WrappedWatchableObject) obj;
			
			return Objects.equal(getIndex(), other.getIndex()) &&
				   Objects.equal(getTypeID(), other.getTypeID()) &&
				   Objects.equal(getValue(), other.getValue());
		}
		
		// No, this is not equivalent
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getIndex(), getTypeID(), getValue());
	}
	
	@Override
	public String toString() {
		return String.format("[%s: %s (%s)]", getIndex(), getValue(), getType().getSimpleName());
	}
}
