package com.comphenix.protocol.injector.server;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketAddress;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;

class InputStreamReflectLookup extends AbstractInputStreamLookup {
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
	public SocketInjector getSocketInjector(InputStream input) {
		SocketInjector injector = ownerSocket.get(input);
		
		if (injector != null) {
			return injector;
		} else {
			try {
				Socket socket = getSocket(input);
				Player player = tempPlayerFactory.createTemporaryPlayer(server);
				SocketInjector created = new TemporarySocketInjector(player, socket);
				
				// Update injector
				TemporaryPlayerFactory.setInjectorInPlayer(player, created);
				
				// Save address too
				addressLookup.put(socket.getRemoteSocketAddress(), input);
				
				// Associate the socket with a given input stream
				setSocketInjector(input, created);
				return created;
			
			} catch (IllegalAccessException e) {
				throw new FieldAccessException("Cannot find or access socket field for " + input, e);
			}
		}
	}

	@Override
	public SocketInjector getSocketInjector(SocketAddress address) {
		InputStream input = addressLookup.get(address);
		
		if (input != null)
			return getSocketInjector(input);
		else
			return null;
	}

	@Override
	public void cleanupAll() {
		// Do nothing
	}
	
	private static Socket getSocket(InputStream stream) throws IllegalAccessException {
		if (stream instanceof FilterInputStream) {
			return getSocket(getInputStream((FilterInputStream) stream));
		} else {
			// Just do it
			Field socketField = FuzzyReflection.fromObject(stream, true).
								 getFieldByType("socket", Socket.class);

			return (Socket) FieldUtils.readField(socketField, stream, true);
		}
	}
}
