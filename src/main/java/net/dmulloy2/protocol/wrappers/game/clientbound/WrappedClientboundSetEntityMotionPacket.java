package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * Wrapper for {@code ClientboundSetEntityMotionPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – entity ID</li>
 *   <li>{@code Vec3 movement} – velocity vector</li>
 * </ul>
 */
public class WrappedClientboundSetEntityMotionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_VELOCITY;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter());

    public WrappedClientboundSetEntityMotionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetEntityMotionPacket(int entityId, Vector velocity) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(entityId, velocity)));
    }

    public WrappedClientboundSetEntityMotionPacket(PacketContainer packet) {
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
    // Velocity
    // -------------------------------------------------------------------------

    public Vector getVelocity() {
        return handle.getVectors().read(0);
    }

    public void setVelocity(Vector velocity) {
        handle.getVectors().write(0, velocity);
    }
}
