package com.comphenix.protocol.injector.server;

/**
 * Able to store a socket injector.
 * <p>
 * A necessary hack.
 * @author Kristian
 */
class InjectContainer {
	private SocketInjector injector;

	public SocketInjector getInjector() {
		return injector;
	}

	public void setInjector(SocketInjector injector) {
		this.injector = injector;
	}
}