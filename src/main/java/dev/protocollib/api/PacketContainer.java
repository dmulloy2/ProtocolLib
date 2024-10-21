package dev.protocollib.api;

/**
 * Representing a container for a packet.
 */
public interface PacketContainer {

    /**
     * Retrieves the type of the packet.
     *
     * @return the packet type
     */
    PacketType packetType();

    /**
     * Retrieves the raw packet object.
     *
     * @return the packet object
     */
    Object packet();

}
