package dev.protocollib.api;

/**
 * Representing the direction of a packet, either sent to or from the server.
 */
public enum ProtocolDirection {

    /** Packet sent from the client to the server. */
    SERVERBOUND, 

    /** Packet sent from the server to the client. */
    CLIENTBOUND;

}
