package com.comphenix.protocol.injector.packet;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a class that can detect if a map has changed.
 * @author Kristian
 */
public class MapContainer {
	// For detecting changes
	private final FieldAccessor modCountField;
	private int lastModCount;
	
	// The object along with whether or not this is the initial run
	private final Object source;
	private boolean changed;
	
	public MapContainer(Object source) {
		this.source = source;
		this.changed = false;

		this.modCountField = Accessors.getFieldAccessorOrNull(source.getClass(), "modCount", int.class);
		checkNotNull(this.modCountField, "unable to retrieve modCount field for " + source.getClass());

		this.lastModCount = getModificationCount();
	}
	
	/**
	 * Determine if the map has changed.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public boolean hasChanged() {
		// Check if unchanged
		checkChanged();
		return changed;
	}
	
	/**
	 * Mark the map as changed or unchanged.
	 * @param changed - TRUE if the map has changed, FALSE otherwise.
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	/**
	 * Check for modifications to the current map.
	 */
	protected void checkChanged() {
		if (!changed) {
			if (getModificationCount() != lastModCount) {
				lastModCount = getModificationCount();
				changed = true;
			}
		}
	}
	
	/**
	 * Retrieve the current modification count.
	 * @return The current count
	 */
	private int getModificationCount() {
		return (int) modCountField.get(source);
	}
}
