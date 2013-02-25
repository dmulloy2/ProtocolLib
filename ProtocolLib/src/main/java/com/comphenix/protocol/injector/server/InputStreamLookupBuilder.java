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
	private boolean alternativeJVM;
	
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
	
	/**
	 * Set whether or not the current JVM implementation is alternative.
	 * @param value - TRUE if it is, FALSE otherwise.
	 * @return The current builder, for chaining.
	 */
	public InputStreamLookupBuilder alternativeJVM(boolean value) {
		alternativeJVM = value;
		return this;
	}
	
	public AbstractInputStreamLookup build() {
		if (alternativeJVM)
			return new InputStreamProxyLookup(reporter, server);
		else
			return new InputStreamReflectLookup(reporter, server);
	}
}
