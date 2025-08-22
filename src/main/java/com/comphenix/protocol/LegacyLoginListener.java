package com.comphenix.protocol;

import com.comphenix.protocol.injector.PacketFilterManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

class LegacyLoginListener implements Listener {
    private final PacketFilterManager manager;

    LegacyLoginListener(PacketFilterManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void handleLogin(PlayerLoginEvent event) {
        manager.injectPlayer(event.getPlayer());
    }
}
