package com.comphenix.protocol.wrappers.nbt;

import java.util.Collection;
import java.util.Set;

abstract class ConvertedSet<VInner, VOuter> extends ConvertedCollection<VInner, VOuter> implements Set<VOuter> {
	public ConvertedSet(Collection<VInner> inner) {
		super(inner);
	}
}
