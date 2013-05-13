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

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.metrics.Updater;
import com.comphenix.protocol.metrics.Updater.UpdateResult;
import com.comphenix.protocol.metrics.Updater.UpdateType;
import com.comphenix.protocol.utility.WrappedScheduler;

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
	
	public static final ReportType REPORT_HTTP_ERROR = new ReportType("Http error: %s");
	public static final ReportType REPORT_CANNOT_CHECK_FOR_UPDATES = new ReportType("Cannot check updates for ProtocolLib.");
	public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot update ProtocolLib.");
	
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
		else
			return false;
		return true;
	}
	
	public void checkVersion(final CommandSender sender) {
		// Perform on an async thread
		 WrappedScheduler.runAsynchronouslyOnce(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					UpdateResult result = updater.update(UpdateType.NO_DOWNLOAD, true);
					sender.sendMessage(ChatColor.BLUE + "[ProtocolLib] " + result.toString());
				} catch (Exception e) {
					if (isHttpError(e)) {
						getReporter().reportWarning(this, 
								Report.newBuilder(REPORT_HTTP_ERROR).messageParam(e.getCause().getMessage())
						);
					} else {
						getReporter().reportDetailed(this, Report.newBuilder(REPORT_CANNOT_CHECK_FOR_UPDATES).error(e).callerParam(sender));
					}
				}
			}
		}, 0L);
		
		updateFinished();
	}
	
	public void updateVersion(final CommandSender sender) {
		// Perform on an async thread
		WrappedScheduler.runAsynchronouslyOnce(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					UpdateResult result = updater.update(UpdateType.DEFAULT, true);
					sender.sendMessage(ChatColor.BLUE + "[ProtocolLib] " + result.toString());
				} catch (Exception e) {
					if (isHttpError(e)) {
						getReporter().reportWarning(this, 
								Report.newBuilder(REPORT_HTTP_ERROR).messageParam(e.getCause().getMessage())
						);
					} else {
						getReporter().reportDetailed(this,Report.newBuilder(REPORT_CANNOT_UPDATE_PLUGIN).error(e).callerParam(sender));
					}
				}
			}
		}, 0L);
		
		updateFinished();
	}
	
	private boolean isHttpError(Exception e) { 
		Throwable cause = e.getCause();
		
		if (cause instanceof IOException) {
			// Thanks for making the message a part of the API ...
			return cause.getMessage().contains("HTTP response");
		} else {
			return false;
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
