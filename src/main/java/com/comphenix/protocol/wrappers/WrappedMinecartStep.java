package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * Wrapper for {@code NewMinecartBehavior.MinecartStep}.
 */
public final class WrappedMinecartStep {

    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getMinecraftClass(
            "world.entity.vehicle.minecart.NewMinecartBehavior$MinecartStep");
    private static final ConstructorAccessor CONSTRUCTOR = Accessors.getConstructorAccessor(
            HANDLE_TYPE,
            MinecraftReflection.getVec3DClass(),
            MinecraftReflection.getVec3DClass(),
            float.class,
            float.class,
            float.class);
    private static final MethodAccessor GET_POSITION = Accessors.getMethodAccessor(HANDLE_TYPE, "position");
    private static final MethodAccessor GET_MOVEMENT = Accessors.getMethodAccessor(HANDLE_TYPE, "movement");
    private static final MethodAccessor GET_Y_ROT = Accessors.getMethodAccessor(HANDLE_TYPE, "yRot");
    private static final MethodAccessor GET_X_ROT = Accessors.getMethodAccessor(HANDLE_TYPE, "xRot");
    private static final MethodAccessor GET_WEIGHT = Accessors.getMethodAccessor(HANDLE_TYPE, "weight");

    public static final EquivalentConverter<WrappedMinecartStep> CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(WrappedMinecartStep specific) {
                    EquivalentConverter<Vector> vectorConverter = BukkitConverters.getVectorConverter();
                    return CONSTRUCTOR.invoke(
                            vectorConverter.getGeneric(specific.position),
                            vectorConverter.getGeneric(specific.movement),
                            specific.yRot,
                            specific.xRot,
                            specific.weight);
                }

                @Override
                public WrappedMinecartStep getSpecific(Object generic) {
                    EquivalentConverter<Vector> vectorConverter = BukkitConverters.getVectorConverter();
                    return new WrappedMinecartStep(
                            vectorConverter.getSpecific(GET_POSITION.invoke(generic)),
                            vectorConverter.getSpecific(GET_MOVEMENT.invoke(generic)),
                            (float) GET_Y_ROT.invoke(generic),
                            (float) GET_X_ROT.invoke(generic),
                            (float) GET_WEIGHT.invoke(generic));
                }

                @Override
                public Class<WrappedMinecartStep> getSpecificType() {
                    return WrappedMinecartStep.class;
                }
            });

    private final Vector position;
    private final Vector movement;
    private final float yRot;
    private final float xRot;
    private final float weight;

    public WrappedMinecartStep(Vector position, Vector movement, float yRot, float xRot, float weight) {
        this.position = position.clone();
        this.movement = movement.clone();
        this.yRot = yRot;
        this.xRot = xRot;
        this.weight = weight;
    }

    public Vector getPosition() {
        return position.clone();
    }

    public Vector getMovement() {
        return movement.clone();
    }

    public float getYRot() {
        return yRot;
    }

    public float getXRot() {
        return xRot;
    }

    public float getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WrappedMinecartStep that)) {
            return false;
        }
        return Float.compare(yRot, that.yRot) == 0
                && Float.compare(xRot, that.xRot) == 0
                && Float.compare(weight, that.weight) == 0
                && Objects.equals(position, that.position)
                && Objects.equals(movement, that.movement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, movement, yRot, xRot, weight);
    }

    @Override
    public String toString() {
        return "WrappedMinecartStep{"
                + "position=" + position
                + ", movement=" + movement
                + ", yRot=" + yRot
                + ", xRot=" + xRot
                + ", weight=" + weight
                + '}';
    }
}
