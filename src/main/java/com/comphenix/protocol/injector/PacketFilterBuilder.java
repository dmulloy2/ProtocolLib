package com.comphenix.protocol.injector;

import javax.annotation.Nonnull;

import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.utility.MinecraftVersion;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class PacketFilterBuilder {
	private ClassLoader classLoader;
	private Server server;
	private Plugin library;
	private MinecraftVersion mcVersion;
	private DelayedSingleTask unhookTask;
	private ErrorReporter reporter;
	
	// Whether or not we need to enable Netty
	private AsyncFilterManager asyncManager;
	private boolean nettyEnabled;

	/**
	 * Update the current class loader.
	 * @param classLoader - current class loader.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder classLoader(@Nonnull ClassLoader classLoader) {
		if (classLoader == null)
			throw new IllegalArgumentException("classLoader cannot be NULL.");
		this.classLoader = classLoader;
		return this;
	}
	
	/**
	 * Set the current server.
	 * @param server - current server.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder server(@Nonnull Server server) {
		if (server == null)
			throw new IllegalArgumentException("server cannot be NULL.");
		this.server = server;
		return this;
	}
	
	/**
	 * Set a reference to the plugin instance of ProtocolLib.
	 * @param library - plugin instance.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder library(@Nonnull Plugin library) {
		if (library == null)
			throw new IllegalArgumentException("library cannot be NULL.");
		this.library = library;
		return this;
	}
	
	/**
	 * Set the current Minecraft version.
	 * @param mcVersion - Minecraft version.
	 * @return This builder, for chaining. 
	 */
	public PacketFilterBuilder minecraftVersion(@Nonnull MinecraftVersion mcVersion) {
		if (mcVersion == null)
			throw new IllegalArgumentException("minecraftVersion cannot be NULL.");
		this.mcVersion = mcVersion;
		return this;
	}
	
	/**
	 * Set the task used to delay unhooking when ProtocolLib is no in use.
	 * @param unhookTask - the unhook task.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder unhookTask(@Nonnull DelayedSingleTask unhookTask) {
		if (unhookTask == null)
			throw new IllegalArgumentException("unhookTask cannot be NULL.");
		this.unhookTask = unhookTask;
		return this;
	}
	
	/**
	 * Set the error reporter.
	 * @param reporter - new error reporter.
	 * @return This builder, for chaining.
	 */
	public PacketFilterBuilder reporter(@Nonnull ErrorReporter reporter) {
		if (reporter == null)
			throw new IllegalArgumentException("reporter cannot be NULL.");
		this.reporter = reporter;
		return this;
	}
	
	/**
	 * Determine if we should prepare to hook Netty in Spigot.
	 * <p>
	 * This is calculated in the {@link #build()} method.
	 * @return TRUE if we should, FALSE otherwise.
	 */
	public boolean isNettyEnabled() {
		return nettyEnabled;
	}
	
	/**
	 * Retrieve the class loader set in this builder.
	 * @return The class loader.
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * Retrieve the current CraftBukkit server.
	 * @return Current server.
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Retrieve a reference to the current ProtocolLib instance.
	 * @return ProtocolLib.
	 */
	public Plugin getLibrary() {
		return library;
	}

	/**
	 * Retrieve the current Minecraft version.
	 * @return Current version.
	 */
	public MinecraftVersion getMinecraftVersion() {
		return mcVersion;
	}

	/**
	 * Retrieve the task that is used to delay unhooking when ProtocolLib is no in use.
	 * @return The unhook task.
	 */
	public DelayedSingleTask getUnhookTask() {
		return unhookTask;
	}

	/**
	 * Retrieve the error reporter.
	 * @return Error reporter.
	 */
	public ErrorReporter getReporter() {
		return reporter;
	}

	/**
	 * Retrieve the asynchronous manager.
	 * <p>
	 * This is first constructed the {@link #build()} method.
	 * @return The asynchronous manager.
	 */
	public AsyncFilterManager getAsyncManager() {
		return asyncManager;
	}
	
	/**
	 * Create a new packet filter manager.
	 * @return A new packet filter manager.
	 */
	public InternalManager build() {
		if (reporter == null)
			throw new IllegalArgumentException("reporter cannot be NULL.");
		if (classLoader == null)
			throw new IllegalArgumentException("classLoader cannot be NULL.");
		
		asyncManager = new AsyncFilterManager(reporter, server.getScheduler());
		nettyEnabled = false;

		return buildInternal();
	}

	/**
	 * Construct a new packet filter manager without checking for Netty.
	 * @return A new packet filter manager.
	 */
	private PacketFilterManager buildInternal() {
		PacketFilterManager manager = new PacketFilterManager(this);
		
		// It's a cyclic reference, but it's too late to fix now
		asyncManager.setManager(manager);
		return manager;
	}
}
