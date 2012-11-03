package com.comphenix.protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Factory;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.concurrency.AbstractIntervalTree;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.PrettyPrinter;
import com.comphenix.protocol.utility.ChatExtensions;
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
		ADD, REMOVE, NAMES;
	}
	
	/**
	 * Name of this command.
	 */
	public static final String NAME = "packet";
	
	private Plugin plugin;
	private Logger logger;
	private ErrorReporter reporter;
	private ProtocolManager manager;
		
	private ChatExtensions chatter;
	
	// Registered packet listeners
	private AbstractIntervalTree<Integer, DetailedPacketListener> clientListeners = createTree(ConnectionSide.CLIENT_SIDE);
	private AbstractIntervalTree<Integer, DetailedPacketListener> serverListeners = createTree(ConnectionSide.SERVER_SIDE);
	
	public CommandPacket(Plugin plugin, Logger logger, ErrorReporter reporter, ProtocolManager manager) {
		this.plugin = plugin;
		this.logger = logger;
		this.reporter = reporter;
		this.manager = manager;
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
	 * @param player - the player to send it to.
	 * @param message - the message to send.
	 * @return TRUE if the message was sent successfully, FALSE otherwise.
	 */
	public void sendMessageSilently(CommandSender receiver, String message) {
		try {
			chatter.sendMessageSilently(receiver, message);
		} catch (InvocationTargetException e) {
			reporter.reportDetailed(this, "Cannot send chat message.", e, receiver, message);
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
			reporter.reportDetailed(this, "Cannot send chat message.", e, message, message);
		}
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
				
				Integer lastIndex = args.length - 1;
				Boolean detailed = parseBoolean(args, lastIndex);

				// See if the last element is a boolean
				if (detailed == null) {
					detailed = false;
				} else {
					lastIndex--;
				}
				
				// Make sure the packet IDs are valid
				List<Range<Integer>> ranges = getRanges(args, 2, lastIndex, Ranges.closed(0, 255));

				if (ranges.isEmpty()) {
					// Use every packet ID
					ranges.add(Ranges.closed(0, 255));
				}
				
				// Perform command
				if (subCommand == SubCommand.ADD) {
					for (Range<Integer> range : ranges) {
						DetailedPacketListener listener = addPacketListeners(side, range.lowerEndpoint(), range.upperEndpoint(), detailed);
						sendMessageSilently(sender, ChatColor.BLUE + "Added listener " + getWhitelistInfo(listener));
					}
					
				} else if (subCommand == SubCommand.REMOVE) {
					int count = 0; 
					
					// Remove each packet listener
					for (Range<Integer> range : ranges) {
						count += removePacketListeners(side, range.lowerEndpoint(), range.upperEndpoint(), detailed).size();
					}
					
					sendMessageSilently(sender, ChatColor.BLUE + "Fully removed " + count + " listeners.");
				} else if (subCommand == SubCommand.NAMES) {
					
					// Print the equivalent name of every given ID
					for (Range<Integer> range : ranges) {
						for (int id : range.asSet(DiscreteDomains.integers())) {
							sendMessageSilently(sender, ChatColor.BLUE + "" + id + ": " + Packets.getDeclaredName(id));
						}
					}
				}
				
			} catch (NumberFormatException e) {
				sendMessageSilently(sender, ChatColor.RED + "Cannot parse number: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				sendMessageSilently(sender, ChatColor.RED + e.getMessage());
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Parse ranges from an array of tokens.
	 * @param args - array of tokens.
	 * @param offset - beginning offset.
	 * @param legalRange - range of legal values.
	 * @return The parsed ranges.
	 */
	public static List<Range<Integer>> getRanges(String[] args, int offset, int lastIndex, Range<Integer> legalRange) {
		List<String> tokens = tokenizeInput(args, offset, lastIndex);
		List<Range<Integer>> ranges = new ArrayList<Range<Integer>>();
		
		for (int i = 0; i < tokens.size(); i++) {
			Range<Integer> range;
			String current = tokens.get(i);
			String next = i + 1 < tokens.size() ? tokens.get(i + 1) : null;
			
			// Yoda equality is done for null-safety
			if ("-".equals(current)) {
				throw new IllegalArgumentException("A hyphen must appear between two numbers.");
			} else if ("-".equals(next)) {
				if (i + 2 >= tokens.size())
					throw new IllegalArgumentException("Cannot form a range without a upper limit.");

				// This is a proper range
				range = Ranges.closed(Integer.parseInt(current), Integer.parseInt(tokens.get(i + 2)));
				ranges.add(range);
				
				// Skip the two next tokens
				i += 2;
				
			} else {
				// Just a single number
				range = Ranges.singleton(Integer.parseInt(current));
				ranges.add(range);
			}
			
			// Validate ranges
			if (!legalRange.encloses(range)) {
				throw new IllegalArgumentException(range + " is not in the range " + range.toString());
			}
		}
		
		return simplify(ranges, legalRange.upperEndpoint());
	}
	
	/**
	 * Simplify a list of ranges by assuming a maximum value.
	 * @param ranges - the list of ranges to simplify.
	 * @param maximum - the maximum value (minimum value is always 0).
	 * @return A simplified list of ranges.
	 */
	private static List<Range<Integer>> simplify(List<Range<Integer>> ranges, int maximum) {
		List<Range<Integer>> result = new ArrayList<Range<Integer>>();
		boolean[] set = new boolean[maximum + 1];
		int start = -1;
		
		// Set every ID
		for (Range<Integer> range : ranges) {
			for (int id : range.asSet(DiscreteDomains.integers())) {
				set[id] = true;
			}
		}
		
		// Generate ranges from this set
		for (int i = 0; i <= set.length; i++) {
			if (i < set.length && set[i]) {
				if (start < 0) {
					start = i;
				}
			} else {
				if (start >= 0) {
					result.add(Ranges.closed(start, i - 1));
					start = -1;
				}
			}
		}
		
		return result;
	}
	
	private static List<String> tokenizeInput(String[] args, int offset, int lastIndex) {
		List<String> tokens = new ArrayList<String>();
		
		// Tokenize the input
		for (int i = offset; i <= lastIndex; i++) {
			String text = args[i];
			StringBuilder number = new StringBuilder();
			
			for (int j = 0; j < text.length(); j++) {
				char current = text.charAt(j);
				
				if (Character.isDigit(current)) {
					number.append(current);
				} else if (Character.isWhitespace(current)) {
					// That's ok
				} else if (current == '-') {
					// Add the number token first
					if (number.length() > 0) {
						tokens.add(number.toString());
						number.setLength(0);
					}
					
					tokens.add(Character.toString(current));
				} else {
					throw new IllegalArgumentException("Illegal character '" + current + "' found.");
				}
			}
			
			// Add the number token, if it hasn't already
			if (number.length() > 0)
				tokens.add(number.toString());
		}
		
		return tokens;
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
						"%s %s (%s) from %s",
						verb, 
						Packets.getDeclaredName(event.getPacketID()),
						event.getPacketID(),
						event.getPlayer().getName()
				);
				
				// Detailed will print the packet's content too
				if (detailed) {
					try {
						Packet packet = event.getPacket().getHandle();
						Class<?> clazz = packet.getClass();
						
						// Get the first Minecraft super class
						while ((!clazz.getName().startsWith("net.minecraft.server") || 
								 Factory.class.isAssignableFrom(clazz)) && clazz != Object.class) {
							clazz = clazz.getSuperclass();
						}
						
						logger.info(shortDescription + ":\n" +
							PrettyPrinter.printObject(packet, clazz, Packet.class)
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
			getListenerTree(side).put(idStart, idStop, listener);
			return listener;
		} else {
			throw new IllegalArgumentException("No packets found in the range " + idStart + " - " + idStop + ".");
		}
	}
	
	public Set<AbstractIntervalTree<Integer, DetailedPacketListener>.Entry> removePacketListeners(
			ConnectionSide side, int idStart, int idStop, boolean detailed) {
		
		// The interval tree will automatically remove the listeners for us
		return getListenerTree(side).remove(idStart, idStop);
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
		else if ("names".startsWith(text)) 
			return SubCommand.NAMES;
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
	private Boolean parseBoolean(String[] args, int index) {
		if (index < args.length) {
			if (args[index].equalsIgnoreCase("true"))
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
