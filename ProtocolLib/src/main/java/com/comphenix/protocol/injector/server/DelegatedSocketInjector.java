package com.comphenix.protocol.injector.server;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;

import org.bukkit.entity.Player;

/**
 * Represents a socket injector that delegates to a passed injector.
 * @author Kristian
 *
 */
class DelegatedSocketInjector implements SocketInjector {
	private volatile SocketInjector delegate;

	public DelegatedSocketInjector(SocketInjector delegate) {
		this.delegate = delegate;
	}

	@Override
	public void disconnect(String message) throws InvocationTargetException {
		delegate.disconnect(message);
	}
	@Override
	public SocketAddress getAddress() throws IllegalAccessException {
		return delegate.getAddress();
	}
	
	@Override
	public Player getPlayer() {
		return delegate.getPlayer();
	}
	
	@Override
	public Socket getSocket() throws IllegalAccessException {
		return delegate.getSocket();
	}
	
	@Override
	public Player getUpdatedPlayer() {
		return delegate.getUpdatedPlayer();
	}
	
	@Override
	public void sendServerPacket(Object packet, boolean filtered) throws InvocationTargetException {
		delegate.sendServerPacket(packet, filtered);
	}
	
	public SocketInjector getDelegate() {
		return delegate;
	}
	
	@Override
	public void transferState(SocketInjector delegate) {
		delegate.transferState(delegate);
	}
	
	public synchronized void setDelegate(SocketInjector delegate) {
		// Let the old delegate pass values to the new
		this.delegate.transferState(delegate);
		this.delegate = delegate;
	}
}
