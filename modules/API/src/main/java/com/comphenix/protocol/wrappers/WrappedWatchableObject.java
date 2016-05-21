/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2016 dmulloy2
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

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

/**
 * Represents a DataWatcher Item in 1.9.
 * @author dmulloy2
 */

public class WrappedWatchableObject extends AbstractWrapper {
	private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherItemClass();
	private static ConstructorAccessor constructor;

	private final StructureModifier<Object> modifier;

	/**
	 * Constructs a wrapped watchable object around an existing NMS data watcher item.
	 * @param handle Data watcher item
	 */
	public WrappedWatchableObject(Object handle) {
		super(HANDLE_TYPE);
		setHandle(handle);
		this.modifier = new StructureModifier<Object>(handleType).withTarget(handle);
	}

	/**
	 * Constructs a wrapped watchable object with a given watcher object and initial value.
	 * @param watcherObject Watcher object
	 * @param value Initial value
	 */
	public WrappedWatchableObject(WrappedDataWatcherObject watcherObject, Object value) {
		this(newHandle(watcherObject, value));
	}

	private static Object newHandle(WrappedDataWatcherObject watcherObject, Object value) {
		if (constructor == null) {
			constructor = Accessors.getConstructorAccessor(HANDLE_TYPE.getConstructors()[0]);
		}

		return constructor.invoke(watcherObject.getHandle(), value);
	}

	// ---- Getter methods

	/**
	 * Gets this Item's watcher object, which contains the index and serializer.
	 * @return The watcher object
	 */
	public WrappedDataWatcherObject getWatcherObject() {
		return new WrappedDataWatcherObject(modifier.read(0));
	}

	/**
	 * Gets this Item's index from the watcher object
	 * @return The index
	 */
	public int getIndex() {
		return getWatcherObject().getIndex();
	}

	/**
	 * Gets the wrapped value of this data watcher item.
	 * @return The wrapped value
	 */
	public Object getValue() {
		return getWrapped(getRawValue());
	}

	/**
	 * Gets the raw value of this data watcher item.
	 * @return Raw value
	 */
	public Object getRawValue() {
		return modifier.readSafely(1);
	}

	/**
	 * Retrieve the wrapped object value, if needed.
	 * 
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
	 * 
	 * @param wrapped - the wrapped class type.
	 * @return The wrapped class type.
	 */
	static Class<?> getWrappedType(Class<?> unwrapped) {
		if (unwrapped.equals(MinecraftReflection.getChunkPositionClass()))
			return ChunkPosition.class;
		else if (unwrapped.equals(MinecraftReflection.getBlockPositionClass()))
			return BlockPosition.class;
		else if (unwrapped.equals(MinecraftReflection.getChunkCoordinatesClass()))
			return WrappedChunkCoordinate.class;
		else if (unwrapped.equals(MinecraftReflection.getItemStackClass()))
			return ItemStack.class;
		else
			return unwrapped;
	}

	/**
	 * Sets the value of this item.
	 * @param value New value
	 * @param updateClient Whether or not to update the client
	 */
	public void setValue(Object value, boolean updateClient) {
		modifier.write(1, getUnwrapped(value));

		if (updateClient) {
			setDirtyState(true);
		}
	}

	/**
	 * Sets the value of this item.
	 * @param value New value
	 */
	public void setValue(Object value) {
		setValue(value, false);
	}

	/**
	 * Retrieve the raw NMS value.
	 * 
	 * @param wrapped - the wrapped position.
	 * @return The raw NMS object.
	 */
	static Object getUnwrapped(Object wrapped) {
		// Convert special cases
		if (wrapped instanceof ChunkPosition)
			return ChunkPosition.getConverter().getGeneric(MinecraftReflection.getChunkPositionClass(), (ChunkPosition) wrapped);
		else if (wrapped instanceof BlockPosition)
			return BlockPosition.getConverter().getGeneric(MinecraftReflection.getBlockPositionClass(), (BlockPosition) wrapped);
		else if (wrapped instanceof WrappedChunkCoordinate)
			return ((WrappedChunkCoordinate) wrapped).getHandle();
		else if (wrapped instanceof ItemStack)
			return BukkitConverters.getItemStackConverter().getGeneric(MinecraftReflection.getItemStackClass(), (ItemStack) wrapped);
		else
			return wrapped;
	}

	/**
	 * Retrieve the unwrapped type, if needed.
	 * 
	 * @param wrapped - the unwrapped class type.
	 * @return The unwrapped class type.
	 */
	static Class<?> getUnwrappedType(Class<?> wrapped) {
		if (wrapped.equals(ChunkPosition.class))
			return MinecraftReflection.getChunkPositionClass();
		else if (wrapped.equals(BlockPosition.class))
			return MinecraftReflection.getBlockPositionClass();
		else if (wrapped.equals(WrappedChunkCoordinate.class))
			return MinecraftReflection.getChunkCoordinatesClass();
		else if (ItemStack.class.isAssignableFrom(wrapped))
			return MinecraftReflection.getItemStackClass();
		else
			return wrapped;
	}

	/**
	 * Whether or not the value must be synchronized with the client.
	 * @return True if it must, false if not
	 */
	public boolean getDirtyState() {
		return (boolean) modifier.read(2);
	}

	/**
	 * Sets this item's dirty state
	 * @param dirty New state
	 */
	public void setDirtyState(boolean dirty) {
		modifier.write(2, dirty);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;

		if (obj instanceof WrappedWatchableObject) {
			// watcher object, value, dirty state
			WrappedWatchableObject other = (WrappedWatchableObject) obj;
			return getWatcherObject().equals(other.getWatcherObject())
					&& getRawValue().equals(other.getRawValue())
					&& getDirtyState() == other.getDirtyState();
		}

		return false;
	}

	@Override
	public String toString() {
		return "DataWatcherItem[object=" + getWatcherObject() + ", value=" + getValue() + ", dirty=" + getDirtyState() + "]";
	}
}
