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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
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

import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.PrettyPrinter;
import com.comphenix.protocol.reflect.PrettyPrinter.ObjectPrinter;
import com.comphenix.protocol.utility.ChatExtensions;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.google.common.collect.Sets;

/**
 * Handles the "packet" debug command.
 * 
 * @author Kristian
 */
class CommandPacket extends CommandBase {
	public static final ReportType REPORT_CANNOT_SEND_MESSAGE = new ReportType("Cannot send chat message.");

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
	
	// The main parser
	private PacketTypeParser typeParser = new PacketTypeParser();
	
	// Paged message
	private Map<CommandSender, List<String>> pagedMessage = new WeakHashMap<CommandSender, List<String>>();
	
	// Current registered packet types
	private PacketTypeSet packetTypes = new PacketTypeSet();
	private PacketTypeSet extendedTypes = new PacketTypeSet();
	
	// The packet listener
	private PacketListener listener;
	
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
			Deque<String> arguments = new ArrayDeque<String>(Arrays.asList(args));
			SubCommand subCommand = parseCommand(arguments);

			// Commands with different parameters
			if (subCommand == SubCommand.PAGE) {
				int page = Integer.parseInt(args[1]);
				
				if (page > 0)
					printPage(sender, page);
				else
					sendMessageSilently(sender, ChatColor.RED + "Page index must be greater than zero.");
				return true;
			}
			
			Set<PacketType> types = typeParser.parseTypes(arguments, PacketTypeParser.DEFAULT_MAX_RANGE);
			Boolean detailed = parseBoolean(arguments, "detailed");
			
			if (arguments.size() > 0) {
				throw new IllegalArgumentException("Insufficient arguments.");
			}
		
			// The last element is optional
			if (detailed == null) {
				detailed = false;
			}

			// Perform commands
			if (subCommand == SubCommand.ADD) {
				// The add command is dangerous - don't default on the connection side
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "Please specify a connection side.");
					return false;
				}
				
				executeAddCommand(sender, types, detailed);
			} else if (subCommand == SubCommand.REMOVE) {
				executeRemoveCommand(sender, types);
			} else if (subCommand == SubCommand.NAMES) {
				executeNamesCommand(sender, types);
			}
			
		} catch (NumberFormatException e) {
			sendMessageSilently(sender, ChatColor.RED + "Cannot parse number: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			sendMessageSilently(sender, ChatColor.RED + e.getMessage());
		}
		
		return true;
	}
	
	private void executeAddCommand(CommandSender sender, Set<PacketType> addition, boolean detailed) {
		packetTypes.addAll(addition);
		
		// Also mark these types as "detailed"
		if (detailed) {
			extendedTypes.addAll(addition);
		}
		updatePacketListener();
		sendMessageSilently(sender, ChatColor.BLUE + "Added listener " + getWhitelistInfo(listener));
	}
	
	private void executeRemoveCommand(CommandSender sender, Set<PacketType> removal) {
		packetTypes.removeAll(removal);
		extendedTypes.removeAll(removal);
		updatePacketListener();
		sendMessageSilently(sender, ChatColor.BLUE + "Removing packet types.");
	}
	
	private void executeNamesCommand(CommandSender sender, Set<PacketType> types) {
		List<String> messages = new ArrayList<String>();
		
		// Print the equivalent name of every given ID
		for (PacketType type : types) {
			messages.add(ChatColor.BLUE + type.toString());
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
		
	private Set<PacketType> filterTypes(Set<PacketType> types, Sender sender) {
		Set<PacketType> result = Sets.newHashSet();
		
		for (PacketType type : types) {
			if (type.getSender() == sender) {
				result.add(type);
			}
		}
		return result;
	}
	
	public PacketListener createPacketListener(Set<PacketType> type) {
		final ListeningWhitelist serverList = ListeningWhitelist.newBuilder().
				types(filterTypes(type, Sender.SERVER)).
				gamePhaseBoth().
				monitor().
				build();
		
		final ListeningWhitelist clientList = ListeningWhitelist.newBuilder(serverList).
				types(filterTypes(type, Sender.CLIENT)).
				build();
		
		return new PacketListener() {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (filter.filterEvent(event)) {
					printInformation(event);
				}
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (filter.filterEvent(event)) {
					printInformation(event);
				}
			}
			
			private void printInformation(PacketEvent event) {
				String verb = event.isServerPacket() ? "Sent" : "Received";
				String format = event.isServerPacket() ? 
						"%s %s to %s" : 
						"%s %s from %s";
				
				String shortDescription = String.format(format,
						event.isCancelled() ? "Cancelled" : verb,
						event.getPacketType(),
						event.getPlayer().getName()
				);
				
				// Detailed will print the packet's content too
				if (extendedTypes.contains(event.getPacketType())) {
					try {
						Object packet = event.getPacket().getHandle();
						Class<?> clazz = packet.getClass();
						
						// Get the first Minecraft super class
						while ((!MinecraftReflection.isMinecraftClass(clazz) || 
								 Factory.class.isAssignableFrom(clazz)) && clazz != Object.class) {
							clazz = clazz.getSuperclass();
						}
						
						logger.info(shortDescription + ":\n" +
							PrettyPrinter.printObject(packet, clazz, MinecraftReflection.getPacketClass(), PrettyPrinter.RECURSE_DEPTH, new ObjectPrinter() {
								@Override
								public boolean print(StringBuilder output, Object value) {
									if (value != null) {
										EquivalentConverter<Object> converter = BukkitConverters.getConvertersForGeneric().get(value.getClass());
										
										if (converter != null) {
											output.append(converter.getSpecific(value));
											return true;
										}
									}
									return false;
								}
							})
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
				return serverList;
			}
			
			@Override
			public ListeningWhitelist getReceivingWhitelist() {
				return clientList;
			}
			
			@Override
			public Plugin getPlugin() {
				return plugin;
			}
		};
	}
		
	public PacketListener updatePacketListener() {
		if (listener != null) {
			manager.removePacketListener(listener);
		}
		
		// Register a new listener instead
		listener = createPacketListener(packetTypes.values());
		manager.addPacketListener(listener);
		return listener;
	}
	
	private SubCommand parseCommand(Deque<String> arguments) {
		String text = arguments.poll().toLowerCase();
		
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
}
