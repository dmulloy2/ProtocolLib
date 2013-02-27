package com.comphenix.protocol.injector.server;

/**
 * Able to store a socket injector.
 * <p>
 * A necessary hack.
 * @author Kristian
 */
class InjectorContainer {
	private volatile SocketInjector injector;

	public SocketInjector getInjector() {
		return injector;
	}

	public void setInjector(SocketInjector injector) {
		if (injector == null)
			throw new IllegalArgumentException("Injector cannot be NULL.");
		this.injector = injector;
	}
}