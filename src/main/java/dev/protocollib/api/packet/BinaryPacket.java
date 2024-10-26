package dev.protocollib.api.packet;

/**
 * Representing a raw binary packet with a packet id and payload.
 */
public non-sealed interface BinaryPacket extends PacketLike {

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
