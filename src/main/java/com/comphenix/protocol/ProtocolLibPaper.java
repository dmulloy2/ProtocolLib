package com.comphenix.protocol;

public final class ProtocolLibPaper extends ProtocolLib {

    @Override
    void registerLoginListener() {
        try {
            Class.forName("io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent");
            getServer().getPluginManager().registerEvents(new com.comphenix.protocol.PaperLoginListener(protocolManager), this);
        } catch (ClassNotFoundException ignored) {
            getServer().getPluginManager().registerEvents(new LegacyLoginListener(protocolManager), this);
        }
    }

    @Override
    public void registerCommand(String name, CommandBase command) {
        registerCommand(name, (stack, args) -> command.onCommand(stack.getSender(), args));
    }
}
