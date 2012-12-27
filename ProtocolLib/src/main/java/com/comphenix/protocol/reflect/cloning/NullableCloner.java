package com.comphenix.protocol.reflect.cloning;

/**
 * Creates a cloner wrapper that accepts and clones NULL values.
 * 
 * @author Kristian
 */
public class NullableCloner implements Cloner {
	protected Cloner wrapped;
	
	public NullableCloner(Cloner wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean canClone(Object source) {
		return true;
	}

	@Override
	public Object clone(Object source) {
		// Don't pass the NULL value to the cloner
		if (source == null)
			return null;
		else
			return wrapped.clone(source);
	}

	public Cloner getWrapped() {
		return wrapped;
	}
}
