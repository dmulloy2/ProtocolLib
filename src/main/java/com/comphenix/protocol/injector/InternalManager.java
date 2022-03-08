package com.comphenix.protocol.injector;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Yields access to the internal hook configuration.
 *
 * @author Kristian
 */
public interface InternalManager extends ProtocolManager {

	/**
	 * Register this protocol manager on Bukkit.
	 *
	 * @param manager - Bukkit plugin manager that provides player join/leave events.
	 * @param plugin  - the parent plugin.
	 */
	void registerEvents(PluginManager manager, final Plugin plugin);

	/**
	 * Called when ProtocolLib is closing.
	 */
	void close();

	/**
	 * Determine if debug mode is enabled.
	 *
	 * @return TRUE if it is, FALSE otherwise.
	 */
	boolean isDebug();

	/**
	 * Set whether or not debug mode is enabled.
	 *
	 * @param debug - TRUE if it is, FALSE otherwise.
	 */
	void setDebug(boolean debug);
}
