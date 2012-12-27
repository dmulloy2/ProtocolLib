package com.comphenix.protocol.reflect.cloning;

/**
 * Represents an object that is capable of cloning other objects.
 * 
 * @author Kristian
 */
public interface Cloner {
	/**
	 * Determine whether or not the current cloner can clone the given object.
	 * @param source - the object that is being considered.
	 * @return TRUE if this cloner can actually clone the given object, FALSE otherwise.
	 */
	public boolean canClone(Object source);
	
	/**
	 * Perform the clone. 
	 * <p>
	 * This method should never be called unless a corresponding {@link #canClone(Object)} returns TRUE.
	 * @param source - the value to clone.
	 * @return A cloned value.
	 * @throws IllegalArgumentException If this cloner cannot perform the clone.
	 */
	public Object clone(Object source);
}
