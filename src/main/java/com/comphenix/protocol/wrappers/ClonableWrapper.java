package com.comphenix.protocol.wrappers;

public interface ClonableWrapper {
	Object getHandle();
	ClonableWrapper deepClone();

}
