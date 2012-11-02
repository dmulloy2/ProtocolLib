package com.comphenix.protocol;

import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.concurrency.AbstractIntervalTree;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * Handles the "packet" debug command.
 * 
 * @author Kristian
 */
class CommandPacket implements CommandExecutor {
	private interface DetailedPacketListener extends PacketListener {
		/**
		 * Determine whether or not the given packet listener is detailed or not.
		 * @return TRUE if it is detailed, FALSE otherwise.
		 */
		public boolean isDetailed();
	}
	
	private enum SubCommand {
		ADD, REMOVE;
	}
	
	/**
	 * Name of this command.
	 */
	public static final String NAME = "packet";
	
	private Plugin plugin;
	private Logger logger;
	private ProtocolManager manager;
	
	// Registered packet listeners
	private AbstractIntervalTree<Integer, DetailedPacketListener> clientListeners = createTree(ConnectionSide.CLIENT_SIDE);
	private AbstractIntervalTree<Integer, DetailedPacketListener> serverListeners = createTree(ConnectionSide.SERVER_SIDE);
	
	public CommandPacket(Plugin plugin, Logger logger, ProtocolManager manager) {
		this.plugin = plugin;
		this.logger = logger;
		this.manager = manager;
	}
	
	/**
	 * Construct a packet listener interval tree.
	 * @return Construct the tree.
	 */
	private AbstractIntervalTree<Integer, DetailedPacketListener> createTree(final ConnectionSide side) {
		return new AbstractIntervalTree<Integer, DetailedPacketListener>() {
			@Override
			protected Integer decrementKey(Integer key) {
				return key != null ? key - 1 : null;
			}
			
			@Override
			protected Integer incrementKey(Integer key) {
				return key != null ? key + 1 : null;
			}
			
			@Override
			protected void onEntryAdded(Entry added) {
				// Ensure that the starting ID and the ending ID is correct
				// This is necessary because the interval tree may change the range.
				if (added != null) {
					Range<Integer> key = added.getKey();
					DetailedPacketListener listener = added.getValue();
					DetailedPacketListener corrected = createPacketListener(
							side, key.lowerEndpoint(), key.upperEndpoint(), listener.isDetailed());
					
					added.setValue(corrected);
					
					if (corrected != null) {
						manager.addPacketListener(corrected);
					} else {
						// Never mind
						remove(key.lowerEndpoint(), key.upperEndpoint());
					}
				}
			}
			
			@Override
			protected void onEntryRemoved(Entry removed) {
				// Remove the listener
				if (removed != null) {
					DetailedPacketListener listener = removed.getValue();
					
					if (listener != null) {
						manager.removePacketListener(listener);
					}
				}
			}
		};
	}
	
