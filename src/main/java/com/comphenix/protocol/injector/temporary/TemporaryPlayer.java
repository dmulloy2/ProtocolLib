package com.comphenix.protocol.injector.temporary;

/**
 * A temporary player created by ProtocolLib when a true player instance does not exist.
 * <p>
 * Also able to store a socket injector
 * </p>
 */
public class TemporaryPlayer {

	private volatile MinimalInjector injector;

	MinimalInjector getInjector() {
		return this.injector;
	}

	void setInjector(MinimalInjector injector) {
		if (injector == null) {
			throw new IllegalArgumentException("Injector cannot be NULL.");
		}

		this.injector = injector;
	}
}
