/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * @author dmulloy2
 */

public class WrappedWatchableObject extends AbstractWrapper {
	private StructureModifier<Object> modifier;

	private WrappedWatchableObject() {
		super(MinecraftReflection.getDataWatcherItemClass());
	}

	/**
	 * Constructs a wrapped watchable object around an existing NMS data watcher item.
	 * @param handle Data watcher item
	 */
	public WrappedWatchableObject(Object handle) {
		this();
		setHandle(handle);

		modifier = new StructureModifier<Object>(handleType).withTarget(handle);
	}

	// ---- Getter methods

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

	public void setValue(Object value, boolean updateClient) {
		modifier.write(1, getUnwrapped(value));

		if (updateClient) {
			setDirtyState(true);
		}
	}

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

	public boolean getDirtyState() {
		return (boolean) modifier.read(2);
	}

	public void setDirtyState(boolean dirty) {
		modifier.write(2, dirty);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;

		if (obj instanceof WrappedWatchableObject) {
			WrappedWatchableObject other = (WrappedWatchableObject) obj;
			return other.handle.equals(handle);
		}

		return false;
	}
}