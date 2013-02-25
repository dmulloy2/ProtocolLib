package com.comphenix.protocol.injector.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

class DelegatedServerSocket extends ServerSocket {
	protected ServerSocket delegate;
	
	public DelegatedServerSocket(ServerSocket delegate) throws IOException {
		super();
		this.delegate = delegate;
	}
	
	@Override
	public void close() throws IOException {
		delegate.close();
	}
	
	@Override
	public Socket accept() throws IOException {
		return delegate.accept();
	}
	
	@Override
	public void bind(SocketAddress endpoint) throws IOException {
		delegate.bind(endpoint);
	}
	
	@Override
	public void bind(SocketAddress endpoint, int backlog) throws IOException {
		delegate.bind(endpoint, backlog);
	}
	
	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}
	
	@Override
	public ServerSocketChannel getChannel() {
		return delegate.getChannel();
	}
	
	@Override
	public InetAddress getInetAddress() {
		return delegate.getInetAddress();
	}
	
	@Override
	public int getLocalPort() {
		return delegate.getLocalPort();
	}
	
	@Override
	public SocketAddress getLocalSocketAddress() {
		return delegate.getLocalSocketAddress();
	}
	
	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {
		return delegate.getReceiveBufferSize();
	}
	
	@Override
	public boolean getReuseAddress() throws SocketException {
		return delegate.getReuseAddress();
	}
	
	@Override
	public synchronized int getSoTimeout() throws IOException {
		return delegate.getSoTimeout();
	}
	
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}
	
	@Override
	public boolean isBound() {
		return delegate.isBound();
	}
	
	@Override
	public boolean isClosed() {
		return delegate.isClosed();
	}
	
	@Override
	public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
		delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
	}
	
	@Override
	public synchronized void setReceiveBufferSize(int size) throws SocketException {
		delegate.setReceiveBufferSize(size);
	}
	
	@Override
	public void setReuseAddress(boolean on) throws SocketException {
		delegate.setReuseAddress(on);
	}
	
	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {
		delegate.setSoTimeout(timeout);
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
	
	public ServerSocket getDelegate() {
		return delegate;
	}
}
