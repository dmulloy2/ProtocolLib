package com.comphenix.protocol.injector.player;

import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * Constructor for different player injectors.
 * 
 * @author Kristian
 */
public class PlayerInjectorBuilder {
	public static PlayerInjectorBuilder newBuilder() {
		return new PlayerInjectorBuilder();
	}

	protected PlayerInjectorBuilder() {
		// Use the static method.
	}

	protected ErrorReporter reporter;
	protected Predicate<GamePhase> injectionFilter;
	protected ListenerInvoker invoker;
	protected Set<PacketListener> packetListeners;
	protected Server server;
	protected MinecraftVersion version;

	/**
	 * The error reporter used by the created injector.
	 * @param reporter - new error reporter.
	 * @return This builder, for chaining.
	 */
	public PlayerInjectorBuilder reporter(@Nonnull ErrorReporter reporter) {
		Preconditions.checkNotNull(reporter, "reporter cannot be NULL");
		this.reporter = reporter;
		return this;
	}
	
	/**
	 * The injection filter that is used to determine if it is necessary to perform
	 * injection during a certain phase.
	 * @param injectionFilter - filter predicate.
	 * @return This builder, for chaining.
	 */
	@Nonnull
	public PlayerInjectorBuilder injectionFilter(@Nonnull Predicate<GamePhase> injectionFilter) {
		Preconditions.checkNotNull(injectionFilter, "injectionFilter cannot be NULL");
		this.injectionFilter = injectionFilter;
		return this;
	}
	
	/**
	 * The packet stream invoker.
	 * @param invoker - the invoker.
	 * @return This builder, for chaining.
	 */
	public PlayerInjectorBuilder invoker(@Nonnull ListenerInvoker invoker) {
		Preconditions.checkNotNull(invoker, "invoker cannot be NULL");
		this.invoker = invoker;
		return this;
	}
	
	/**
	 * Set the set of packet listeners.
	 * @param packetListeners - packet listeners.
	 * @return This builder, for chaining.
	 */
	@Nonnull
	public PlayerInjectorBuilder packetListeners(@Nonnull Set<PacketListener> packetListeners) {
		Preconditions.checkNotNull(packetListeners, "packetListeners cannot be NULL");
		this.packetListeners = packetListeners;
		return this;
	}
	
	/**
	 * Set the Bukkit server used for scheduling.
	 * @param server - the Bukkit server.
	 * @return This builder, for chaining.
	 */
	public PlayerInjectorBuilder server(@Nonnull Server server) {
		Preconditions.checkNotNull(server, "server cannot be NULL");
		this.server = server;
		return this;
	}
	
	/**
	 * Set the current Minecraft version.
	 * @param version - the current Minecraft version, or NULL if unknown.
	 * @return This builder, for chaining.
	 */
	public PlayerInjectorBuilder version(MinecraftVersion version) {
		this.version = version;
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
		if (server == null)
			server = Bukkit.getServer();
		if (injectionFilter == null)
			throw new IllegalStateException("injectionFilter must be initialized.");
		if (packetListeners == null)
			throw new IllegalStateException("packetListeners must be initialized.");
	}
	
	/**
	 * Construct the injection handler.
	 * <p>
	 * Any builder parameter marked as NON-NULL is essential and must be initialized.
	 * @return The constructed injection handler using the current parameters.
	 */
	public PlayerInjectionHandler buildHandler() {
		// Fill any default fields
		initializeDefaults();
		
		return new ProxyPlayerInjectionHandler(
				reporter, injectionFilter, 
				invoker, packetListeners, server, version);
	}
}
