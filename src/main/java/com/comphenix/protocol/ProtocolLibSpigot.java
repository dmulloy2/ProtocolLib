package com.comphenix.protocol;

import com.comphenix.protocol.error.Report;

import org.bukkit.command.PluginCommand;

public final class ProtocolLibSpigot extends ProtocolLib {
    @Override
    void registerLoginListener() {
        getServer().getPluginManager().registerEvents(new LegacyLoginListener(protocolManager), this);
    }

    @Override
    void registerCommand(String name, CommandBase command) {
        try {
            PluginCommand pluginCmd = this.getCommand(name);

            // Try to load the command
            if (pluginCmd != null) {
                pluginCmd.setExecutor((sender, _c, _l, args) ->
                    command.onCommand(sender, args));
            } else {
                throw new RuntimeException("plugin.yml might be corrupt.");
            }
        } catch (RuntimeException e) {
            reporter.reportWarning(this,
                Report.newBuilder(REPORT_CANNOT_REGISTER_COMMAND).messageParam(name, e.getMessage()).error(e));
        }
    }
}
