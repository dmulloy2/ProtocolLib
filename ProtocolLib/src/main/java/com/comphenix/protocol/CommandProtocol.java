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

import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.metrics.Updater;
import com.comphenix.protocol.metrics.Updater.UpdateType;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimingReportGenerator;

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
		if (subCommand.equalsIgnoreCase("config") || subCommand.equalsIgnoreCase("reload"))
			reloadConfiguration(sender);
		else if (subCommand.equalsIgnoreCase("check"))
			checkVersion(sender);
		else if (subCommand.equalsIgnoreCase("update"))
			updateVersion(sender);
		else if (subCommand.equalsIgnoreCase("timings"))
			toggleTimings(sender, args);
		else if (subCommand.equalsIgnoreCase("listeners"))
			printListeners(sender, args);
		else
			return false;
		return true;
	}
	
	public void checkVersion(final CommandSender sender) {
		performUpdate(sender, UpdateType.NO_DOWNLOAD);
	}
	
	public void updateVersion(final CommandSender sender) {
		performUpdate(sender, UpdateType.DEFAULT);
	}
	
	// Display every listener on the server
	private void printListeners(final CommandSender sender, String[] args) {
		for (PacketListener listener : ProtocolLibrary.getProtocolManager().getPacketListeners()) {
			sender.sendMessage(ChatColor.GOLD + "Packet listeners:");
			sender.sendMessage(ChatColor.GOLD + " " + listener);
		}
	}
	
	private void performUpdate(final CommandSender sender, UpdateType type) {
		if (updater.isChecking()) {
			sender.sendMessage(ChatColor.RED + "Already checking for an update.");
			return;
		}
		
		// Perform on an async thread
		Runnable notify = new Runnable() {
			@Override
			public void run() {
				sender.sendMessage(ChatColor.BLUE + "[ProtocolLib] " + updater.getResult());
				
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
			Boolean parsed = parseBoolean(args, "start", 2);
			
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
		long currentTime = System.currentTimeMillis() / ProtocolLibrary.MILLI_PER_SECOND;

		config.setAutoLastTime(currentTime);
		config.saveAll();
	}
	
	public void reloadConfiguration(CommandSender sender) {
		plugin.reloadConfig();
		sender.sendMessage(ChatColor.BLUE + "Reloaded configuration!");
	}
}
