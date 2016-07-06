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
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.google.common.base.Optional;

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

	// ---- Wrapping

	/**
	 * Retrieve the wrapped object value, if needed. All non-primitive objects
	 * with {@link Serializer}s should be covered by this.
	 * 
	 * @param value - the raw NMS object to wrap.
	 * @return The wrapped object.
	 */
	@SuppressWarnings("rawtypes")
	static Object getWrapped(Object value) {
		// Deal with optionals first
		if (value instanceof Optional) {
			Optional<?> optional = (Optional<?>) value;
			if (optional.isPresent()) {
				return Optional.of(getWrapped(optional.get()));
			} else {
				return Optional.absent();
			}
		}

		// Current supported classes
		if (is(MinecraftReflection.getIChatBaseComponentClass(), value)) {
			return WrappedChatComponent.fromHandle(value);
		} else if (is(MinecraftReflection.getItemStackClass(), value)) {
			return BukkitConverters.getItemStackConverter().getSpecific(value);
		} else if (is(MinecraftReflection.getIBlockDataClass(), value)) {
			return BukkitConverters.getWrappedBlockDataConverter().getSpecific(value);
		} else if (is (Vector3F.getMinecraftClass(), value)) {
			return Vector3F.getConverter().getSpecific(value);
		} else if (is(MinecraftReflection.getBlockPositionClass(), value)) {
			return BlockPosition.getConverter().getSpecific(value);
		} else if (is(EnumWrappers.getDirectionClass(), value)) {
			return EnumWrappers.getDirectionConverter().getSpecific(value);
		}

		// Legacy classes
		if (is(MinecraftReflection.getChunkCoordinatesClass(), value)) {
			return new WrappedChunkCoordinate((Comparable) value);
		} else if (is(MinecraftReflection.getChunkPositionClass(), value)) {
			return ChunkPosition.getConverter().getSpecific(value);
		}

		return value;
	}

	private static boolean is(Class<?> clazz, Object object) {
		if (clazz == null || object == null) {
			return false;
		}

		return clazz.isAssignableFrom(object.getClass());
	}

	/**
	 * Retrieve the raw NMS value.
	 * 
	 * @param wrapped - the wrapped position.
	 * @return The raw NMS object.
	 */
	// Must be kept in sync with getWrapped!
	static Object getUnwrapped(Object wrapped) {
		if (wrapped instanceof Optional) {
			Optional<?> optional = (Optional<?>) wrapped;
			if (optional.isPresent()) {
				return Optional.of(getUnwrapped(optional.get()));
			} else {
				return Optional.absent();
			}
		}

		// Current supported classes
		if (wrapped instanceof WrappedChatComponent) {
			return ((WrappedChatComponent) wrapped).getHandle();
		} else if (wrapped instanceof ItemStack) {
			return BukkitConverters.getItemStackConverter().getGeneric(MinecraftReflection.getItemStackClass(), (ItemStack) wrapped);
		} else if (wrapped instanceof WrappedBlockData) {
			return BukkitConverters.getWrappedBlockDataConverter().getGeneric(MinecraftReflection.getIBlockDataClass(), (WrappedBlockData) wrapped);
		} else if (wrapped instanceof Vector3F) {
			return Vector3F.getConverter().getGeneric(Vector3F.getMinecraftClass(), (Vector3F) wrapped);
		} else if (wrapped instanceof BlockPosition) {
			return BlockPosition.getConverter().getGeneric(MinecraftReflection.getBlockPositionClass(), (BlockPosition) wrapped);
		} else if (wrapped instanceof Direction) {
			return EnumWrappers.getDirectionConverter().getGeneric(EnumWrappers.getDirectionClass(), (Direction) wrapped);
		}

		// Legacy classes
		if (wrapped instanceof ChunkPosition) {
			return ChunkPosition.getConverter().getGeneric(MinecraftReflection.getChunkPositionClass(), (ChunkPosition) wrapped);
		} else if (wrapped instanceof WrappedChunkCoordinate) {
			return ((WrappedChunkCoordinate) wrapped).getHandle();
		}

		return wrapped;
	}
}
