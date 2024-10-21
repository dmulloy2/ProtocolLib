package dev.protocollib.api;

/**
 * Representing a raw binary packet with a packet id and payload.
 */
public interface BinaryPacket {

    /**
     * Retrieves the packet id.
     *
     * @return the packet ID
     */
    int id();

    /**
     * Retrieves the payload (data) of the packet.
     *
     * @return the packet payload as a byte array
     */
    byte[] payload();
}

