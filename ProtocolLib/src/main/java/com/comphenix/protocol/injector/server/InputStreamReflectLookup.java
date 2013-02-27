package com.comphenix.protocol.injector.server;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.collect.MapMaker;

class InputStreamReflectLookup extends AbstractInputStreamLookup {
	// Using weak keys and values ensures that we will not hold up garbage collection
	protected ConcurrentMap<Socket, SocketInjector> ownerSocket = new MapMaker().weakKeys().makeMap();
	protected ConcurrentMap<SocketAddress, Socket> addressLookup = new MapMaker().weakValues().makeMap();
	protected ConcurrentMap<InputStream, Socket> inputLookup = new MapMaker().weakValues().makeMap();
	
	// Used to create fake players
	private TemporaryPlayerFactory tempPlayerFactory = new TemporaryPlayerFactory();

	public InputStreamReflectLookup(ErrorReporter reporter, Server server) {
		super(reporter, server);
	}

	@Override
	public void inject(Object container) {
		// Do nothing
	}

	@Override
	public void postWorldLoaded() {
		// Nothing again
	}

	@Override
	public SocketInjector getSocketInjector(Socket socket) {
		SocketInjector result = ownerSocket.get(socket);
		
		if (result == null) {
			Player player = tempPlayerFactory.createTemporaryPlayer(server);
			SocketInjector created = new TemporarySocketInjector(player, socket);
							
			result = ownerSocket.putIfAbsent(socket, created);
			
			if (result == null) {
				// We won - use our created injector
				TemporaryPlayerFactory.setInjectorInPlayer(player, created);
				result = created;
			}
		}
		return result;		
	}
	
	@Override
	public SocketInjector getSocketInjector(InputStream input) {
		try {
			Socket socket = getSocket(input);
			
			// Guard against NPE
			if (socket != null)
				return getSocketInjector(socket);
			else
				return null;
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot find or access socket field for " + input, e);
		}
	}

	/**
	 * Use reflection to get the underlying socket from an input stream.
	 * @param stream - the socket stream to lookup.
	 * @return The underlying socket, or NULL if not found.
	 * @throws IllegalAccessException Unable to access socket field.
	 */
	private Socket getSocket(InputStream stream) throws IllegalAccessException {
		// Extra check, just in case
		if (stream instanceof FilterInputStream)
			return getSocket(getInputStream((FilterInputStream) stream));
		
		Socket result = inputLookup.get(stream);
		
		if (result == null) {
			result = lookupSocket(stream);
			
			// Save it
			inputLookup.put(stream, result);
		}
		return result;
	}
	
	@Override
	public void setSocketInjector(Socket socket, SocketInjector injector) {
		if (socket == null)
			throw new IllegalArgumentException("socket cannot be NULL");
		if (injector == null)
			throw new IllegalArgumentException("injector cannot be NULL.");
		
		SocketInjector previous = ownerSocket.put(socket, injector);
		
		// Save the address lookup too
		addressLookup.put(socket.getRemoteSocketAddress(), socket);
		
		// Any previous temporary players will also be associated
		if (previous != null) {
			// Update the reference to any previous injector
			onPreviousSocketOverwritten(previous, injector);
		}
	}
	
	@Override
	public SocketInjector getSocketInjector(SocketAddress address) {
		Socket socket = addressLookup.get(address);
		
		if (socket != null)
			return getSocketInjector(socket);
		else
			return null;
	}

	@Override
	public void cleanupAll() {
		// Do nothing
	}
	
	/**
	 * Lookup the underlying socket of a stream through reflection.
	 * @param stream - the socket stream.
	 * @return The underlying socket. 
	 * @throws IllegalAccessException If reflection failed.
	 */
	private static Socket lookupSocket(InputStream stream) throws IllegalAccessException {
		if (stream instanceof FilterInputStream) {
			return lookupSocket(getInputStream((FilterInputStream) stream));
		} else {
			// Just do it
			Field socketField = FuzzyReflection.fromObject(stream, true).
								 getFieldByType("socket", Socket.class);

			return (Socket) FieldUtils.readField(socketField, stream, true);
		}
	}
}
