/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * @author dmulloy2
 */

public class WrappedWatchableObject extends AbstractWrapper {

	private WrappedWatchableObject() {
		super(MinecraftReflection.getDataWatcherItemClass());
	}

	public WrappedWatchableObject(Object handle) {
		this();
		setHandle(handle);
	}

	public Object getValue() {
		return new StructureModifier<Object>(handleType).withTarget(this).read(1);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;

		if (obj instanceof WrappedWatchableObject) {
			WrappedWatchableObject other = (WrappedWatchableObject) obj;
			return other.handle.equals(handle);
		}

		return false;
	}

	// TODO Flesh out this class
}