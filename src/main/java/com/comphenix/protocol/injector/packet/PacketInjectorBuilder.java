package com.comphenix.protocol.injector.packet;

import javax.annotation.Nonnull;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.base.Preconditions;

/**
 * A builder responsible for creating incoming packet injectors.
 * 
 * @author Kristian
 */
public class PacketInjectorBuilder {
	protected PacketInjectorBuilder() {
		// No need to construct this
	}
	
	/**
	 * Retrieve a new packet injector builder.
	 * @return Injector builder.
	 */
	public static PacketInjectorBuilder newBuilder() {
		return new PacketInjectorBuilder();
	}

	protected ListenerInvoker invoker;
	protected ErrorReporter reporter;
	protected PlayerInjectionHandler playerInjection;
	
	/**
	 * The error reporter used by the created injector.
	 * @param reporter - new error reporter.
	 * @return This builder, for chaining.
	 */
	public PacketInjectorBuilder reporter(@Nonnull ErrorReporter reporter) {
		Preconditions.checkNotNull(reporter, "reporter cannot be NULL");
		this.reporter = reporter;
		return this;
	}
	
	/**
	 * The packet stream invoker.
	 * @param invoker - the invoker.
	 * @return This builder, for chaining.
	 */
	public PacketInjectorBuilder invoker(@Nonnull ListenerInvoker invoker) {
		Preconditions.checkNotNull(invoker, "invoker cannot be NULL");
		this.invoker = invoker;
		return this;
	}

	/**
	 * Set the player injection.
	 * @param playerInjection - the injection.
	 * @return This builder, for chaining.
	 */
	@Nonnull
	public PacketInjectorBuilder playerInjection(@Nonnull PlayerInjectionHandler playerInjection) {
		Preconditions.checkNotNull(playerInjection, "playerInjection cannot be NULL");
		this.playerInjection = playerInjection;
		return this;
	}
	
	/**
	 * Called before an object is created with this builder.
	 */
	private void initializeDefaults() {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager(); 
		
		// Initialize with default values if we can
		if (reporter == null)
			reporter = ProtocolLibrary.getErrorReporter();
		if (invoker == null)
			invoker = (PacketFilterManager) manager;
		if (playerInjection == null)
			throw new IllegalStateException("Player injection parameter must be initialized.");
	}

	/**
	 * Create a packet injector using the provided fields or the default values.
	 * <p>
	 * Note that any non-null builder parameters must be set.
	 * @return The created injector.
	 * @throws FieldAccessException If anything goes wrong in terms of reflection.
	 */
	public PacketInjector buildInjector() throws FieldAccessException {
		initializeDefaults();
		return new ProxyPacketInjector(invoker, playerInjection, reporter);
	}
}
