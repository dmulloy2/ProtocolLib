package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import org.bukkit.util.Vector;

/**
 * Wrapper around the NMS {@code PositionMoveRotation} record, introduced in Minecraft 1.21.2.
 * <p>
 * This type is used in packets such as {@code ENTITY_TELEPORT} and {@code ENTITY_POSITION_SYNC}
 * to represent combined position, movement delta and rotation.
 * <p>
 * Example usage:
 * <pre>{@code
 * WrappedPositionMoveRotation posRot = WrappedPositionMoveRotation.create(
 *         new Vector(x, y, z),   // position
 *         new Vector(0, 0, 0),   // deltaMovement
 *         yaw,                   // yRot
 *         pitch                  // xRot
 * );
 * packet.getPositionMoveRotation().write(0, posRot);
 * }</pre>
 */
public class WrappedPositionMoveRotation extends AbstractWrapper {

    private static Class<?> NMS_CLASS;
    private static ConstructorAccessor CONSTRUCTOR;
    private static StructureModifier<Object> BASE_MODIFIER;

    private final StructureModifier<Object> modifier;

    private WrappedPositionMoveRotation(Object handle) {
        super(getNmsClass());
        setHandle(handle);
        this.modifier = getBaseModifier().withTarget(handle);
    }

    private static Class<?> getNmsClass() {
        if (NMS_CLASS == null) {
            NMS_CLASS = MinecraftReflection.getPositionMoveRotationClass();
        }
        return NMS_CLASS;
    }

    private static StructureModifier<Object> getBaseModifier() {
        if (BASE_MODIFIER == null) {
            BASE_MODIFIER = new StructureModifier<>(getNmsClass());
        }
        return BASE_MODIFIER;
    }

    private static ConstructorAccessor getConstructor() {
        if (CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(
                    getNmsClass(),
                    MinecraftReflection.getVec3DClass(),
                    MinecraftReflection.getVec3DClass(),
                    float.class,
                    float.class
            );
        }
        return CONSTRUCTOR;
    }

    /**
     * Creates a new {@code WrappedPositionMoveRotation} from the given NMS handle.
     *
     * @param handle - the NMS {@code PositionMoveRotation} instance.
     * @return the wrapper.
     */
    public static WrappedPositionMoveRotation fromHandle(Object handle) {
        return new WrappedPositionMoveRotation(handle);
    }

    /**
     * Creates a new {@code WrappedPositionMoveRotation} with the given values.
     *
     * @param position      - the absolute position.
     * @param deltaMovement - the movement delta (velocity).
     * @param yRot          - the yaw rotation.
     * @param xRot          - the pitch rotation.
     * @return a new wrapper instance.
     */
    public static WrappedPositionMoveRotation create(Vector position, Vector deltaMovement, float yRot, float xRot) {
        EquivalentConverter<Vector> conv = BukkitConverters.getVectorConverter();
        Object handle = getConstructor().invoke(
                conv.getGeneric(position),
                conv.getGeneric(deltaMovement),
                yRot,
                xRot
        );
        return new WrappedPositionMoveRotation(handle);
    }

    /**
     * Retrieves the absolute position component.
     *
     * @return the position as a Bukkit {@link Vector}.
     */
    public Vector getPosition() {
        return modifier.withType(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter()).read(0);
    }

    /**
     * Sets the absolute position component.
     *
     * @param position - the new position.
     */
    public void setPosition(Vector position) {
        modifier.withType(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter()).write(0, position);
    }

    /**
     * Retrieves the movement delta (velocity) component.
     *
     * @return the delta movement as a Bukkit {@link Vector}.
     */
    public Vector getDeltaMovement() {
        return modifier.withType(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter()).read(1);
    }

    /**
     * Sets the movement delta (velocity) component.
     *
     * @param deltaMovement - the new delta movement.
     */
    public void setDeltaMovement(Vector deltaMovement) {
        modifier.withType(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter()).write(1, deltaMovement);
    }

    /**
     * Retrieves the yaw rotation (y-axis rotation).
     *
     * @return the yaw as a float.
     */
    public float getYRot() {
        StructureModifier<Float> floats = modifier.withType(float.class);
        return floats.read(0);
    }

    /**
     * Sets the yaw rotation (y-axis rotation).
     *
     * @param yRot - the new yaw value.
     */
    public void setYRot(float yRot) {
        modifier.withType(float.class).write(0, yRot);
    }

    /**
     * Retrieves the pitch rotation (x-axis rotation).
     *
     * @return the pitch as a float.
     */
    public float getXRot() {
        StructureModifier<Float> floats = modifier.withType(float.class);
        return floats.read(1);
    }

    /**
     * Sets the pitch rotation (x-axis rotation).
     *
     * @param xRot - the new pitch value.
     */
    public void setXRot(float xRot) {
        modifier.withType(float.class).write(1, xRot);
    }

    /**
     * Returns an {@link EquivalentConverter} that converts between {@code WrappedPositionMoveRotation}
     * and the underlying NMS handle.
     *
     * @return the converter.
     */
    public static EquivalentConverter<WrappedPositionMoveRotation> getConverter() {
        return Converters.handle(AbstractWrapper::getHandle,
                WrappedPositionMoveRotation::fromHandle, WrappedPositionMoveRotation.class);
    }
}
