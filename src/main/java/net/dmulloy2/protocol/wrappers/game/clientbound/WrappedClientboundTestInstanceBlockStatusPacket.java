package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundTestInstanceBlockStatus} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code Component status} – the status message</li>
 *   <li>{@code Optional<Vec3i> size} – optional test block size (opaque NMS type, no ProtocolLib accessor)</li>
 * </ul>
 */
public class WrappedClientboundTestInstanceBlockStatusPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TEST_INSTANCE_BLOCK_STATUS;

    public WrappedClientboundTestInstanceBlockStatusPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTestInstanceBlockStatusPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getStatus() {
        return handle.getChatComponents().read(0);
    }

    public void setStatus(WrappedChatComponent status) {
        handle.getChatComponents().write(0, status);
    }
}
