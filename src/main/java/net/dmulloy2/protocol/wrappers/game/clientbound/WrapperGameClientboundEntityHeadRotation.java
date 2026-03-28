package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Wrapper for {@code ClientboundRotateHeadPacket} (Play phase, clientbound).
 *
 * <p>Rotates an entity's head independently of its body yaw.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – the entity to rotate</li>
 *   <li>{@code byte yHeadRot} – head yaw packed as a byte (256 = 360°)</li>
 * </ul>
 *
 * <p>Use {@link WrapperGameClientboundSpawnEntity#angleToByte(float)} and
 * {@link WrapperGameClientboundSpawnEntity#byteToAngle(byte)} to convert between
 * degrees and the packed byte representation.
 */
public class WrapperGameClientboundEntityHeadRotation extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_HEAD_ROTATION;

    public WrapperGameClientboundEntityHeadRotation() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundEntityHeadRotation(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    /** Returns the head yaw as a packed byte (256 units = 360°). */
    public byte getYHeadRot() {
        return handle.getBytes().read(0);
    }

    public void setYHeadRot(byte yHeadRot) {
        handle.getBytes().write(0, yHeadRot);
    }
}
