package com.comphenix.protocol.spigot;

import java.util.function.Consumer;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.internal.PlatformProvider;

import io.netty.channel.Channel;

public final class SpigotPlatformProvider implements PlatformProvider {

    @Override
    public boolean hasEarlyChannelInitialization() {
        return false;
    }

    @Override
    public Runnable registerChannelInitializer(Consumer<Channel> channelInitializer) {
        throw new UnsupportedOperationException("Early channel initialization is unavailable");
    }

    @Override
    public void registerCommand(ProtocolLib plugin, String name, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            throw new IllegalStateException("plugin.yml might be corrupt.");
        }

        command.setExecutor(executor);
    }
}
