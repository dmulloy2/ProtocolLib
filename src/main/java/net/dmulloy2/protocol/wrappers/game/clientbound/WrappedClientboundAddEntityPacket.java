package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Wrapper for {@code ClientboundAddEntityPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – entity ID</li>
 *   <li>{@code UUID uuid} – entity UUID</li>
 *   <li>{@code EntityType<?> type} – entity type</li>
 *   <li>{@code double x, y, z} – spawn position</li>
 *   <li>{@code Vec3 movement} – initial velocity</li>
 *   <li>{@code byte xRot, yRot} – pitch and yaw (packed: 256 = 360°)</li>
 *   <li>{@code byte yHeadRot} – head yaw (packed)</li>
 *   <li>{@code int data} – optional type-specific data</li>
 * </ul>
 */
public class WrappedClientboundAddEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SPAWN_ENTITY;

    public WrappedClientboundAddEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundAddEntityPacket(PacketContainer packet) {
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
    // UUID
    // -------------------------------------------------------------------------

    public UUID getEntityUUID() {
        return handle.getUUIDs().read(0);
    }

    public void setEntityUUID(UUID uuid) {
        handle.getUUIDs().write(0, uuid);
    }

    // -------------------------------------------------------------------------
    // Entity type
    // -------------------------------------------------------------------------

    public EntityType getEntityType() {
        return handle.getEntityTypeModifier().read(0);
    }

    public void setEntityType(EntityType type) {
        handle.getEntityTypeModifier().write(0, type);
    }

    // -------------------------------------------------------------------------
    // Position
    // -------------------------------------------------------------------------

    public double getX() { return handle.getDoubles().read(0); }
    public void setX(double x) { handle.getDoubles().write(0, x); }

    public double getY() { return handle.getDoubles().read(1); }
    public void setY(double y) { handle.getDoubles().write(1, y); }

    public double getZ() { return handle.getDoubles().read(2); }
    public void setZ(double z) { handle.getDoubles().write(2, z); }

    // -------------------------------------------------------------------------
    // Initial velocity
    // -------------------------------------------------------------------------

    public Vector getVelocity() {
        return handle.getVectors().read(0);
    }

    public void setVelocity(Vector velocity) {
        handle.getVectors().write(0, velocity);
    }

    // -------------------------------------------------------------------------
    // Rotation (packed bytes: value / 256.0 * 360.0 = degrees)
    // -------------------------------------------------------------------------

    public byte getPitchByte() { return handle.getBytes().read(0); }
    public void setPitchByte(byte xRot) { handle.getBytes().write(0, xRot); }

    public byte getYawByte() { return handle.getBytes().read(1); }
    public void setYawByte(byte yRot) { handle.getBytes().write(1, yRot); }

    public byte getHeadYawByte() { return handle.getBytes().read(2); }
    public void setHeadYawByte(byte yHeadRot) { handle.getBytes().write(2, yHeadRot); }

    /** Converts a packed byte angle to degrees. */
    public static float byteToAngle(byte packed) {
        return (packed & 0xFF) / 256.0f * 360.0f;
    }

    /** Converts degrees to a packed byte angle. */
    public static byte angleToByte(float degrees) {
        return (byte) Math.round(degrees / 360.0f * 256.0f);
    }

    // -------------------------------------------------------------------------
    // Extra data
    // -------------------------------------------------------------------------

    /** Type-specific extra data (e.g. projectile data, falling-block ID). */
    public int getData() {
        return handle.getIntegers().read(1);
    }

    public void setData(int data) {
        handle.getIntegers().write(1, data);
    }
}
