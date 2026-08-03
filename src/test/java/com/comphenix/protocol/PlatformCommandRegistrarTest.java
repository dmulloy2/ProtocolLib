package com.comphenix.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.comphenix.protocol.paper.PaperPlatformProvider;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;

class PlatformCommandRegistrarTest {

    @Test
    void registersLegacyExecutorAsPaperCommand() {
        ProtocolLib plugin = mock(ProtocolLib.class);
        CommandExecutor executor = mock(CommandExecutor.class);
        ArgumentCaptor<BasicCommand> commandCaptor = ArgumentCaptor.forClass(BasicCommand.class);

        new PaperPlatformProvider().registerCommand(plugin, "filter", executor);

        verify(plugin).registerCommand(
                eq("filter"),
                eq("Add or remove programmable filters to the packet listeners."),
                eq(List.of("packet_filter")),
                commandCaptor.capture());

        CommandSourceStack source = mock(CommandSourceStack.class);
        CommandSender sender = mock(CommandSender.class);
        when(source.getSender()).thenReturn(sender);

        ArgumentCaptor<Command> legacyCommand = ArgumentCaptor.forClass(Command.class);
        String[] arguments = {"add"};
        when(executor.onCommand(eq(sender), legacyCommand.capture(), eq("filter"), eq(arguments)))
                .thenReturn(false);

        commandCaptor.getValue().execute(source, arguments);

        assertEquals("filter", legacyCommand.getValue().getName());
        assertEquals("protocol.admin", commandCaptor.getValue().permission());
        verify(sender).sendMessage("/filter add|remove name [ID start]-[ID stop]");
    }
}
