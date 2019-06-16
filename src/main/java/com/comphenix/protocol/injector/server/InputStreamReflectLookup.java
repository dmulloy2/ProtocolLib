package com.comphenix.protocol.injector.server;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Server;

import com.comphenix.protocol.concurrency.BlockingHashMap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.collect.MapMaker;

class InputStreamReflectLookup extends AbstractInputStreamLookup {
	// Used to access the inner input stream of a filtered input stream
	private static Field filteredInputField;
	
	// The default lookup timeout
	private static final long DEFAULT_TIMEOUT = 2000; // ms
	
	// Using weak keys and values ensures that we will not hold up garbage collection
	protected BlockingHashMap<SocketAddress, SocketInjector> addressLookup = new BlockingHashMap<SocketAddress, SocketInjector>();
	protected ConcurrentMap<InputStream, SocketAddress> inputLookup = new MapMaker().weakValues().makeMap();
	
	// The timeout
	private final long injectorTimeout;
	
	public InputStreamReflectLookup(ErrorReporter reporter, Server server) {
		this(reporter, server, DEFAULT_TIMEOUT);
	}

	/**
	 * Initialize a reflect lookup with a given default injector timeout.
	 * <p>
	 * This timeout defines the maximum amount of time to wait until an injector has been discovered.
	 * @param reporter - the error reporter.
	 * @param server - the current Bukkit server.
	 * @param injectorTimeout - the injector timeout.
	 */
	public InputStreamReflectLookup(ErrorReporter reporter, Server server, long injectorTimeout) {
		super(reporter, server);
		this.injectorTimeout = injectorTimeout;
	}
	
	@Override
	public void inject(Object container) {
		// Do nothing
	}

	@Override
	public SocketInjector peekSocketInjector(SocketAddress address) {
		try {
			return addressLookup.get(address, 0, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// Whatever
			return null;
		}
	}

	@Override
	public SocketInjector waitSocketInjector(SocketAddress address) {
		try {
			// Note that we actually SWALLOW interrupts here - this is because Minecraft uses interrupts to 
			// periodically wake up waiting readers and writers. We have to wait for the dedicated server thread
			// to catch up, so we'll swallow these interrupts.
			//
			// TODO: Consider if we should raise the thread priority of the dedicated server listener thread. 
			return addressLookup.get(address, injectorTimeout, TimeUnit.MILLISECONDS, true);
		} catch (InterruptedException e) { 
			// This cannot be!
			throw new IllegalStateException("Impossible exception occured!", e);
		}
	}
	
	@Override
	public SocketInjector waitSocketInjector(Socket socket) {
		return waitSocketInjector(socket.getRemoteSocketAddress());
	}
	
	@Override
	public SocketInjector waitSocketInjector(InputStream input) {
		try {
			SocketAddress address = waitSocketAddress(input);
			
			// Guard against NPE
			if (address != null)
				return waitSocketInjector(address);
			else
				return null;
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot find or access socket field for " + input, e);
		}
	}

	/**
	 * Use reflection to get the underlying socket address from an input stream.
	 * @param stream - the socket stream to lookup.
	 * @return The underlying socket address, or NULL if not found.
	 * @throws IllegalAccessException Unable to access socket field.
	 */
	private SocketAddress waitSocketAddress(InputStream stream) throws IllegalAccessException {
		// Extra check, just in case
		if (stream instanceof FilterInputStream)
			return waitSocketAddress(getInputStream((FilterInputStream) stream));
		
		SocketAddress result = inputLookup.get(stream);
		
		if (result == null) {
			Socket socket = lookupSocket(stream);
			
			// Save it
			result = socket.getRemoteSocketAddress();
			inputLookup.put(stream, result);
		}
		return result;
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
	
	@Override
	public void setSocketInjector(SocketAddress address, SocketInjector injector) {
		if (address == null)
			throw new IllegalArgumentException("address cannot be NULL");
		if (injector == null)
			throw new IllegalArgumentException("injector cannot be NULL.");
		
		SocketInjector previous = addressLookup.put(address, injector);
		
		// Any previous temporary players will also be associated
		if (previous != null) {
			// Update the reference to any previous injector
			onPreviousSocketOverwritten(previous, injector);
		}
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
