package dev.protocollib.api;

public interface BinaryPacket {

    int id();

    byte[] payload();
}
