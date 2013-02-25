package com.comphenix.protocol.injector.server;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.google.common.collect.MapMaker;

/**
 * Injection hook used to determine which Socket, and thus address, created any given DataInputStream.
 *  
 * @author Kristian
 */
public class InjectedServerSocket {
	/**
	 * The read and connect timeout for our built-in MOTD reader.
	 */
	private static final int READ_TIMEOUT = 5000;
	private static final int CONNECT_TIMEOUT = 1000;

	/**
	 * Represents a single send packet command.
	 * @author Kristian
	 */
	private static class SendPacketCommand {
		private final Object packet;
		private final boolean filtered;
		
		public SendPacketCommand(Object packet, boolean filtered) {
			this.packet = packet;
			this.filtered = filtered;
		}

		public Object getPacket() {
			return packet;
		}

		public boolean isFiltered() {
			return filtered;
		}
	}
	
	private static class TemporarySocketInjector implements SocketInjector {
		private Player temporaryPlayer;
		private Socket socket;

		// Queue of server packets
		private List<SendPacketCommand> syncronizedQueue = Collections.synchronizedList(new ArrayList<SendPacketCommand>());
		
		/**
		 * Represents a temporary socket injector.
		 * @param temporaryPlayer - temporary player instance.
		 * @param socket - the socket we are representing.
		 * @param fake - whether or not this connection should be ignored.
		 */
		public TemporarySocketInjector(Player temporaryPlayer, Socket socket) {
			this.temporaryPlayer = temporaryPlayer;
			this.socket = socket;
		}

		@Override
		public Socket getSocket() throws IllegalAccessException {
			return socket;
		}

		@Override
		public SocketAddress getAddress() throws IllegalAccessException {
			if (socket != null)
				return socket.getRemoteSocketAddress();
			return null;
		}

		@Override
		public void disconnect(String message) throws InvocationTargetException {
			// We have no choice - disregard message too
			try {
				socket.close();
			} catch (IOException e) {
				throw new InvocationTargetException(e);
			}
		}

		@Override
		public void sendServerPacket(Object packet, boolean filtered)
				throws InvocationTargetException {
			SendPacketCommand command = new SendPacketCommand(packet, filtered);
			
			// Queue until we can find something better
			syncronizedQueue.add(command);
		}
		
		@Override
		public Player getPlayer() {
			return temporaryPlayer;
		}

		@Override
		public Player getUpdatedPlayer() {
			return temporaryPlayer;
		}

		@Override
		public void transferState(SocketInjector delegate) {
			// Transmit all queued packets to a different injector.
			try {
				synchronized(syncronizedQueue) {
				    for (SendPacketCommand command : syncronizedQueue) {
						delegate.sendServerPacket(command.getPacket(), command.isFiltered());
				    }
				    syncronizedQueue.clear();
				}
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Unable to transmit packets to " + delegate + " from old injector.", e);
			}
		}
	}
	
	// Used to access the inner input stream of a filtered input stream
	private static Field filteredInputField;

	// Using weak keys and values ensures that we will not hold up garbage collection
	private ConcurrentMap<InputStream, SocketInjector> ownerSocket = new MapMaker().weakKeys().makeMap();
	private ConcurrentMap<SocketAddress, InputStream> addressLookup = new MapMaker().weakValues().makeMap();

	// Fake connections
	private Set<SocketAddress> fakeConnections = Collections.newSetFromMap(
			new MapMaker().weakKeys().<SocketAddress, Boolean>makeMap()
	);
	
	// The server socket that has been injected
	private VolatileField injectedServerSocket;
	
	// Reference to the server itself
	private final Server server;
	
	// Error reporter
	private final ErrorReporter reporter;
	
	// Used to create fake players
	private TemporaryPlayerFactory tempPlayerFactory = new TemporaryPlayerFactory();

	public InjectedServerSocket(ErrorReporter reporter, Server server) {
		this.reporter = reporter;
		this.server = server;
	}

