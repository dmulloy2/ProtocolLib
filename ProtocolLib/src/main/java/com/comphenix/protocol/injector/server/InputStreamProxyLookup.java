package com.comphenix.protocol.injector.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.google.common.collect.MapMaker;

/**
 * Injection hook used to determine which Socket, and thus address, created any given DataInputStream.
 *  
 * @author Kristian
 */
class InputStreamProxyLookup extends AbstractInputStreamLookup {
	/**
	 * The read and connect timeout for our built-in MOTD reader.
	 */
	private static final int READ_TIMEOUT = 5000;
	private static final int CONNECT_TIMEOUT = 1000;
		
	// Using weak keys and values ensures that we will not hold up garbage collection
	protected ConcurrentMap<InputStream, SocketInjector> ownerSocket = new MapMaker().weakKeys().makeMap();
	protected ConcurrentMap<SocketAddress, InputStream> addressLookup = new MapMaker().weakValues().makeMap();
	protected ConcurrentMap<Socket, InputStream> socketLookup = new MapMaker().weakKeys().makeMap();
	
	// Fake connections
	private Set<SocketAddress> fakeConnections = Collections.newSetFromMap(
			new MapMaker().weakKeys().<SocketAddress, Boolean>makeMap()
	);
	
	// The server socket that has been injected
	private VolatileField injectedServerSocket;
	
	// Used to create fake players
	private TemporaryPlayerFactory tempPlayerFactory = new TemporaryPlayerFactory();

	public InputStreamProxyLookup(ErrorReporter reporter, Server server) {
		super(reporter, server);
	}

	@Override
	public void inject(Object container) {
		if (injectedServerSocket != null)
			throw new IllegalStateException("Can only inject once. Create a new object instead.");
		
		Field selected = FuzzyReflection.fromObject(container, true).
							getFieldByType("serverSocket", ServerSocket.class);
		injectedServerSocket = new VolatileField(selected, container, true);

		// Load socket
		ServerSocket socket = (ServerSocket) injectedServerSocket.getValue();
		
		// Make sure it exists
		if (socket == null) {
			throw new IllegalStateException("Cannot find socket to inject. Reference " + selected + " contains NULL.");
		}
		
		// Next, let us create the injected server socket
		try {
			injectedServerSocket.setValue(new DelegatedServerSocket(socket) {
				@Override
				public Socket accept() throws IOException {
					Socket accepted = super.accept();

					if (fakeConnections.contains(accepted.getRemoteSocketAddress())) {
						// Don't intercept this connection
						return accepted;
					}
					
					// Wrap the socket we return
					return new DelegatedSocket(accepted) {
						@Override
						public InputStream getInputStream() throws IOException {
							InputStream input = super.getInputStream();
							SocketAddress address = delegate.getRemoteSocketAddress();
							
							// Make sure that the address is actually valid
							if (address != null) {
								InputStream previousStream = addressLookup.
									putIfAbsent(delegate.getRemoteSocketAddress(), input);
																
								// Ensure that this is our first time
								if (previousStream == null) {
									// Create a new temporary player
									Player temporaryPlayer = tempPlayerFactory.createTemporaryPlayer(server);
									TemporarySocketInjector temporaryInjector = new TemporarySocketInjector(temporaryPlayer, delegate);
									DelegatedSocketInjector socketInjector = new DelegatedSocketInjector(temporaryInjector);
									
									// Update it
									TemporaryPlayerFactory.setInjectorInPlayer(temporaryPlayer, socketInjector);
								
									// Socket lookup
									socketLookup.put(this, input);
									
									// Associate the socket with a given input stream
									setSocketInjector(input, socketInjector);
								}
							}
							return input;
						}
					};
				}
			});
			
		} catch (IOException e) {
			throw new IllegalStateException("Unbound socket threw an exception. Should never occur.", e);
		}
	}
	
	@Override
	public SocketInjector getSocketInjector(Socket socket) {
		InputStream stream = getStream(socket);
		
		if (stream != null)
			return getSocketInjector(stream);
		else
			return null;
	}

