package dev.protocollib.api;

public interface PacketType {

    PacketDirection packetDirection();

    Class<?> packetClass();

    boolean isSupported();

    boolean isDeprecated();

}
