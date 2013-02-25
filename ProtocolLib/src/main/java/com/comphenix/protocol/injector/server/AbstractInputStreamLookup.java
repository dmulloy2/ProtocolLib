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

public abstract class AbstractInputStreamLookup {
	// Used to access the inner input stream of a filtered input stream
	private static Field filteredInputField;
	
	// Using weak keys and values ensures that we will not hold up garbage collection
	protected ConcurrentMap<InputStream, SocketInjector> ownerSocket = new MapMaker().weakKeys().makeMap();
	protected ConcurrentMap<SocketAddress, InputStream> addressLookup = new MapMaker().weakValues().makeMap();

	// Error reporter
	protected final ErrorReporter reporter;
	
	// Reference to the server itself
	protected final Server server;

	protected AbstractInputStreamLookup(ErrorReporter reporter, Server server) {
		this.reporter = reporter;
		this.server = server;
	}

	/**
	 * Retrieve the underlying input stream that is associated with a given filter input stream.
	 * @param filtered - the filter input stream.
	 * @return The underlying input stream that is being filtered.
	 * @throws FieldAccessException Unable to access input stream.
	 */
	protected static InputStream getInputStream(FilterInputStream filtered) {
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
	 * Inject the given server thread or dedicated connection.
	 * @param container - class that contains a ServerSocket field.
	 */
	public abstract void inject(Object container);
	
	/**
	 * Invoked when the world has loaded.
	 */
	public abstract void postWorldLoaded();
	
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
	public abstract SocketInjector getSocketInjector(InputStream input);

	/**
	 * Retrieve a injector by its address.
	 * @param address - the address of the socket.
	 * @return The socket injector.
	 */
	public abstract SocketInjector getSocketInjector(SocketAddress address);
	
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
			onPreviousSocketOverwritten(previous, injector);
		}
	}
	
	protected void onPreviousSocketOverwritten(SocketInjector previous, SocketInjector current) {
		// Do nothing
	}
	
	/**
	 * Invoked when the injection should be undone.
	 */
	public abstract void cleanupAll();
}