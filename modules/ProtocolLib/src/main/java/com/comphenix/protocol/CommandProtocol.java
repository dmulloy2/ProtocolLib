/**
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.comphenix.protocol.error.DetailedErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimingReportGenerator;
import com.comphenix.protocol.updater.Updater;
import com.comphenix.protocol.updater.Updater.UpdateType;
import com.comphenix.protocol.utility.Closer;

/**
 * Handles the "protocol" administration command.
 *
 * @author Kristian
 */
class CommandProtocol extends CommandBase {
	/**
	 * Name of this command.
	 */
	public static final String NAME = "protocol";
	
	private Plugin plugin;
	private Updater updater;
	private ProtocolConfig config;

	public CommandProtocol(ErrorReporter reporter, Plugin plugin, Updater updater, ProtocolConfig config) {
		super(reporter, CommandBase.PERMISSION_ADMIN, NAME, 1);
		this.plugin = plugin;
		this.updater = updater;
		this.config = config;
	}
	
	@Override
	protected boolean handleCommand(CommandSender sender, String[] args) {
		String subCommand = args[0];

		// Only return TRUE if we executed the correct command
		if (subCommand.equalsIgnoreCase("config") || subCommand.equalsIgnoreCase("reload")) {
			reloadConfiguration(sender);
		} else if (subCommand.equalsIgnoreCase("check")) {
			checkVersion(sender, true);
		} else if (subCommand.equalsIgnoreCase("update")) {
			updateVersion(sender, true);
		} else if (subCommand.equalsIgnoreCase("timings")) {
			toggleTimings(sender, args);
		} else if (subCommand.equalsIgnoreCase("listeners")) {
			printListeners(sender);
		} else if (subCommand.equalsIgnoreCase("version")) {
			printVersion(sender);
		} else if (subCommand.equalsIgnoreCase("dump")) {
			dump(sender);
		} else {
			return false;
		}

		return true;
	}
	
	public void checkVersion(final CommandSender sender, boolean command) {
		performUpdate(sender, UpdateType.NO_DOWNLOAD, command);
	}
	
	public void updateVersion(final CommandSender sender, boolean command) {
		performUpdate(sender, UpdateType.DEFAULT, command);
	}
	
	// Display every listener on the server
	private void printListeners(final CommandSender sender) {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		
		sender.sendMessage(ChatColor.GOLD + "Packet listeners:");
		for (PacketListener listener : manager.getPacketListeners()) {
			sender.sendMessage(ChatColor.GOLD + " - " + listener);
		}

		// Along with every asynchronous listener
		sender.sendMessage(ChatColor.GOLD + "Asynchronous listeners:");
		for (PacketListener listener : manager.getAsynchronousManager().getAsyncHandlers()) {
			sender.sendMessage(ChatColor.GOLD + " - " + listener);
		}
	}
	
	private void performUpdate(final CommandSender sender, UpdateType type, final boolean command) {
		if (updater.isChecking()) {
			sender.sendMessage(ChatColor.RED + "Already checking for an update.");
			return;
		}

		// Perform on an async thread
		Runnable notify = new Runnable() {
			@Override
			public void run() {
				if (command) {
					sender.sendMessage(ChatColor.YELLOW + "[ProtocolLib] " + updater.getResult());
					String remoteVersion = updater.getRemoteVersion();
					if (remoteVersion != null) {
						sender.sendMessage(ChatColor.YELLOW + "Remote version: " + remoteVersion);
						sender.sendMessage(ChatColor.YELLOW + "Current version: " + plugin.getDescription().getVersion());
					}
				} else if (updater.shouldNotify() || config.isDebug()) {
					sender.sendMessage(ChatColor.YELLOW + "[ProtocolLib] " + updater.getResult());
				}

				updater.removeListener(this);
				updateFinished();
			}
		};
		updater.start(type);
		updater.addListener(notify);
	}
	
	private void toggleTimings(CommandSender sender, String[] args) {
		TimedListenerManager manager = TimedListenerManager.getInstance();
		boolean state = !manager.isTiming(); // toggle
		
		// Parse the boolean parameter
		if (args.length == 2) {
			Boolean parsed = parseBoolean(toQueue(args, 2), "start");
			
			if (parsed != null) {
				state = parsed;
			} else {
				sender.sendMessage(ChatColor.RED + "Specify a state: ON or OFF.");
				return;
			}
		} else if (args.length > 2) {
			sender.sendMessage(ChatColor.RED + "Too many parameters.");
			return;
		}
		
		// Now change the state
		if (state) {
			if (manager.startTiming())
				sender.sendMessage(ChatColor.GOLD + "Started timing packet listeners.");
			else
				sender.sendMessage(ChatColor.RED + "Packet timing already started.");
		} else {
			if (manager.stopTiming()) {
				saveTimings(manager);
				sender.sendMessage(ChatColor.GOLD + "Stopped and saved result in plugin folder.");
			} else {
				sender.sendMessage(ChatColor.RED + "Packet timing already stopped.");
			}
 		}
	}
	
