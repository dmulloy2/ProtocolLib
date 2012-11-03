package com.comphenix.protocol;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Base class for all our commands.
 * 
 * @author Kristian
 */
abstract class CommandBase implements CommandExecutor {

	public static final String PERMISSION_ADMIN = "protocol.admin";
	
	private String permission;
	private String name;
	private int minimumArgumentCount;

	public CommandBase(String permission, String name) {
		this(permission, name, 0);
	}
	
	public CommandBase(String permission, String name, int minimumArgumentCount) {
		this.name = name;
		this.permission = permission;
		this.minimumArgumentCount = minimumArgumentCount;
	}

	@Override
	public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Make sure we're dealing with the correct command
		if (!command.getName().equalsIgnoreCase(name)) {
			return false;
		}
		if (!sender.hasPermission(permission)) {
			sender.sendMessage(ChatColor.RED + "You haven't got permission to run this command.");
			return true;
		}
		
		// Check argument length
		if (args != null && args.length >= minimumArgumentCount) {
			return handleCommand(sender, args);
		} else {
			return false;
		}
	}
	
	protected abstract boolean handleCommand(CommandSender sender, String[] args);
}
