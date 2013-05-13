/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.cglib.proxy.Factory;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.concurrency.AbstractIntervalTree;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.PrettyPrinter;
import com.comphenix.protocol.utility.ChatExtensions;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

/**
 * Handles the "packet" debug command.
 * 
 * @author Kristian
 */
class CommandPacket extends CommandBase {
	public static final ReportType REPORT_CANNOT_SEND_MESSAGE = new ReportType("Cannot send chat message.");
	
	private interface DetailedPacketListener extends PacketListener {
		/**
		 * Determine whether or not the given packet listener is detailed or not.
		 * @return TRUE if it is detailed, FALSE otherwise.
		 */
		public boolean isDetailed();
	}
	
	private enum SubCommand {
		ADD, REMOVE, NAMES, PAGE;
	}
	
	/**
	 * Name of this command.
	 */
	public static final String NAME = "packet";

	/**
	 * Number of lines per page.
	 */
	public static final int PAGE_LINE_COUNT = 9;
	
	private Plugin plugin;
	private Logger logger;
	private ProtocolManager manager;
		
	private ChatExtensions chatter;
	
	// Paged message
	private Map<CommandSender, List<String>> pagedMessage = new WeakHashMap<CommandSender, List<String>>();
	
	// Registered packet listeners
	private AbstractIntervalTree<Integer, DetailedPacketListener> clientListeners = createTree(ConnectionSide.CLIENT_SIDE);
	private AbstractIntervalTree<Integer, DetailedPacketListener> serverListeners = createTree(ConnectionSide.SERVER_SIDE);
	
	// Filter packet events
	private CommandFilter filter;
	
	public CommandPacket(ErrorReporter reporter, Plugin plugin, Logger logger, CommandFilter filter, ProtocolManager manager) {
		super(reporter, CommandBase.PERMISSION_ADMIN, NAME, 1);
		this.plugin = plugin;
		this.logger = logger;
		this.manager = manager;
		this.filter = filter;
		this.chatter = new ChatExtensions(manager);
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
	
	/**
	 * Send a message without invoking the packet listeners.
	 * @param receiver - the player to send it to.
	 * @param message - the message to send.
	 * @return TRUE if the message was sent successfully, FALSE otherwise.
	 */
	public void sendMessageSilently(CommandSender receiver, String message) {
		try {
			chatter.sendMessageSilently(receiver, message);
		} catch (InvocationTargetException e) {
			reporter.reportDetailed(this, 
					Report.newBuilder(REPORT_CANNOT_SEND_MESSAGE).error(e).callerParam(receiver, message)
			);
		}
	}
	
	/**
	 * Broadcast a message without invoking any packet listeners.
	 * @param message - message to send.
	 * @param permission - permission required to receieve the message. NULL to target everyone.
	 */
	public void broadcastMessageSilently(String message, String permission) {
		try {
			chatter.broadcastMessageSilently(message, permission);
		} catch (InvocationTargetException e) {
			reporter.reportDetailed(this, 
					Report.newBuilder(REPORT_CANNOT_SEND_MESSAGE).error(e).callerParam(message, permission)
			);
		}
	}
	
	private void printPage(CommandSender sender, int pageIndex) {
		List<String> paged = pagedMessage.get(sender);
		
		// Make sure the player has any pages
		if (paged != null) {
			int lastPage = ((paged.size() - 1) / PAGE_LINE_COUNT) + 1;
			
			for (int i = PAGE_LINE_COUNT * (pageIndex - 1); i < PAGE_LINE_COUNT * pageIndex; i++) {
				if (i < paged.size()) {
					sendMessageSilently(sender, " " + paged.get(i));
				}
			}
			
			// More data?
			if (pageIndex < lastPage) {
				sendMessageSilently(sender, "Send /packet page " + (pageIndex + 1) + " for the next page.");
			}
			
		} else {
			sendMessageSilently(sender, ChatColor.RED + "No pages found.");
		}
	}
	
	/*
	 * Description: Adds or removes a simple packet listener.
       Usage:       /<command> add|remove client|server|both [ID start] [ID stop] [detailed] 
	 */
	@Override
	protected boolean handleCommand(CommandSender sender, String[] args) {
		try {
			SubCommand subCommand = parseCommand(args, 0);
			
			// Commands with different parameters
			if (subCommand == SubCommand.PAGE) {
				int page = Integer.parseInt(args[1]);
				
				if (page > 0)
					printPage(sender, page);
				else
					sendMessageSilently(sender, ChatColor.RED + "Page index must be greater than zero.");
				return true;
			}
			
			ConnectionSide side = parseSide(args, 1, ConnectionSide.BOTH);
			
			Integer lastIndex = args.length - 1;
			Boolean detailed = parseBoolean(args, "detailed", lastIndex);

			// See if the last element is a boolean
			if (detailed == null) {
				detailed = false;
			} else {
				lastIndex--;
			}
			
			// Make sure the packet IDs are valid
			List<Range<Integer>> ranges = RangeParser.getRanges(args, 2, lastIndex, Ranges.closed(0, 255));

			if (ranges.isEmpty()) {
				// Use every packet ID
				ranges.add(Ranges.closed(0, 255));
			}
			
			// Perform commands
			if (subCommand == SubCommand.ADD) {
				// The add command is dangerous - don't default on the connection side
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "Please specify a connectionn side.");
					return false;
				}
				
				executeAddCommand(sender, side, detailed, ranges);
			} else if (subCommand == SubCommand.REMOVE) {
				executeRemoveCommand(sender, side, detailed, ranges);
			} else if (subCommand == SubCommand.NAMES) {
				executeNamesCommand(sender, side, ranges);
			}
			
		} catch (NumberFormatException e) {
			sendMessageSilently(sender, ChatColor.RED + "Cannot parse number: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			sendMessageSilently(sender, ChatColor.RED + e.getMessage());
		}
		
		return true;
	}

