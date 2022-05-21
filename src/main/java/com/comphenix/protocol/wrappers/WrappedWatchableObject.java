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

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Optional;
import org.bukkit.inventory.ItemStack;

import static com.comphenix.protocol.utility.MinecraftReflection.is;

/**
 * Represents a DataWatcher Item in 1.8 thru 1.10.
 * @author dmulloy2
 */
public class WrappedWatchableObject extends AbstractWrapper {
	private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherItemClass();
	private static Integer VALUE_INDEX = null;

	private static ConstructorAccessor constructor;

	private final StructureModifier<Object> modifier;

	/**
	 * Constructs a DataWatcher Item wrapper from an existing NMS data watcher item.
	 * @param handle Data watcher item
	 */
	public WrappedWatchableObject(Object handle) {
		super(HANDLE_TYPE);
		setHandle(handle);
		this.modifier = new StructureModifier<Object>(handleType).withTarget(handle);
	}

	/**
	 * Constructs a DataWatcher Item wrapper from a given index and initial value.
	 * <p>
	 * Not recommended in 1.9 and up.
	 * @param index Index of the Item
	 * @param value Initial value
	 */
	public WrappedWatchableObject(int index, Object value) {
		this(newHandle(WrappedDataWatcherObject.fromIndex(index), value));
	}

	/**
	 * Constructs a DataWatcher Item wrapper from a given watcher object and initial value.
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

		if (MinecraftReflection.watcherObjectExists()) {
			return constructor.invoke(watcherObject.getHandle(), value);
		} else {
			// new WatchableObject(classId, index, value)
			return constructor.invoke(WrappedDataWatcher.getTypeID(value.getClass()), watcherObject.getIndex(), value);
		}
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
		if (MinecraftReflection.watcherObjectExists()) {
			return getWatcherObject().getIndex();
		}

		return modifier.<Integer>withType(int.class).read(1);
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
		if (VALUE_INDEX == null) {
			VALUE_INDEX = MinecraftReflection.watcherObjectExists() ? 1 : 2;
		}

		return modifier.readSafely(VALUE_INDEX);
	}

	/**
	 * Sets the value of this item.
	 * @param value New value
	 * @param updateClient Whether or not to update the client
	 */
	public void setValue(Object value, boolean updateClient) {
		if (VALUE_INDEX == null) {
			VALUE_INDEX = MinecraftReflection.watcherObjectExists() ? 1 : 2;
		}

		modifier.write(VALUE_INDEX, getUnwrapped(value));

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
		return modifier.<Boolean>withType(boolean.class).read(0);
	}

	/**
	 * Sets this item's dirty state
	 * @param dirty New state
	 */
	public void setDirtyState(boolean dirty) {
		modifier.<Boolean>withType(boolean.class).write(0, dirty);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;

		if (obj instanceof WrappedWatchableObject) {
			WrappedWatchableObject that = (WrappedWatchableObject) obj;
			return this.getIndex() == that.getIndex() &&
					this.getRawValue().equals(that.getRawValue()) &&
					this.getDirtyState() == that.getDirtyState();
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIndex();
		result = prime * result + getRawValue().hashCode();
		result = prime * result + (getDirtyState() ? 1231 : 1237);
		return result;
	}

	@Override
	public String toString() {
		return "DataWatcherItem[index=" + getIndex() + ", value=" + getValue() + ", dirty=" + getDirtyState() + "]";
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
		// Handle watcher items first
		if (is(MinecraftReflection.getDataWatcherItemClass(), value)) {
			return getWrapped(new WrappedWatchableObject(value).getRawValue());
		}

		// Then deal with optionals
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
		} else if (is(MinecraftReflection.getNBTCompoundClass(), value)) {
			return NbtFactory.fromNMSCompound(value);
		}

		// Legacy classes
		if (is(MinecraftReflection.getChunkCoordinatesClass(), value)) {
			return new WrappedChunkCoordinate((Comparable) value);
		} else if (is(MinecraftReflection.getChunkPositionClass(), value)) {
			return ChunkPosition.getConverter().getSpecific(value);
		}

		return value;
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
			return BukkitConverters.getItemStackConverter().getGeneric((ItemStack) wrapped);
		} else if (wrapped instanceof WrappedBlockData) {
			return BukkitConverters.getWrappedBlockDataConverter().getGeneric((WrappedBlockData) wrapped);
		} else if (wrapped instanceof Vector3F) {
			return Vector3F.getConverter().getGeneric((Vector3F) wrapped);
		} else if (wrapped instanceof BlockPosition) {
			return BlockPosition.getConverter().getGeneric((BlockPosition) wrapped);
		} else if (wrapped instanceof Direction) {
			return EnumWrappers.getDirectionConverter().getGeneric((Direction) wrapped);
		} else if (wrapped instanceof NbtCompound) {
			return NbtFactory.fromBase((NbtCompound) wrapped).getHandle();
		}

		// Legacy classes
		if (wrapped instanceof ChunkPosition) {
			return ChunkPosition.getConverter().getGeneric((ChunkPosition) wrapped);
		} else if (wrapped instanceof WrappedChunkCoordinate) {
			return ((WrappedChunkCoordinate) wrapped).getHandle();
		}

		return wrapped;
	}
}