	private void saveTimings(TimedListenerManager manager) {
		try {
			File destination = new File(plugin.getDataFolder(), "Timings - " + System.currentTimeMillis() + ".txt");
			TimingReportGenerator generator = new TimingReportGenerator();
			
			// Print to a text file
			generator.saveTo(destination, manager);
			manager.clear();
			
		} catch (IOException e) {
			reporter.reportMinimal(plugin, "saveTimings()", e);
		}
	}
	
	/**
	 * Prevent further automatic updates until the next delay.
	 */
	public void updateFinished() {
		long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

		config.setAutoLastTime(currentTime);
		config.saveAll();
	}
	
	public void reloadConfiguration(CommandSender sender) {
		plugin.reloadConfig();
		sender.sendMessage(ChatColor.YELLOW + "Reloaded configuration!");
	}

	private void printVersion(CommandSender sender) {
		PluginDescriptionFile desc = plugin.getDescription();

		sender.sendMessage(ChatColor.GREEN + desc.getName() + ChatColor.WHITE + " v" + ChatColor.GREEN + desc.getVersion());
		sender.sendMessage(ChatColor.WHITE + "Authors: " + ChatColor.GREEN + "dmulloy2" + ChatColor.WHITE + " and " + ChatColor.GREEN + "Comphenix");
		sender.sendMessage(ChatColor.WHITE + "Issues: " + ChatColor.GREEN + "https://github.com/dmulloy2/ProtocolLib/issues");
	}

	private static SimpleDateFormat FILE_FORMAT;
	private static SimpleDateFormat TIMESTAMP_FORMAT;

	private void dump(CommandSender sender) {
		Closer closer = Closer.create();

		if (FILE_FORMAT == null)
			FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		if (TIMESTAMP_FORMAT == null)
			TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

		try {
			Date date = new Date();
			File file = new File(plugin.getDataFolder(), "dump-" + FILE_FORMAT.format(date) + ".txt");
			if (file.exists()) {
				file.delete();
			}

			file.createNewFile();
			
			FileWriter fw = closer.register(new FileWriter(file));
			PrintWriter pw = closer.register(new PrintWriter(fw));

			pw.println("ProtocolLib Dump");
			pw.println("Timestamp: " + TIMESTAMP_FORMAT.format(date));
			pw.println();

			pw.println("ProtocolLib Version: " + plugin.toString());
			pw.println("Bukkit Version: " + plugin.getServer().getBukkitVersion());
			pw.println("Server Version: " + plugin.getServer().getVersion());
			pw.println("Java Version: " + System.getProperty("java.version"));
			pw.println();

			ProtocolManager manager = ProtocolLibrary.getProtocolManager();
			pw.println("ProtocolLib: " + DetailedErrorReporter.getStringDescription(plugin));
			pw.println("Manager: " + DetailedErrorReporter.getStringDescription(manager));
			pw.println();

			Set<PacketListener> listeners = manager.getPacketListeners();
			Set<Plugin> plugins = new HashSet<>();

			if (!listeners.isEmpty()) {
				pw.println("Listeners:");

				for (PacketListener listener : listeners) {
					pw.println(DetailedErrorReporter.getStringDescription(listener));

					Plugin plugin = listener.getPlugin();
					if (plugin != null) {
						plugins.add(plugin);
					} else {
						pw.println("(Missing plugin!)");
					}
				}

				pw.println();
			} else {
				pw.println("No listeners");
			}

			if (!plugins.isEmpty()) {
				pw.println("Plugins Using ProtocolLib:");
				for (Plugin plugin : plugins) {
					pw.println(plugin.getName() + " by " + plugin.getDescription().getAuthors());
				}
			}

			sender.sendMessage("Data dump written to " + file.getAbsolutePath());
		} catch (IOException ex) {
			ProtocolLogger.log(Level.SEVERE, "Failed to create dump:", ex);
			sender.sendMessage(ChatColor.RED + "Failed to create dump! Check console!");
		} finally {
			closer.close();
		}
	}
}
