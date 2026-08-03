package com.comphenix.protocol.internal;

import java.util.ServiceLoader;
import java.util.function.Consumer;

import org.bukkit.command.CommandExecutor;

import com.comphenix.protocol.ProtocolLib;

import io.netty.channel.Channel;

public interface PlatformProvider {

    static PlatformProvider get() {
        return Holder.INSTANCE;
    }

    boolean hasEarlyChannelInitialization();

    Runnable registerChannelInitializer(Consumer<Channel> channelInitializer);

    void registerCommand(ProtocolLib plugin, String name, CommandExecutor executor);

    final class Holder {

        private static final PlatformProvider INSTANCE = ServiceLoader.load(
                        PlatformProvider.class,
                        PlatformProvider.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No ProtocolLib platform provider is available"));

        private Holder() {
        }
    }
}