	/*
	 * Description: Adds or removes a simple packet listener.
       Usage:       /<command> add|remove client|server|both [ID start] [ID stop] [detailed] 
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Make sure we're dealing with the correct command
		if (!command.getName().equalsIgnoreCase(NAME))
			return false;
		
		// We need at least one argument
		if (args != null && args.length > 0) {
			try {
				SubCommand subCommand = parseCommand(args, 0);
				ConnectionSide side = parseSide(args, 1, ConnectionSide.BOTH);
				
				int idStart = parseInteger(args, 2, 0);
				int idStop = parseInteger(args, 3, 255);
				
				// Make sure the packet IDs are valid
				if (idStart < 0 || idStart > 255)
					throw new IllegalAccessError("The starting packet ID must be within 0 - 255.");
				if (idStop < 0 || idStop > 255)
					throw new IllegalAccessError("The stop packet ID must be within 0 - 255.");
				
				// Special case. If stop is not set, but start is set, use a interval size of 1.
				if (args.length == 3)
					idStop = idStart + 1;
				
				boolean detailed = parseBoolean(args, 4, false);	
			
				// Perform command
				if (subCommand == SubCommand.ADD)
					addPacketListeners(side, idStart, idStop, detailed);
				else
					removePacketListeners(side, idStart, idStop, detailed);
				
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.DARK_RED + "Cannot parse number: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				sender.sendMessage(ChatColor.DARK_RED + e.getMessage());
			}
		}
		
		return false;
	}
	
	private Set<Integer> getValidPackets(ConnectionSide side) throws FieldAccessException {
		if (side.isForClient())
			return Packets.Client.getSupported();
		else if (side.isForServer())
			return Packets.Server.getSupported();
		else
			throw new IllegalArgumentException("Illegal side: " + side);
	}
	
	public DetailedPacketListener createPacketListener(final ConnectionSide side, int idStart, int idStop, final boolean detailed) {
		
		Set<Integer> range = Ranges.closed(idStart, idStop).asSet(DiscreteDomains.integers());
		Set<Integer> packets;
		
		try {
			// Only use supported packet IDs
			packets = getValidPackets(side);
			packets.retainAll(range);
			
		} catch (FieldAccessException e) {
			// Don't filter anything then
			packets = range;
		}

		// Ignore empty sets
		if (packets.isEmpty())
			return null;
		
		// Create the listener we will be using
		final ListeningWhitelist whitelist = new ListeningWhitelist(ListenerPriority.MONITOR, packets, GamePhase.BOTH);
		
		return new DetailedPacketListener() {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (side.isForServer()) {
					printInformation(event);
				}
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (side.isForClient()) {
					printInformation(event);
				}
			}
			
			private void printInformation(PacketEvent event) {
				String verb = side.isForClient() ? "Received" : "Sent";
				String shortDescription = String.format(
						"%s packet %s (%s)",
						verb, 
						event.getPacketID(),
						Packets.getDeclaredName(event.getPacketID())
				);
				
				// Detailed will print the packet's content too
				if (detailed) {
					logger.info(shortDescription + ":\n" +
						ToStringBuilder.reflectionToString(event.getPacket().getHandle(), ToStringStyle.MULTI_LINE_STYLE)
					);
				} else {
					logger.info(shortDescription + ".");
				}
			}
			
			@Override
			public ListeningWhitelist getSendingWhitelist() {
				return side.isForServer() ? whitelist : ListeningWhitelist.EMPTY_WHITELIST;
			}
			
			@Override
			public ListeningWhitelist getReceivingWhitelist() {
				return side.isForClient() ? whitelist : ListeningWhitelist.EMPTY_WHITELIST;
			}
			
			@Override
			public Plugin getPlugin() {
				return plugin;
			}

			@Override
			public boolean isDetailed() {
				return detailed;
			}
		};
	}
	
	public void addPacketListeners(ConnectionSide side, int idStart, int idStop, boolean detailed) {
		DetailedPacketListener listener = createPacketListener(side, idStart, idStop, detailed);
		
		// The trees will manage the listeners for us
		if (listener != null)
			getListenerTree(side).put(idStart, idStop, listener);
		else
			throw new IllegalArgumentException("No packets found in the range " + idStart + " - " + idStop + ".");
	}
	
	public void removePacketListeners(ConnectionSide side, int idStart, int idStop, boolean detailed) {
		// The interval tree will automatically remove the listeners for us
		getListenerTree(side).remove(idStart, idStop);
	}
	
	private AbstractIntervalTree<Integer, DetailedPacketListener> getListenerTree(ConnectionSide side) {
		if (side.isForClient())
			return clientListeners;
		else if (side.isForServer())
			return serverListeners;
		else
			throw new IllegalArgumentException("Not a legal connection side.");
	}

	private SubCommand parseCommand(String[] args, int index) {
		String text = args[index].toLowerCase();
		
		// Parse this too
		if ("add".startsWith(text))
			return SubCommand.ADD;
		else if ("remove".startsWith(text))
			return SubCommand.REMOVE;
		else
			throw new IllegalArgumentException(text + " is not a valid sub command. Must be add or remove.");
	}
	
	private ConnectionSide parseSide(String[] args, int index, ConnectionSide defaultValue) {
		if (index < args.length) {
			String text = args[index].toLowerCase();
			
			// Parse the side gracefully
			if ("both".startsWith(text))
				return ConnectionSide.BOTH;
			else if ("client".startsWith(text))
				return ConnectionSide.CLIENT_SIDE;
			else if ("server".startsWith(text))
				return ConnectionSide.SERVER_SIDE;
			else
				throw new IllegalArgumentException(text + " is not a connection side.");
			
		} else {
			return defaultValue;
		}
	}
	
	// Parse a boolean
	private boolean parseBoolean(String[] args, int index, boolean defaultValue) {
		if (index < args.length) {
			return Boolean.parseBoolean(args[index]);
		} else {
			return defaultValue;
		}
	}
	
	// And an integer
	private int parseInteger(String[] args, int index, int defaultValue) {
		if (index < args.length) {
			return Integer.parseInt(args[index]);
		} else {
			return defaultValue;
		}
	}
	
	public void cleanupAll() {
		clientListeners.clear();
		serverListeners.clear();
	}
}
