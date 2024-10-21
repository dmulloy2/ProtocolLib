package dev.protocollib.api;

@FunctionalInterface
public interface PacketListener {

    void handlePacket(PacketEvent event);

}
