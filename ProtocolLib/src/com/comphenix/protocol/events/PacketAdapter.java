package com.comphenix.protocol.events;

import java.util.Set;

import org.bukkit.plugin.Plugin;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * Represents a packet listener with useful constructors.
 * 
 * @author Kristian
 */
public abstract class PacketAdapter implements PacketListener {
	
	protected Plugin plugin;
	protected Set<Integer> packetsID;
	protected ConnectionSide connectionSide;
	
	/**
	 * Initialize a packet listener.
	 * @param plugin - the plugin that spawned this listener.
	 * @param connectionSide - the packet type the listener is looking for.
	 * @param packets - the packet IDs the listener is looking for.
	 */
	public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, Integer... packets) {
		this.plugin = plugin;
		this.connectionSide = connectionSide;
		this.packetsID = Sets.newHashSet(packets);
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		// Default is to do nothing
	}
	
	@Override
	public void onPacketSending(PacketEvent event) {
		// And here too
	}
	
	@Override
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
	
	@Override
	public Set<Integer> getPacketsID() {
		return packetsID;
	}
	
	@Override
	public Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Retrieves the name of the plugin that has been associated with the listener.
	 * @return Name of the associated plugin.
	 */
	public static String getPluginName(PacketListener listener) {
		
		Plugin plugin = listener.getPlugin();
		
		// Try to get the plugin name
		try {
			if (plugin == null)
				return "UNKNOWN";
			else
				return plugin.getName();
			
		} catch (NoSuchMethodError e) {
			return plugin.toString();
		}
	}
	
	@Override
	public String toString() {		
		// This is used by the error reporter 
		return String.format("PacketAdapter[plugin=%s, side=%s, packets=%s]", 
				getPluginName(this), getConnectionSide().name(), 
				Joiner.on(", ").join(packetsID));
	}
}
