package com.comphenix.protocol.injector;

import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public class PacketFilterBuilder {

	private Server server;
	private Plugin library;
	private MinecraftVersion mcVersion;
	private ErrorReporter reporter;
	private AsyncFilterManager asyncManager;

	/**
	 * Set the current server.
	 *
	 * @param server - current server.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder server(@Nonnull Server server) {
		this.server = server;
		return this;
	}

	/**
	 * Set a reference to the plugin instance of ProtocolLib.
	 *
	 * @param library - plugin instance.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder library(@Nonnull Plugin library) {
		this.library = library;
		return this;
	}

	/**
	 * Set the current Minecraft version.
	 *
	 * @param mcVersion - Minecraft version.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder minecraftVersion(@Nonnull MinecraftVersion mcVersion) {
		this.mcVersion = mcVersion;
		return this;
	}

	/**
	 * Set the error reporter.
	 *
	 * @param reporter - new error reporter.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder reporter(@Nonnull ErrorReporter reporter) {
		this.reporter = reporter;
		return this;
	}

	/**
	 * Retrieve the current CraftBukkit server.
	 *
	 * @return Current server.
	 */
	public Server getServer() {
		return this.server;
	}

	/**
	 * Retrieve a reference to the current ProtocolLib instance.
	 *
	 * @return ProtocolLib.
	 */
	public Plugin getLibrary() {
		return this.library;
	}

	/**
	 * Retrieve the current Minecraft version.
	 *
	 * @return Current version.
	 */
	public MinecraftVersion getMinecraftVersion() {
		return this.mcVersion;
	}

	/**
	 * Retrieve the error reporter.
	 *
	 * @return Error reporter.
	 */
	public ErrorReporter getReporter() {
		return this.reporter;
	}

	/**
	 * Retrieve the asynchronous manager.
	 * <p>
	 * This is first constructed the {@link #build()} method.
	 *
	 * @return The asynchronous manager.
	 */
	public AsyncFilterManager getAsyncManager() {
		return this.asyncManager;
	}

	/**
	 * Create a new packet filter manager.
	 *
	 * @return A new packet filter manager.
	 */
	public InternalManager build() {
		if (this.reporter == null) {
			throw new IllegalArgumentException("reporter cannot be NULL.");
		}

		this.asyncManager = new AsyncFilterManager(this.reporter, this.server.getScheduler());
		return new PacketFilterManager(this);
	}
}
