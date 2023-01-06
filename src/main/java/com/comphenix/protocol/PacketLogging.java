/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2017 Dan Mulloy
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

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logs packets to a given stream
 * @author dmulloy2
 */
public class PacketLogging implements CommandExecutor, PacketListener {
	public static final String NAME = "packetlog";

	private static MethodAccessor HEX_DUMP;

	private List<PacketType> sendingTypes = new ArrayList<>();
	private List<PacketType> receivingTypes = new ArrayList<>();

	private ListeningWhitelist sendingWhitelist;
	private ListeningWhitelist receivingWhitelist;

	private Logger fileLogger;
	private LogLocation location = LogLocation.FILE;

	private final ProtocolManager manager;
	private final Plugin plugin;

	PacketLogging(Plugin plugin, ProtocolManager manager) {
		this.plugin = plugin;
		this.manager = manager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		PacketType type = null;

		try {
			if (args.length > 2) {
				Protocol protocol;

				try {
					protocol = Protocol.valueOf(args[0].toUpperCase());
				} catch (IllegalArgumentException ex) {
					sender.sendMessage(ChatColor.RED + "Unknown protocol " + args[0]);
					return true;
				}

				Sender pSender;

				try {
					pSender = Sender.valueOf(args[1].toUpperCase());
				} catch (IllegalArgumentException ex) {
					sender.sendMessage(ChatColor.RED + "Unknown sender: " + args[1]);
					return true;
				}

				try {
					try { // Try IDs first
						int id = Integer.parseInt(args[2]);
						type = PacketType.findCurrent(protocol, pSender, id);
					} catch (NumberFormatException ex) { // Check packet names
						String name = args[2];
						for (PacketType packet : PacketType.values()) {
							if (packet.getProtocol() == protocol && packet.getSender() == pSender) {
								if (packet.name().equalsIgnoreCase(name)) {
									type = packet;
									break;
								}
								for (String className : packet.getClassNames()) {
									if (className.equalsIgnoreCase(name)) {
										type = packet;
										break;
									}
								}
							}
						}
					}
				} catch (IllegalArgumentException ex) { // RIP
					type = null;
				}

				if (type == null) {
					sender.sendMessage(ChatColor.RED + "Unknown packet: " + args[2]);
					return true;
				}

				if (args.length > 3) {
					if (args[3].equalsIgnoreCase("console")) {
						this.location = LogLocation.CONSOLE;
					} else {
						this.location = LogLocation.FILE;
					}
				}

				if (pSender == Sender.CLIENT) {
					if (receivingTypes.contains(type)) {
						receivingTypes.remove(type);
					} else {
						receivingTypes.add(type);
					}
				} else {
					if (sendingTypes.contains(type)) {
						sendingTypes.remove(type);
					} else {
						sendingTypes.add(type);
					}
				}

				startLogging();
				sender.sendMessage(ChatColor.GREEN + "Now logging " + type.getPacketClass().getSimpleName());
				return true;
			}

			sender.sendMessage(ChatColor.RED + "Invalid syntax: /packetlog <protocol> <sender> <packet> [location]");
			return true;
		} catch (Throwable ex) {
			sender.sendMessage(ChatColor.RED + "Failed to parse command: " + ex.toString());
			return true;
		}
	}

	private void startLogging() {
		manager.removePacketListener(this);

		if (sendingTypes.isEmpty() && receivingTypes.isEmpty()) {
			return;
		}

		this.sendingWhitelist = ListeningWhitelist.newBuilder().types(sendingTypes).build();
		this.receivingWhitelist = ListeningWhitelist.newBuilder().types(receivingTypes).build();

		// Setup the file logger if it hasn't been already
		if (location == LogLocation.FILE && fileLogger == null) {
			fileLogger = Logger.getLogger("ProtocolLib-FileLogging");

			for (Handler handler : fileLogger.getHandlers())
				fileLogger.removeHandler(handler);
			fileLogger.setUseParentHandlers(false);

			try {
				File logFile = new File(plugin.getDataFolder(), "log.log");
				FileHandler handler = new FileHandler(logFile.getAbsolutePath(), true);
				handler.setFormatter(new LogFormatter());
				fileLogger.addHandler(handler);
			} catch (IOException ex) {
				plugin.getLogger().log(Level.SEVERE, "Failed to obtain log file:", ex);
				return;
			}
		}

		manager.addPacketListener(this);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		log(event);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		log(event);
	}

	// Here's where the magic happens

	private static String hexDump(byte[] bytes) throws IOException {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			if (HEX_DUMP == null) {
				Class<?> hexDumpClass = MinecraftReflection.getLibraryClass("org.apache.commons.io.HexDump");
				HEX_DUMP = Accessors.getMethodAccessor(FuzzyReflection.fromClass(hexDumpClass)
						.getMethodByParameters("dump", byte[].class, long.class, OutputStream.class, int.class));
			}

			HEX_DUMP.invoke(null, bytes, 0, output, 0);
			return new String(output.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	private void log(PacketEvent event) {
		try {
			byte[] bytes = WirePacket.bytesFromPacket(event.getPacket());
			String hexDump = hexDump(bytes);

			if (location == LogLocation.FILE) {
				fileLogger.log(Level.INFO, event.getPacketType() + ":");
				fileLogger.log(Level.INFO, hexDump);
				fileLogger.log(Level.INFO, "");
			} else {
				System.out.println(event.getPacketType() + ":");
				System.out.println(hexDump);
				System.out.println();
			}
		} catch (Throwable ex) {
			plugin.getLogger().log(Level.WARNING, "Failed to log packet " + event.getPacketType() + ":", ex);
			plugin.getLogger().log(Level.WARNING, "Clearing packet logger...");

			sendingTypes.clear();
			receivingTypes.clear();
			startLogging();
		}
	}

	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return sendingWhitelist;
	}

	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return receivingWhitelist;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}

	private enum LogLocation {
		CONSOLE, FILE
	}

	private static class LogFormatter extends Formatter {
		private static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		private static final String LINE_SEPARATOR = System.getProperty("line.separator");
		private static final String FORMAT = "[{0}] {1}";

		@Override
		public String format(LogRecord record) {
			String string = formatMessage(record);
			if (string.isEmpty()) {
				return LINE_SEPARATOR;
			}

			StringBuilder message = new StringBuilder();
			message.append(MessageFormat.format(FORMAT, DATE.format(record.getMillis()), string));
			message.append(LINE_SEPARATOR);
			return message.toString();
		}
	}
}