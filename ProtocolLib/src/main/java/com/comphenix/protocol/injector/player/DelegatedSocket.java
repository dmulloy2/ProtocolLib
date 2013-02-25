package com.comphenix.protocol.injector.player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

// This is a fixed JVM class, so there's probably no need to use CGLib
class DelegatedSocket extends Socket {
	protected Socket delegate;
	
	public DelegatedSocket(Socket delegate) {
		super();
		this.delegate = delegate;
	}	
	
	@Override
	public void bind(SocketAddress arg0) throws IOException {		
		delegate.bind(arg0);
	}
	
	@Override
	public synchronized void close() throws IOException {		
		delegate.close();
	}
	
	@Override
	public void connect(SocketAddress endpoint) throws IOException {		
		delegate.connect(endpoint);
	}

	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {		
		delegate.connect(endpoint, timeout);
	}
	
	@Override
	public boolean equals(Object obj) {		
		return delegate.equals(obj);
	}
	
	@Override
	public SocketChannel getChannel() {		
		return delegate.getChannel();
	}
	
	@Override
	public InetAddress getInetAddress() {		
		return delegate.getInetAddress();
	}
	
	@Override
	public InputStream getInputStream() throws IOException {		
		return delegate.getInputStream();
	}
	
	@Override
	public boolean getKeepAlive() throws SocketException {		
		return delegate.getKeepAlive();
	}
	
	@Override
	public InetAddress getLocalAddress() {		
		return delegate.getLocalAddress();
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
	public boolean getOOBInline() throws SocketException {		
		return delegate.getOOBInline();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {		
		return delegate.getOutputStream();
	}
	
	@Override
	public int getPort() {		
		return delegate.getPort();
	}
	
	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {		
		return delegate.getReceiveBufferSize();
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {		
		return delegate.getRemoteSocketAddress();
	}

	@Override
	public boolean getReuseAddress() throws SocketException {		
		return delegate.getReuseAddress();
	}
	
	@Override
	public synchronized int getSendBufferSize() throws SocketException {		
		return delegate.getSendBufferSize();
	}
	
	@Override
	public int getSoLinger() throws SocketException {		
		return delegate.getSoLinger();
	}
	
	@Override
	public synchronized int getSoTimeout() throws SocketException {		
		return delegate.getSoTimeout();
	}
	
	@Override
	public boolean getTcpNoDelay() throws SocketException {		
		return delegate.getTcpNoDelay();
	}
	
	@Override
	public int getTrafficClass() throws SocketException {		
		return delegate.getTrafficClass();
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
	public boolean isConnected() {		
		return delegate.isConnected();
	}
	
	@Override
	public boolean isInputShutdown() {		
		return delegate.isInputShutdown();
	}
	
	@Override
	public boolean isOutputShutdown() {		
		return delegate.isOutputShutdown();
	}
	
	@Override
	public void sendUrgentData(int data) throws IOException {		
		delegate.sendUrgentData(data);
	}
	
	@Override
	public void setKeepAlive(boolean on) throws SocketException {		
		delegate.setKeepAlive(on);
	}
	
	@Override
	public void setOOBInline(boolean on) throws SocketException {		
		delegate.setOOBInline(on);
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
	public synchronized void setSendBufferSize(int size) throws SocketException {
		
		delegate.setSendBufferSize(size);
	}
	
	@Override
	public void setSoLinger(boolean on, int linger) throws SocketException {		
		delegate.setSoLinger(on, linger);
	}
	
	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {		
		delegate.setSoTimeout(timeout);
	}
	
	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {		
		delegate.setTcpNoDelay(on);
	}
	
	@Override
	public void setTrafficClass(int tc) throws SocketException {		
		delegate.setTrafficClass(tc);
	}
	
	@Override
	public void shutdownInput() throws IOException {		
		delegate.shutdownInput();
	}
	
	@Override
	public void shutdownOutput() throws IOException {	
		delegate.shutdownOutput();
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}
	
	public Socket getDelegate() {
		return delegate;
	}
}

