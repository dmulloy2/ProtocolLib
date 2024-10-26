package dev.protocollib.api.packet;

import dev.protocollib.api.listener.PacketTransmissionListener;

/**
 * Builder interface for constructing packet operations.
 * 
 * <p>This interface provides methods to configure how packets are sent
 * or received within ProtocolLib.</p>
 */
public interface PacketOperationBuilder {

    /**
     * Skips processing the packet in ProtocolLib's pipeline.
     * 
     * <p>This method allows the caller to prevent further handling of the packet 
     * by other plugin listeners in the pipeline.</p>
     *
     * @return the same builder for further configuration
     */
    PacketOperationBuilder skipProcessing();

    /**
     * Registers a listener to be called once the packet has been sent or received.
     * 
     * @param listener the listener to be notified upon packet transmission
     * @return the same builder for further configuration
     */
    PacketOperationBuilder postTransmission(PacketTransmissionListener listener);

    /**
     * Sends a packet to the client.
     * 
     * @param packet the {@link PacketContainer} to send
     */
    void send(PacketLike packet);

    /**
     * Receives a packet as if the client had sent it.
     * 
     * @param packet the {@link PacketContainer} to receive
     */
    void receive(PacketLike packet);
}
