package com.comphenix.protocol.injector;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;

/**
 * Yields access to the internal hook configuration.
 * 
 * @author Kristian
 */
public interface InternalManager extends ProtocolManager {
	/**
	 * Retrieves how the server packets are read.
	 * @return Injection method for reading server packets.
	 */
	public PlayerInjectHooks getPlayerHook();

	/**
	 * Sets how the server packets are read.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	public void setPlayerHook(PlayerInjectHooks playerHook);

	/**
	 * Register this protocol manager on Bukkit.
	 * @param manager - Bukkit plugin manager that provides player join/leave events.
	 * @param plugin - the parent plugin.
	 */
	public void registerEvents(PluginManager manager, final Plugin plugin);
	
	/**
	 * Called when ProtocolLib is closing.
	 */
	public void close();

	/**
	 * Determine if debug mode is enabled.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isDebug();
	
	/**
	 * Set whether or not debug mode is enabled.
	 * @param debug - TRUE if it is, FALSE otherwise.
	 */
	public void setDebug(boolean debug);
}