	private void executeAddCommand(CommandSender sender, ConnectionSide side, Boolean detailed, List<Range<Integer>> ranges) {
		for (Range<Integer> range : ranges) {
			DetailedPacketListener listener = addPacketListeners(side, range.lowerEndpoint(), range.upperEndpoint(), detailed);
			sendMessageSilently(sender, ChatColor.BLUE + "Added listener " + getWhitelistInfo(listener));
		}
	}
	
	private void executeRemoveCommand(CommandSender sender, ConnectionSide side, Boolean detailed, List<Range<Integer>> ranges) {
		int count = 0; 
		
		// Remove each packet listener
		for (Range<Integer> range : ranges) {
			count += removePacketListeners(side, range.lowerEndpoint(), range.upperEndpoint(), detailed).size();
		}
		
		sendMessageSilently(sender, ChatColor.BLUE + "Fully removed " + count + " listeners.");
	}
	
	private void executeNamesCommand(CommandSender sender, ConnectionSide side, List<Range<Integer>> ranges) {
		Set<Integer> named = getNamedPackets(side);
		List<String> messages = new ArrayList<String>();
		
		// Print the equivalent name of every given ID
		for (Range<Integer> range : ranges) {
			for (int id : range.asSet(DiscreteDomains.integers())) {
				if (named.contains(id)) {
					messages.add(ChatColor.WHITE + "" + id + ": " + ChatColor.BLUE + Packets.getDeclaredName(id));
				}
			}
		}
		
		if (sender instanceof Player && messages.size() > 0 && messages.size() > PAGE_LINE_COUNT) {
			// Divide the messages into chuncks
			pagedMessage.put(sender, messages);
			printPage(sender, 1);
			
		} else {
			// Just print the damn thing
			for (String message : messages) {
				sendMessageSilently(sender, message);
			}
		}
	}

	/**
	 * Retrieve whitelist information about a given listener.
	 * @param listener - the given listener.
	 * @return Whitelist information.
	 */
	private String getWhitelistInfo(PacketListener listener) {
		boolean sendingEmpty = ListeningWhitelist.isEmpty(listener.getSendingWhitelist());
		boolean receivingEmpty = ListeningWhitelist.isEmpty(listener.getReceivingWhitelist());
		
		if (!sendingEmpty && !receivingEmpty)
			return String.format("Sending: %s, Receiving: %s", listener.getSendingWhitelist(), listener.getReceivingWhitelist());
		else if (!sendingEmpty)
			return listener.getSendingWhitelist().toString();
		else if (!receivingEmpty)
			return listener.getReceivingWhitelist().toString();
		else
			return "[None]";
	}
	
	private Set<Integer> getValidPackets(ConnectionSide side) throws FieldAccessException {
		HashSet<Integer> supported = Sets.newHashSet();
		
		if (side.isForClient())
			supported.addAll(Packets.Client.getSupported());
		else if (side.isForServer())
			supported.addAll(Packets.Server.getSupported());
		
		return supported;
	}
	
