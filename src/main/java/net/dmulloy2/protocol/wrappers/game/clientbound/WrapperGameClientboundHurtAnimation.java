package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Wrapper for {@code ClientboundHurtAnimationPacket} (Play phase, clientbound).
 *
 * <p>Triggers the hurt/damage animation on an entity. The {@code yaw} field
 * controls the direction the hurt animation originates from, in degrees.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – entity ID</li>
 *   <li>{@code float yaw} – yaw angle of the damage source (degrees)</li>
 * </ul>
 */
public class WrapperGameClientboundHurtAnimation extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.HURT_ANIMATION;

    public WrapperGameClientboundHurtAnimation() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundHurtAnimation(PacketContainer packet) {
        super(packet, TYPE);
    }

    // -------------------------------------------------------------------------
    // Entity ID
    // -------------------------------------------------------------------------

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    // -------------------------------------------------------------------------
    // Damage source yaw
    // -------------------------------------------------------------------------

    /** Yaw angle (degrees) of the incoming damage direction. */
    public float getYaw() {
        return handle.getFloat().read(0);
    }

    public void setYaw(float yaw) {
        handle.getFloat().write(0, yaw);
    }
}
