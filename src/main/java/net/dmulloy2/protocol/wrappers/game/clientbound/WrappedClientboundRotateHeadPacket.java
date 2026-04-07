package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
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
 * <p>Use {@link WrappedClientboundAddEntityPacket#angleToByte(float)} and
 * {@link WrappedClientboundAddEntityPacket#byteToAngle(byte)} to convert between
 * degrees and the packed byte representation.
 */
public class WrappedClientboundRotateHeadPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_HEAD_ROTATION;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getEntityClass(), BukkitUnwrapper.getInstance())
            .withParam(byte.class);

    public WrappedClientboundRotateHeadPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRotateHeadPacket(int entityId, byte yHeadRot) {
        this();
        setEntityId(entityId);
        setYHeadRot(yHeadRot);
    }

    public WrappedClientboundRotateHeadPacket(Entity entity, byte yHeadRot) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(entity, yHeadRot)));
    }

    public WrappedClientboundRotateHeadPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().writeSafely(0, entityId);
    }

    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).readSafely(0);
    }

    public void setEntity(Entity entity) {
        handle.getEntityModifier(entity.getWorld()).writeSafely(0, entity);
    }

    /** Returns the head yaw as a packed byte (256 units = 360°). */
    public byte getYHeadRot() {
        return handle.getBytes().readSafely(0);
    }

    public void setYHeadRot(byte yHeadRot) {
        handle.getBytes().writeSafely(0, yHeadRot);
    }
}
