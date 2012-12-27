package com.comphenix.protocol.reflect.cloning;

/**
 * Represents a cloner that simply returns the given object.
 * 
 * @author Kristian
 */
public class IdentityCloner implements Cloner {
	@Override
	public boolean canClone(Object source) {
		return true;
	}

	@Override
	public Object clone(Object source) {
		return source;
	}
}