	private Set<Integer> getNamedPackets(ConnectionSide side) {
		
		Set<Integer> valids = null;
		Set<Integer> result = Sets.newHashSet();
		
		try {
			valids = getValidPackets(side);
		} catch (FieldAccessException e) {
			valids = Ranges.closed(0, 255).asSet(DiscreteDomains.integers());
		}
		
		// Check connection side
		if (side.isForClient())
			result.addAll(Packets.Client.getRegistry().values());
		if (side.isForServer())
			result.addAll(Packets.Server.getRegistry().values());
		
		// Remove invalid packets
		result.retainAll(valids);
		return result;
	}
		
	public DetailedPacketListener createPacketListener(final ConnectionSide side, int idStart, int idStop, final boolean detailed) {
		Set<Integer> range = Ranges.closed(idStart, idStop).asSet(DiscreteDomains.integers());
		Set<Integer> packets;
		
		try {
			// Only use supported packet IDs
			packets = new HashSet<Integer>(getValidPackets(side));
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
				if (side.isForServer() && filter.filterEvent(event)) {
					printInformation(event);
				}
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (side.isForClient() && filter.filterEvent(event)) {
					printInformation(event);
				}
			}
			
			private void printInformation(PacketEvent event) {
				String format = side.isForClient() ? 
						"Received %s (%s) from %s" : 
						"Sent %s (%s) to %s";
				String shortDescription = String.format(format,
						Packets.getDeclaredName(event.getPacketID()),
						event.getPacketID(),
						event.getPlayer().getName()
				);
				
				// Detailed will print the packet's content too
				if (detailed) {
					try {
						Object packet = event.getPacket().getHandle();
						Class<?> clazz = packet.getClass();
						
						// Get the first Minecraft super class
						while ((!MinecraftReflection.isMinecraftClass(clazz) || 
								 Factory.class.isAssignableFrom(clazz)) && clazz != Object.class) {
							clazz = clazz.getSuperclass();
						}
						
						logger.info(shortDescription + ":\n" +
							PrettyPrinter.printObject(packet, clazz, MinecraftReflection.getPacketClass())
						);
						
					} catch (IllegalAccessException e) {
						logger.log(Level.WARNING, "Unable to use reflection.", e);
					}
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
		
	public DetailedPacketListener addPacketListeners(ConnectionSide side, int idStart, int idStop, boolean detailed) {
		DetailedPacketListener listener = createPacketListener(side, idStart, idStop, detailed);
		
		// The trees will manage the listeners for us
		if (listener != null) {
			if (side.isForClient())
				clientListeners.put(idStart, idStop, listener);
			if (side.isForServer())
				serverListeners.put(idStart, idStop, listener);
			return listener;
		} else {
			throw new IllegalArgumentException("No packets found in the range " + idStart + " - " + idStop + ".");
		}
	}
	
	public Set<AbstractIntervalTree<Integer, DetailedPacketListener>.Entry> removePacketListeners(
			ConnectionSide side, int idStart, int idStop, boolean detailed) {
		
		HashSet<AbstractIntervalTree<Integer, DetailedPacketListener>.Entry> result = Sets.newHashSet();
		
		// The interval tree will automatically remove the listeners for us
		if (side.isForClient())
			result.addAll(clientListeners.remove(idStart, idStop, true));
		if (side.isForServer())
			result.addAll(serverListeners.remove(idStart, idStop, true));
		return result;
	}

	private SubCommand parseCommand(String[] args, int index) {
		String text = args[index].toLowerCase();
		
		// Parse this too
		if ("add".startsWith(text))
			return SubCommand.ADD;
		else if ("remove".startsWith(text))
			return SubCommand.REMOVE;
		else if ("names".startsWith(text)) 
			return SubCommand.NAMES;
		else if ("page".startsWith(text)) 
			return SubCommand.PAGE;
		else
			throw new IllegalArgumentException(text + " is not a valid sub command. Must be add or remove.");
	}
	
	private ConnectionSide parseSide(String[] args, int index, ConnectionSide defaultValue) {
		if (index < args.length) {
			String text = args[index].toLowerCase();
			
			// Parse the side gracefully
			if ("client".startsWith(text))
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
	private Boolean parseBoolean(String[] args, String parameterName, int index) {
		if (index < args.length) {
			if (args[index].equalsIgnoreCase("true"))
				return true;
			else if (args[index].equalsIgnoreCase(parameterName))
				return true;
			else if (args[index].equalsIgnoreCase("false"))
				return false;
			else
				return null;
		} else {
			return null;
		}
	}
}
