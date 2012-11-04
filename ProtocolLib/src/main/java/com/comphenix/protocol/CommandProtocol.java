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
	private ProtocolConfig config;
	
	public CommandProtocol(Plugin plugin, Updater updater, ProtocolConfig config) {
		super(CommandBase.PERMISSION_ADMIN, NAME, 1);
		this.plugin = plugin;
		this.updater = updater;
		this.config = config;
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
				sender.sendMessage(ChatColor.BLUE + "Version check: " + result.toString());
			}
		});
		
		updateFinished();
	}
	
	public void updateVersion(final CommandSender sender) {
		// Perform on an async thread
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				UpdateResult result = updater.update(UpdateType.DEFAULT, true);
				sender.sendMessage(ChatColor.BLUE + "Update: " + result.toString());
			}
		});
		
		updateFinished();
	}
	
	/**
	 * Prevent further automatic updates until the next delay.
	 */
	public void updateFinished() {
		config.setAutoLastTime(System.currentTimeMillis());
		config.saveAll();
	}
	
	public void reloadConfiguration(CommandSender sender) {
		plugin.saveConfig();
		plugin.reloadConfig();
		sender.sendMessage(ChatColor.BLUE + "Reloaded configuration!");
	}
}
