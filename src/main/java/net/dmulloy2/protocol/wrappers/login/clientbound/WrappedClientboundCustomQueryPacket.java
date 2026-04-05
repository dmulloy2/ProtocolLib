package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCustomQueryPacket} (login phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int transactionId} – unique transaction identifier for matching the client's response</li>
 *   <li>{@code CustomQueryPayload payload} – channel-specific payload data</li>
 * </ul>
 *
 * <p>Note: the NMS field uses {@code CustomQueryPayload} (a login-specific interface), not the
 * common {@code CustomPacketPayload}. The {@code payload} field is therefore not accessible via
 * the standard {@code getCustomPacketPayloads()} modifier and will always read as {@code null}.
 */
public class WrappedClientboundCustomQueryPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Server.CUSTOM_PAYLOAD;

    public WrappedClientboundCustomQueryPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCustomQueryPacket(int transactionId, CustomPacketPayloadWrapper payload) {
        this();
        setTransactionId(transactionId);
        setPayload(payload);
    }

    public WrappedClientboundCustomQueryPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTransactionId() {
        return handle.getIntegers().read(0);
    }

    public void setTransactionId(int transactionId) {
        handle.getIntegers().write(0, transactionId);
    }

    public CustomPacketPayloadWrapper getPayload() {
        return handle.getCustomPacketPayloads().readSafely(0);
    }

    public void setPayload(CustomPacketPayloadWrapper payload) {
        handle.getCustomPacketPayloads().writeSafely(0, payload);
    }
}
