package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;

/**
 * Wraps {@code net.minecraft.world.entity.PositionMoveRotation}, which carries
 * position, velocity delta, yaw, and pitch for entity teleportation.
 */
public class WrappedPositionMoveRotation {

    private double x, y, z;
    private double deltaX, deltaY, deltaZ;
    private float yaw, pitch;

    public WrappedPositionMoveRotation() {}

    public WrappedPositionMoveRotation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Reads a {@code PositionMoveRotation} InternalStructure into this wrapper.
     * Field layout: Vec3 position [0], Vec3 deltaMovement [1], float yRot [0], float xRot [1].
     */
    public static WrappedPositionMoveRotation fromHandle(InternalStructure handle) {
        WrappedPositionMoveRotation wrapper = new WrappedPositionMoveRotation();

        Vector position = handle.getVectors().read(0);
        Vector delta    = handle.getVectors().read(1);

        wrapper.x      = position.getX();
        wrapper.y      = position.getY();
        wrapper.z      = position.getZ();
        wrapper.deltaX = delta.getX();
        wrapper.deltaY = delta.getY();
        wrapper.deltaZ = delta.getZ();
        wrapper.yaw    = handle.getFloat().read(0);
        wrapper.pitch  = handle.getFloat().read(1);

        return wrapper;
    }

    /**
     * Returns an {@link EquivalentConverter} that converts between
     * {@link WrappedPositionMoveRotation} and the NMS {@code PositionMoveRotation} record.
     *
     * <p>Reading delegates to {@link #fromHandle(InternalStructure)}; writing
     * constructs a fresh NMS record via its public constructor.
     */
    public static EquivalentConverter<WrappedPositionMoveRotation> getConverter() {
        return new EquivalentConverter<WrappedPositionMoveRotation>() {

            @Override
            public WrappedPositionMoveRotation getSpecific(Object generic) {
                InternalStructure structure = InternalStructure.getConverter().getSpecific(generic);
                return fromHandle(structure);
            }

            @Override
            public Object getGeneric(WrappedPositionMoveRotation specific) {
                try {
                    Class<?> vec3Class = MinecraftReflection.getVec3DClass();
                    Class<?> pmrClass  = MinecraftReflection.getMinecraftClass("world.entity.PositionMoveRotation");

                    Constructor<?> vec3Ctor = vec3Class.getDeclaredConstructor(double.class, double.class, double.class);
                    Object posVec3   = vec3Ctor.newInstance(specific.x, specific.y, specific.z);
                    Object deltaVec3 = vec3Ctor.newInstance(specific.deltaX, specific.deltaY, specific.deltaZ);

                    Constructor<?> pmrCtor = pmrClass.getDeclaredConstructor(vec3Class, vec3Class, float.class, float.class);
                    return pmrCtor.newInstance(posVec3, deltaVec3, specific.yaw, specific.pitch);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to construct PositionMoveRotation", e);
                }
            }

            @Override
            public Class<WrappedPositionMoveRotation> getSpecificType() {
                return WrappedPositionMoveRotation.class;
            }
        };
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }

    public double getDeltaX() { return deltaX; }
    public void setDeltaX(double deltaX) { this.deltaX = deltaX; }

    public double getDeltaY() { return deltaY; }
    public void setDeltaY(double deltaY) { this.deltaY = deltaY; }

    public double getDeltaZ() { return deltaZ; }
    public void setDeltaZ(double deltaZ) { this.deltaZ = deltaZ; }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    @Override
    public String toString() {
        return "WrappedPositionMoveRotation{pos=(" + x + ", " + y + ", " + z + ")"
                + ", delta=(" + deltaX + ", " + deltaY + ", " + deltaZ + ")"
                + ", yaw=" + yaw + ", pitch=" + pitch + "}";
    }
}
