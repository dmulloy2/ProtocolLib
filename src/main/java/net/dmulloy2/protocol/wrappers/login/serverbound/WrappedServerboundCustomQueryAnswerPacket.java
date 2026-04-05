package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundCustomQueryAnswerPacket} (login phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int transactionId} – matches the transaction ID from the server's query packet</li>
 *   <li>{@code CustomQueryAnswerPayload payload} – channel-specific response data (may be null)</li>
 * </ul>
 *
 * <p>Note: the NMS field uses {@code CustomQueryAnswerPayload} (a login-specific interface), not
 * the common {@code CustomPacketPayload}. The {@code payload} field is therefore not accessible via
 * the standard {@code getCustomPacketPayloads()} modifier and will always read as {@code null}.
 */
public class WrappedServerboundCustomQueryAnswerPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Client.CUSTOM_PAYLOAD;

    public WrappedServerboundCustomQueryAnswerPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundCustomQueryAnswerPacket(int transactionId, CustomPacketPayloadWrapper payload) {
        this();
        setTransactionId(transactionId);
        setPayload(payload);
    }

    public WrappedServerboundCustomQueryAnswerPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTransactionId() {
        return handle.getIntegers().read(0);
    }

    public void setTransactionId(int transactionId) {
        handle.getIntegers().write(0, transactionId);
    }

    /**
     * Returns the response payload, or {@code null} if the client does not recognise the channel.
     */
    public CustomPacketPayloadWrapper getPayload() {
        return handle.getCustomPacketPayloads().readSafely(0);
    }

    public void setPayload(CustomPacketPayloadWrapper payload) {
        handle.getCustomPacketPayloads().writeSafely(0, payload);
    }
}
