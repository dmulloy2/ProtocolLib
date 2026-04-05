package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundMoveMinecartPacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code int entityId} – the entity ID of the minecart</li>
 *   <li>{@code List<MinecartStep> lerpSteps} – interpolation steps (opaque NMS type, no ProtocolLib accessor)</li>
 * </ul>
 */
public class WrappedClientboundMoveMinecartPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.MOVE_MINECART;

    public WrappedClientboundMoveMinecartPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundMoveMinecartPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }
}
