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

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Base class for all our commands.
 * 
 * @author Kristian
 */
abstract class CommandBase implements CommandExecutor {
	public static final ReportType REPORT_COMMAND_ERROR = new ReportType("Cannot execute command %s.");
	public static final ReportType REPORT_UNEXPECTED_COMMAND = new ReportType("Incorrect command assigned to %s.");
	
	public static final String PERMISSION_ADMIN = "protocol.admin";
	
	private String permission;
	private String name;
	private int minimumArgumentCount;
	
	protected ErrorReporter reporter;
	
	public CommandBase(ErrorReporter reporter, String permission, String name) {
		this(reporter, permission, name, 0);
	}
	
	public CommandBase(ErrorReporter reporter, String permission, String name, int minimumArgumentCount) {
		this.reporter = reporter;
		this.name = name;
		this.permission = permission;
		this.minimumArgumentCount = minimumArgumentCount;
	}

	@Override
	public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			// Make sure we're dealing with the correct command
			if (!command.getName().equalsIgnoreCase(name)) {
				reporter.reportWarning(this, Report.newBuilder(REPORT_UNEXPECTED_COMMAND).messageParam(this));
				return false;
			}

			if (permission != null && !sender.hasPermission(permission)) {
				sender.sendMessage(ChatColor.RED + "You haven't got permission to run this command.");
				return true;
			}
			
			// Check argument length
			if (args != null && args.length >= minimumArgumentCount) {
				return handleCommand(sender, args);
			} else {
				sender.sendMessage(ChatColor.RED + "Insufficient arguments. You need at least " + minimumArgumentCount);
				return false;
			}
		} catch (Throwable ex) {
			reporter.reportDetailed(this,
					Report.newBuilder(REPORT_COMMAND_ERROR).error(ex).messageParam(name).callerParam(sender, label, args)
			);
			return true;
		}
	}

	/**
	 * Parse a boolean value at the head of the queue.
	 * @param arguments - the queue of arguments.
	 * @param parameterName - the parameter name we will match.
	 * @return The parsed boolean, or NULL if not valid.
	 */
	protected Boolean parseBoolean(Deque<String> arguments, String parameterName) {
		Boolean result = null;
		
		if (!arguments.isEmpty()) {
			String arg = arguments.peek();
			
			if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("on"))
				result = true;
			else if (arg.equalsIgnoreCase(parameterName))
				result = true;
			else if (arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("off"))
				result = false;
		}
		
		if (result != null)
			arguments.poll();
		return result;
	}
		
	/**
	 * Create a queue from a sublist of a given array.
	 * @param args - the source array.
	 * @param start - the starting index.
	 * @return A queue that contains every element in the array, starting at the given index.
	 */
	protected Deque<String> toQueue(String[] args, int start) {
		return new ArrayDeque<String>(Arrays.asList(args).subList(start, args.length));
	}
	
	/**
	 * Retrieve the permission necessary to execute this command.
	 * @return The permission, or NULL if not needed.
	 */
	public String getPermission() {
		return permission;
	}
	
	/**
	 * Retrieve the primary name of this command.
	 * @return Primary name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Retrieve the error reporter.
	 * @return Error reporter.
	 */
	protected ErrorReporter getReporter() {
		return reporter;
	}
	
	/**
	 * Main implementation of this command.
	 * @param sender - command sender.
	 * @param args - input arguments.
	 * @return TRUE if the command was successfully handled, FALSE otherwise.
	 */
	protected abstract boolean handleCommand(CommandSender sender, String[] args);
}
