package dev.protocollib.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface ProtocolLib {

    PacketListenerBuilder createListener(Plugin plugin);

    Connection connection(Player player);

}
