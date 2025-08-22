package com.comphenix.protocol;

import com.comphenix.protocol.injector.PacketFilterManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

class BukkitListener implements Listener {
    private final PacketFilterManager manager;

    BukkitListener(PacketFilterManager manager) {
        this.manager = manager;
    }

//    @EventHandler(priority = EventPriority.LOWEST)
//    public void handleLogin(PlayerLoginEvent event) {
//        networkManagerInjector.getInjector(event.getPlayer()).inject();
//    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleJoin(PlayerJoinEvent event) {
        manager.injectPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleQuit(PlayerQuitEvent event) {
        manager.removePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handlePluginUnload(PluginDisableEvent event) {
        manager.removePacketListeners(event.getPlugin());
    }
}