	@Override
	public void setSocketInjector(Socket socket, SocketInjector injector) {
		InputStream stream = getStream(socket);
		
		if (stream != null) {
			socketLookup.put(socket, stream);
			setSocketInjector(stream, injector);
		}
	}
	
	/**
	 * Set the referenced socket injector by input stream.
	 * @param stream - the input stream.
	 * @param injector - the injector to reference.
	 */
	public void setSocketInjector(InputStream stream, SocketInjector injector) {
		SocketInjector previous = ownerSocket.put(stream, injector);
		
		// Handle overwrite
		if (previous != null) {
			onPreviousSocketOverwritten(previous, injector);
		}
	}

	private InputStream getStream(Socket socket) {
		InputStream result = socketLookup.get(socket);
		
		// Use the socket as well
		if (result == null) {
			try {
				result = socket.getInputStream();
			} catch (IOException e) {
				throw new RuntimeException("Unable to retrieve input stream from socket " + socket, e);
			}
		}
		return result;
	}
	
	@Override
	public SocketInjector getSocketInjector(InputStream input) {
		return ownerSocket.get(input);
	}

	@Override
	public SocketInjector getSocketInjector(SocketAddress address) {
		InputStream input = addressLookup.get(address);
		
		if (input != null) {
			return ownerSocket.get(input);
		} else {
			return null;
		}
	}
	
	@Override
	public void postWorldLoaded() {
		cycleServerPorts();
	}
	
	/**
	 * Invoked when we need to cycle the injected server port. 
	 * <p>
	 * This uses a fairly significant hack - we connect to our own server.
	 */
	void cycleServerPorts() {
		final ServerSocket serverSocket = (ServerSocket) injectedServerSocket.getValue();
		final SocketAddress address = new InetSocketAddress("127.0.0.1", serverSocket.getLocalPort());

		// Sorry
		Thread consumeThread = new Thread("ProtocolLib - Hack Thread") {
			@Override
			public void run() {
				Socket socket = null;
				OutputStream output = null;
				InputStream input = null;
				InputStreamReader reader = null;
				
				try {
					socket = new Socket();
					socket.connect(address, CONNECT_TIMEOUT);

					// Ignore packets from this connection
					fakeConnections.add(socket.getLocalSocketAddress());

					// Shouldn't take that long
					socket.setSoTimeout(READ_TIMEOUT);
					
					// Retrieve sockets
					output = socket.getOutputStream();
					input = socket.getInputStream();
					reader = new InputStreamReader(input, Charset.forName("UTF-16BE"));
					
					// Get the server to send a MOTD
					output.write(new byte[] { (byte) 0xFE, (byte) 0x01 });

					int packetId = input.read();
					int length = reader.read();
					
					if (packetId != Packets.Server.KICK_DISCONNECT) {
						throw new IOException("Invalid packet ID: " + packetId);
					}
					if (length <= 0) {
						throw new IOException("Invalid string length.");
					}
					char[] chars = new char[length];
					 
					// Read all the characters
					if (reader.read(chars, 0, length) != length) {
						throw new IOException("Premature end of stream.");
					}

					System.out.println("Read: " + new String(chars));
				
				} catch (Exception e) {
					reporter.reportWarning(this, "Cannot simulate MOTD.", e);
				} finally {
					try {
						if (reader != null)
							reader.close();
						if (input != null)
							input.close();
						if (output != null)
							output.close();
						if (socket != null)
							socket.close();
					} catch (IOException e) {
						reporter.reportWarning(this, "Cannot clean up socket.", e);
					}
				}
			}
		};
		consumeThread.start();
	}
	
	@Override
	protected void onPreviousSocketOverwritten(SocketInjector previous, SocketInjector current) {
		// Don't forget this
		super.onPreviousSocketOverwritten(previous, current);
		
		if (previous instanceof DelegatedSocketInjector) {
			DelegatedSocketInjector delegated = (DelegatedSocketInjector) previous;
			
			// Update the delegate
			delegated.setDelegate(current);
		}
	}
	
	@Override
	public void cleanupAll() {
		if (injectedServerSocket != null && injectedServerSocket.isCurrentSet()) {
			injectedServerSocket.revertValue();
			
			// This is going to suck
			cycleServerPorts();
		}
	}
}
