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

    // TODO: missing field 'lerpSteps' (NMS type: List<NewMinecartBehavior.MinecartStep>)
    //   Each MinecartStep holds position (Vec3), movement (Vec3), yRot (float), xRot (float), weight (float).
    //   No ProtocolLib accessor exists; use handle.getModifier().read(1) for the raw List,
    //   or add a dedicated WrappedMinecartStep class with AutoWrapper.
}
