/**
 * (c) 2018 dmulloy2
 */
package com.comphenix.protocol.reflect.cloning;

import java.util.Optional;

/**
 * A cloner that can clone Java Optional objects
 * @author dmulloy2
 */
public class JavaOptionalCloner implements Cloner {
	protected Cloner wrapped;

	public JavaOptionalCloner(Cloner wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean canClone(Object source) {
		return source instanceof Optional;
	}

	@Override
	public Object clone(Object source) {
		Optional<?> optional = (Optional<?>) source;
		return optional.map(o -> wrapped.clone(o));
	}

	public Cloner getWrapped() {
		return wrapped;
	}
}
