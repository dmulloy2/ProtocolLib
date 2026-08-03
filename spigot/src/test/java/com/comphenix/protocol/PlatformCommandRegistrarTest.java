package com.comphenix.protocol;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.Test;

import com.comphenix.protocol.spigot.SpigotPlatformProvider;

class PlatformCommandRegistrarTest {

    @Test
    void registersLegacyExecutorFromPluginDescriptor() {
        ProtocolLib plugin = mock(ProtocolLib.class);
        CommandExecutor executor = mock(CommandExecutor.class);
        PluginCommand command = mock(PluginCommand.class);
        when(plugin.getCommand("protocol")).thenReturn(command);

        new SpigotPlatformProvider().registerCommand(plugin, "protocol", executor);

        verify(command).setExecutor(executor);
    }
}
