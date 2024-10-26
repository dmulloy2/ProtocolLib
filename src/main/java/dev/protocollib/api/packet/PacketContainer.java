package dev.protocollib.api.packet;

/**
 * Representing a container for a packet.
 */
public non-sealed interface PacketContainer extends PacketLike {

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

    /**
     * Creates and returns a mutable copy of this packet.
     * 
     * @return a clone of this instance.
     */
    PacketContainer clone();

}
