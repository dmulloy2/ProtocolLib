package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundRotateHeadPacket} / entity look packet
 * (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity ID</li>
 *   <li>{@code byte yaw} – packed yaw (use {@link WrappedClientboundAddEntityPacket#angleToByte}
 *       / {@link WrappedClientboundAddEntityPacket#byteToAngle} for conversion)</li>
 *   <li>{@code byte pitch} – packed pitch</li>
 *   <li>{@code boolean onGround} – whether the entity is on the ground</li>
 * </ul>
 */
public class WrappedClientboundMoveEntityRotPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_LOOK;

    public WrappedClientboundMoveEntityRotPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundMoveEntityRotPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public byte getYaw() {
        return handle.getBytes().read(0);
    }

    public void setYaw(byte yaw) {
        handle.getBytes().write(0, yaw);
    }

    public byte getPitch() {
        return handle.getBytes().read(1);
    }

    public void setPitch(byte pitch) {
        handle.getBytes().write(1, pitch);
    }

    public boolean isOnGround() {
        return handle.getBooleans().read(0);
    }

    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(0, onGround);
    }
}
