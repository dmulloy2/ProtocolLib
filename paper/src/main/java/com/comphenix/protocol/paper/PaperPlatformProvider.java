package com.comphenix.protocol.paper;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.internal.PlatformProvider;

import io.netty.channel.Channel;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;

public final class PaperPlatformProvider implements PlatformProvider {

    public static final Key CHANNEL_INITIALIZER_KEY = Key.key("protocollib", "channel-initializer");

    @Override
    public boolean hasEarlyChannelInitialization() {
        return true;
    }

    @Override
    public Runnable registerChannelInitializer(Consumer<Channel> channelInitializer) {
        ChannelInitializeListenerHolder.addListener(CHANNEL_INITIALIZER_KEY, channelInitializer::accept);
        return () -> ChannelInitializeListenerHolder.removeListener(CHANNEL_INITIALIZER_KEY);
    }

    @Override
    public void registerCommand(ProtocolLib plugin, String name, CommandExecutor executor) {
        CommandMetadata metadata = getMetadata(name);
        Command command = new RegisteredCommand(name, metadata);

        plugin.registerCommand(
                name,
                metadata.description(),
                metadata.aliases(),
                new RegisteredBasicCommand(name, metadata, command, executor));
    }

    private static CommandMetadata getMetadata(String name) {
        return switch (name) {
            case "protocol" -> new CommandMetadata(
                    "Performs administrative tasks regarding ProtocolLib.",
                    "/<command> config|check|update|timings|listeners|version|dump",
                    "protocol.admin",
                    List.of());
            case "packet" -> new CommandMetadata(
                    "Add or remove a simple packet listener.",
                    "/<command> add|remove|names client|server [ID start]-[ID stop] [detailed]",
                    "protocol.admin",
                    List.of());
            case "filter" -> new CommandMetadata(
                    "Add or remove programmable filters to the packet listeners.",
                    "/<command> add|remove name [ID start]-[ID stop]",
                    "protocol.admin",
                    List.of("packet_filter"));
            case "packetlog" -> new CommandMetadata(
                    "Logs hex representations of packets to a file or console",
                    "/<command> <protocol> <sender> <packet> [location]",
                    "protocol.admin",
                    List.of());
            default -> throw new IllegalArgumentException("Unknown ProtocolLib command " + name);
        };
    }

    private record CommandMetadata(String description, String usage, String permission, List<String> aliases) {
    }

    private record RegisteredBasicCommand(
            String name,
            CommandMetadata metadata,
            Command command,
            CommandExecutor executor) implements BasicCommand {

        @Override
        public void execute(CommandSourceStack source, String[] arguments) {
            if (!this.executor.onCommand(source.getSender(), this.command, this.name, arguments)) {
                source.getSender().sendMessage(this.metadata.usage().replace("<command>", this.name));
            }
        }

        @Override
        public String permission() {
            return this.metadata.permission();
        }
    }

    private static final class RegisteredCommand extends Command {

        private RegisteredCommand(String name, CommandMetadata metadata) {
            super(name, metadata.description(), metadata.usage(), metadata.aliases());
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            throw new UnsupportedOperationException();
        }
    }
}
