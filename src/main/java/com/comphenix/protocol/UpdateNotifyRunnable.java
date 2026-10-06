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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.updater.Updater;

/**
 * Callback invoked on the main thread after an asynchronous update check completes.
 * <p>
 * Extracted as a top-level class (instead of an anonymous inner class) so that
 * {@code PaperPluginClassLoader} can always resolve its {@code .class} entry when
 * {@link org.bukkit.scheduler.BukkitScheduler#runTask} dispatches it back onto the
 * main server thread — the same pattern used by {@code SpigotUpdateRunnable}.
 */
final class UpdateNotifyRunnable implements Runnable {

    private final CommandSender sender;
    private final boolean command;
    private final Updater updater;
    private final ProtocolConfig config;
    private final Plugin plugin;
    private final CommandProtocol owner;

    UpdateNotifyRunnable(CommandSender sender, boolean command,
                         Updater updater, ProtocolConfig config,
                         Plugin plugin, CommandProtocol owner) {
        this.sender = sender;
        this.command = command;
        this.updater = updater;
        this.config = config;
        this.plugin = plugin;
        this.owner = owner;
    }

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
        owner.updateFinished();
    }
}
