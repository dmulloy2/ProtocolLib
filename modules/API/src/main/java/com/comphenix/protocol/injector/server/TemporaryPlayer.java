package com.comphenix.protocol.injector.server;

/**
 * A temporary player created by ProtocolLib when a true player instance does not exist.
 * <p>
 * Also able to store a socket injector
 * </p>
 */
public class TemporaryPlayer {
	private volatile SocketInjector injector;

	SocketInjector getInjector() {
		return injector;
	}

	void setInjector(SocketInjector injector) {
		if (injector == null)
			throw new IllegalArgumentException("Injector cannot be NULL.");
		this.injector = injector;
	}
}
