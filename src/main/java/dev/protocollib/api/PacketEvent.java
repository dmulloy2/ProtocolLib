package dev.protocollib.api;

import org.bukkit.event.Cancellable;

public interface PacketEvent extends Cancellable {

    Connection connection();

    PacketContainer packet();

}
