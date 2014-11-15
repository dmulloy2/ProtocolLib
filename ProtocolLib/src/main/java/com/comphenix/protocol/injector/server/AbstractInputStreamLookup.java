package com.comphenix.protocol.injector.server;

import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.error.ErrorReporter;

public abstract class AbstractInputStreamLookup {
	// Error reporter
	protected final ErrorReporter reporter;
	
	// Reference to the server itself
	protected final Server server;

	protected AbstractInputStreamLookup(ErrorReporter reporter, Server server) {
		this.reporter = reporter;
		this.server = server;
	}

	/**
	 * Inject the given server thread or dedicated connection.
	 * @param container - class that contains a ServerSocket field.
	 */
	public abstract void inject(Object container);
	
	/**
	 * Retrieve the associated socket injector for a player.
	 * @param input - the indentifying filtered input stream.
	 * @return The socket injector we have associated with this player.
	 */
	public abstract SocketInjector waitSocketInjector(InputStream input);
	
	/**
	 * Retrieve an injector by its socket.
	 * @param socket - the socket.
	 * @return The socket injector.
	 */
	public abstract SocketInjector waitSocketInjector(Socket socket);
	
	/**
	 * Retrieve a injector by its address.
	 * @param address - the address of the socket.
	 * @return The socket injector, or NULL if not found.
	 */
	public abstract SocketInjector waitSocketInjector(SocketAddress address);

	/**
	 * Attempt to get a socket injector without blocking the thread.
	 * @param address - the address to lookup.
	 * @return The socket injector, or NULL if not found.
	 */
	public abstract SocketInjector peekSocketInjector(SocketAddress address);
	
	/**
	 * Associate a given socket address to the provided socket injector.
	 * @param address - the socket address to associate.
	 * @param injector - the injector.
	 */
	public abstract void setSocketInjector(SocketAddress address, SocketInjector injector);

	/**
	 * If a player can hold a reference to its parent injector, this method will update that reference.
	 * @param previous - the previous injector.
	 * @param current - the new injector.
	 */
	protected void onPreviousSocketOverwritten(SocketInjector previous, SocketInjector current) {
		Player player = previous.getPlayer();
		
		// Default implementation
		if (player instanceof InjectorContainer) {
			TemporaryPlayerFactory.setInjectorInPlayer(player, current);
		}
	}
	
	/**
	 * Invoked when the injection should be undone.
	 */
	public abstract void cleanupAll();
}