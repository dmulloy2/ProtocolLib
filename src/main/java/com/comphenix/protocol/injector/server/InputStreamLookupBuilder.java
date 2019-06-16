package com.comphenix.protocol.injector.server;

import org.bukkit.Server;

import com.comphenix.protocol.error.ErrorReporter;

/**
 * Constructs the appropriate input stream lookup for the current JVM and architecture.
 * 
 * @author Kristian
 */
public class InputStreamLookupBuilder {
	public static InputStreamLookupBuilder newBuilder() {
		return new InputStreamLookupBuilder();
	}

	protected InputStreamLookupBuilder() {
		// Use the static method.
	}
	
	private Server server;
	private ErrorReporter reporter;

	/**
	 * Set the server instance to use.
	 * @param server - server instance.
	 * @return The current builder, for chaining.
	 */
	public InputStreamLookupBuilder server(Server server) {
		this.server = server;
		return this;
	}
	
	/**
	 * Set the error reporter to pass on to the lookup.
	 * @param reporter - the error reporter.
	 * @return The current builder, for chaining.
	 */
	public InputStreamLookupBuilder reporter(ErrorReporter reporter) {
		this.reporter = reporter;
		return this;
	}
	
	public AbstractInputStreamLookup build() {
		return new InputStreamReflectLookup(reporter, server);
	}
}
