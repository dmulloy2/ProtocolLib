package dev.protocollib.api;

public interface PacketContainer {

    PacketType packetType();

    Object packet();

}
