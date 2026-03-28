package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundMoveEntityPacket.PosRot} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity ID</li>
 *   <li>{@code short dx} – delta X in units of 1/4096 of a block</li>
 *   <li>{@code short dy} – delta Y in units of 1/4096 of a block</li>
 *   <li>{@code short dz} – delta Z in units of 1/4096 of a block</li>
 *   <li>{@code byte yaw} – packed yaw angle</li>
 *   <li>{@code byte pitch} – packed pitch angle</li>
 *   <li>{@code boolean onGround} – whether the entity is on the ground</li>
 * </ul>
 */
public class WrappedClientboundMoveEntityPosRotPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.REL_ENTITY_MOVE_LOOK;

    public WrappedClientboundMoveEntityPosRotPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundMoveEntityPosRotPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    /** @return delta X in units of 1/4096 of a block */
    public short getDx() {
        return handle.getShorts().read(0);
    }

    /** @param dx delta X in units of 1/4096 of a block */
    public void setDx(short dx) {
        handle.getShorts().write(0, dx);
    }

    /** @return delta Y in units of 1/4096 of a block */
    public short getDy() {
        return handle.getShorts().read(1);
    }

    /** @param dy delta Y in units of 1/4096 of a block */
    public void setDy(short dy) {
        handle.getShorts().write(1, dy);
    }

    /** @return delta Z in units of 1/4096 of a block */
    public short getDz() {
        return handle.getShorts().read(2);
    }

    /** @param dz delta Z in units of 1/4096 of a block */
    public void setDz(short dz) {
        handle.getShorts().write(2, dz);
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
