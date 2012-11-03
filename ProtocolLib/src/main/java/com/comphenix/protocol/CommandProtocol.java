package com.comphenix.protocol;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.metrics.Updater;
import com.comphenix.protocol.metrics.Updater.UpdateResult;
import com.comphenix.protocol.metrics.Updater.UpdateType;

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
	
	public CommandProtocol(Plugin plugin, Updater updater) {
		super(CommandBase.PERMISSION_ADMIN, NAME, 1);
		this.plugin = plugin;
		this.updater = updater;
	}
	
	@Override
	protected boolean handleCommand(CommandSender sender, String[] args) {
		String subCommand = args[0];
		
		// Only return TRUE if we executed the correct command
		if (subCommand.equalsIgnoreCase("config"))
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
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				UpdateResult result = updater.update(UpdateType.NO_DOWNLOAD, true);
				sender.sendMessage(ChatColor.DARK_BLUE + "Version check: " + result.toString());
			}
		});
	}
	
	public void updateVersion(final CommandSender sender) {
		// Perform on an async thread
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				UpdateResult result = updater.update(UpdateType.DEFAULT, true);
				sender.sendMessage(ChatColor.DARK_BLUE + "Update: " + result.toString());
			}
		});
	}
	
	public void reloadConfiguration(CommandSender sender) {
		plugin.saveConfig();
		plugin.reloadConfig();
		sender.sendMessage(ChatColor.DARK_BLUE + "Reloaded configuration!");
	}
}
