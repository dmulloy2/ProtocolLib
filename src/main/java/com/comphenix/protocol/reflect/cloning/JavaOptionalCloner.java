/**
 * (c) 2018 dmulloy2
 */
package com.comphenix.protocol.reflect.cloning;

import java.util.Optional;
import java.util.OptionalInt;

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
		return source instanceof Optional || source instanceof OptionalInt;
	}

	@Override
	public Object clone(Object source) {
		if (source instanceof Optional) {
			Optional<?> optional = (Optional<?>) source;
			return optional.map(o -> wrapped.clone(o));
		} else if (source instanceof OptionalInt) {
			// why Java felt the need to make each optional class distinct is beyond me
			// like why couldn't they have given us at least a common interface or something
			OptionalInt optional = (OptionalInt) source;
			return optional.isPresent() ? OptionalInt.of(optional.getAsInt()) : OptionalInt.empty();
		}

		return null;
	}

	public Cloner getWrapped() {
		return wrapped;
	}
}
