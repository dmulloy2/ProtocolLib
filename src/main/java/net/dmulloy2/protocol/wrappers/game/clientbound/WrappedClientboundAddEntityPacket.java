package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

/**
 * Wrapper for {@code ClientboundAddEntityPacket} (Play phase, clientbound).
 *
 * <p>The all-args constructor invokes the canonical NMS constructor through
 * {@link EquivalentConstructor}, so values are populated by NMS exactly as if
 * the server had constructed the packet itself.
 *
 * <p>The wrapper accepts pitch/yaw/head-yaw as <em>packed bytes</em> (the on-the-wire
 * representation, 256 units = 360°) for parity with the field-level getters/setters;
 * NMS internally stores them the same way and we round-trip through
 * {@link #byteToAngle(byte)} on the way in. Use {@link #byteToAngle(byte)} /
 * {@link #angleToByte(float)} when you need degrees.
 *
 * <p>Packet fields (NMS declaration order):
 * <ul>
 *   <li>{@code int id} – entity ID</li>
 *   <li>{@code UUID uuid} – entity UUID</li>
 *   <li>{@code EntityType<?> type} – entity type</li>
 *   <li>{@code double x, y, z} – spawn position</li>
 *   <li>{@code Vec3 movement} – initial velocity</li>
 *   <li>{@code byte xRot} – pitch, packed (256 units = 360°)</li>
 *   <li>{@code byte yRot} – yaw, packed</li>
 *   <li>{@code byte yHeadRot} – head yaw, packed</li>
 *   <li>{@code int data} – optional type-specific data</li>
 * </ul>
 */
public class WrappedClientboundAddEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SPAWN_ENTITY;

    // NMS canonical ctor on Spigot 26.1:
    //   ClientboundAddEntityPacket(int id, UUID uuid, double x, double y, double z,
    //       float xRot, float yRot, EntityType<?> type, int data, Vec3 movement, double yHeadRot)
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(UUID.class)
            .withParam(double.class)
            .withParam(double.class)
            .withParam(double.class)
            .withParam(float.class)
            .withParam(float.class)
            .withParam(MinecraftReflection.getMinecraftClass("world.entity.EntityType"),
                    BukkitConverters.getEntityTypeConverter())
            .withParam(int.class)
            .withParam(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter())
            .withParam(double.class);

    public WrappedClientboundAddEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundAddEntityPacket(int entityId, UUID entityUUID, EntityType entityType,
            double x, double y, double z, Vector velocity,
            byte pitchByte, byte yawByte, byte headYawByte, int data) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(
                entityId, entityUUID, x, y, z,
                byteToAngle(pitchByte), byteToAngle(yawByte),
                entityType, data, velocity,
                (double) byteToAngle(headYawByte))));
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

    /** Resolves the live entity in the given world via entity ID. */
    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    /**
     * Convenience setter that copies both the entity ID and UUID from a Bukkit entity.
     * Does not set the entity type, position, velocity, or rotations — set those separately.
     */
    public void setEntity(Entity entity) {
        setEntityId(entity.getEntityId());
        setEntityUUID(entity.getUniqueId());
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
    // Initial velocity (Vec3)
    // -------------------------------------------------------------------------

    public Vector getVelocity() {
        return handle.getVectors().read(0);
    }

    public void setVelocity(Vector velocity) {
        handle.getVectors().write(0, velocity);
    }

    // -------------------------------------------------------------------------
    // Packed rotation bytes  (256 units = 360°)
    // -------------------------------------------------------------------------

    /** Pitch (xRot) as a packed byte. Convert with {@link #byteToAngle(byte)}. */
    public byte getPitchByte() { return handle.getBytes().read(0); }
    public void setPitchByte(byte xRot) { handle.getBytes().write(0, xRot); }

    /** Yaw (yRot) as a packed byte. Convert with {@link #byteToAngle(byte)}. */
    public byte getYawByte() { return handle.getBytes().read(1); }
    public void setYawByte(byte yRot) { handle.getBytes().write(1, yRot); }

    /** Head yaw (yHeadRot) as a packed byte. Convert with {@link #byteToAngle(byte)}. */
    public byte getHeadYawByte() { return handle.getBytes().read(2); }
    public void setHeadYawByte(byte yHeadRot) { handle.getBytes().write(2, yHeadRot); }

    /**
     * Converts a packed byte angle to degrees, matching NMS {@code Mth.unpackDegrees}.
     * Treats {@code packed} as a signed byte: {@code (byte * 360) / 256.0f}.
     */
    public static float byteToAngle(byte packed) {
        return (packed * 360) / 256.0f;
    }

    /**
     * Converts degrees to a packed byte angle, matching NMS {@code Mth.packDegrees}.
     * Uses {@code Math.floor} (not int truncation) so negative angles round toward
     * negative infinity, matching NMS exactly.
     */
    public static byte angleToByte(float degrees) {
        return (byte) (int) Math.floor(degrees * 256.0f / 360.0f);
    }

    // -------------------------------------------------------------------------
    // Extra data
    // -------------------------------------------------------------------------

    /** Type-specific extra data (e.g. projectile type, falling-block state ID). */
    public int getData() {
        return handle.getIntegers().read(1);
    }

    public void setData(int data) {
        handle.getIntegers().write(1, data);
    }
}