	/**
	 * Inject the given server thread or dedicated connection.
	 * @param container - class that contains a ServerSocket field.
	 */
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
	
	/**
	 * Invoked when the world has loaded.
	 */
	public void postWorldLoaded() {
		cycleServerPorts();
	}
	
	/**
	 * Invoked when we need to cycle the injected server port. 
	 * <p>
	 * This uses a fairly significant hack - we connect to our own server.
	 */
	private void cycleServerPorts() {
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
	
	/**
	 * Retrieve the underlying input stream that is associated with a given filter input stream.
	 * @param filtered - the filter input stream.
	 * @return The underlying input stream that is being filtered.
	 * @throws FieldAccessException Unable to access input stream.
	 */
	private static InputStream getInputStream(FilterInputStream filtered) {
		if (filteredInputField == null)
			filteredInputField = FuzzyReflection.fromClass(FilterInputStream.class, true).
								  getFieldByType("in", InputStream.class);
		
		InputStream current = filtered;
		
		try {
			// Iterate until we find the real input stream
			while (current instanceof FilterInputStream) {
				current = (InputStream) FieldUtils.readField(filteredInputField, current, true);
			}
			return current;
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot access filtered input field.", e);
		}
	}
	
	/**
	 * Retrieve the associated socket injector for a player.
	 * @param filtered - the indentifying filtered input stream.
	 * @return The socket injector we have associated with this player.
	 * @throws FieldAccessException Unable to access input stream.
	 */
	public SocketInjector getSocketInjector(FilterInputStream filtered) {
		return getSocketInjector(getInputStream(filtered));
	}
	
	/**
	 * Retrieve the associated socket injector for a player.
	 * @param filtered - the indentifying filtered input stream.
	 * @return The socket injector we have associated with this player.
	 */
	public SocketInjector getSocketInjector(InputStream input) {
		return ownerSocket.get(input);
	}
	
	/**
	 * Retrieve a injector by its address.
	 * @param address - the address of the socket.
	 * @return The socket injector.
	 */
	public SocketInjector getSocketInjector(SocketAddress address) {
		InputStream input = addressLookup.get(address);
		
		if (input != null) {
			return ownerSocket.get(input);
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieve an injector by its socket.
	 * @param socket - the socket.
	 * @return The socket injector.
	 */
	public SocketInjector getSocketInjector(Socket socket) {
		if (socket == null)
			throw new IllegalArgumentException("The socket cannot be NULL.");
		return getSocketInjector(socket.getRemoteSocketAddress());
	}
	
	/**
	 * Associate a given input stream with the provided socket injector.
	 * @param input - the filtered input stream to associate.
	 * @param injector - the injector.
	 * @throws FieldAccessException Unable to access input stream.
	 */
	public void setSocketInjector(FilterInputStream input, SocketInjector injector) {
		setSocketInjector(getInputStream(input), injector);
	}
	
	/**
	 * Associate a given input stream with the provided socket injector.
	 * @param input - the input stream to associate.
	 * @param injector - the injector.
	 */
	public void setSocketInjector(InputStream input, SocketInjector injector) {
		SocketInjector previous = ownerSocket.put(input, injector);
		
		// Any previous temporary players will also be associated
		if (previous != null) {
			Player player = previous.getPlayer();
			
			if (player instanceof InjectContainer) {
				InjectContainer container = (InjectContainer) player;
				container.setInjector(injector);
			}
			
			// Update the reference to any previous injector
			if (previous instanceof DelegatedSocketInjector) {
				DelegatedSocketInjector delegated = (DelegatedSocketInjector) previous;
				
				// Update the delegate
				delegated.setDelegate(injector);
			}
		}
	}
	
	/**
	 * Invoked when the injection should be undone.
	 */
	public void cleanupAll() {
		if (injectedServerSocket != null && injectedServerSocket.isCurrentSet()) {
			injectedServerSocket.revertValue();
			
			// This is going to suck
			cycleServerPorts();
		}
	}
}
