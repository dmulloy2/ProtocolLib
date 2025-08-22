package com.comphenix.protocol;

import java.lang.reflect.Field;
import java.util.logging.Level;

import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import io.netty.channel.Channel;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.connection.ReadablePlayerCookieConnectionImpl;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

class PaperLoginListener implements Listener {
    private final PacketFilterManager manager;

    PaperLoginListener(PacketFilterManager manager) {
        this.manager = manager;
    }

    private static FieldAccessor networkManagerField;
    private static FieldAccessor channelField;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerConnectionValidateLogin(PlayerConnectionValidateLoginEvent event) {
        try {
            PlayerConnection connection = event.getConnection();
            ProtocolLogger.log("onPlayerConnectionValidateLogin: " + connection.getAddress());

            if (networkManagerField == null) {
                FuzzyReflection fuzzy = FuzzyReflection.fromClass(ReadablePlayerCookieConnectionImpl.class, true);
                Field field = fuzzy.getFieldByType("networkManager", MinecraftReflection.getNetworkManagerClass());
                networkManagerField = Accessors.getFieldAccessor(field);
            }

            Object networkManager = networkManagerField.get(connection);

            if (channelField == null) {
                FuzzyReflection fuzzy = FuzzyReflection.fromObject(networkManager, true);
                Field field = fuzzy.getFieldByType("channel", Channel.class);
                channelField = Accessors.getFieldAccessor(field);
            }

            Channel channel = (Channel) channelField.get(networkManager);
            manager.injectChannel(channel);
            ProtocolLogger.log("Successfully injected channel " + channel);
        } catch (Throwable ex) {
            ProtocolLogger.log(Level.SEVERE, "Failed to inject channel", ex);
        }
    }
}
