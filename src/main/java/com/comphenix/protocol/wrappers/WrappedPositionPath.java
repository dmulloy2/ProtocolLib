package com.comphenix.protocol.wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import com.google.common.base.Preconditions;
import org.bukkit.util.Vector;

/**
 * Wrapper around the NMS {@code PositionPath} type, introduced in Minecraft 26.3.
 * <p>
 * Used by {@code ENTITY_POSITION_SYNC} to store the entity position. A path is either linear (a single position) or
 * stepped (several positions reached at given ticks).
 * <pre>{@code
 * packet.getPositionPaths().write(0, WrappedPositionPath.linear(new Vector(x, y, z)));
 * }</pre>
 */
public final class WrappedPositionPath extends AbstractWrapper {

    private static Class<?> STEPPED_CLASS;

    private static ConstructorAccessor LINEAR_CONSTRUCTOR;
    private static ConstructorAccessor STEPPED_CONSTRUCTOR;
    private static ConstructorAccessor STEP_CONSTRUCTOR;

    private static StructureModifier<Object> LINEAR_MODIFIER;
    private static StructureModifier<Object> STEPPED_MODIFIER;
    private static StructureModifier<Object> STEP_MODIFIER;

    private static volatile boolean initialized;

    private WrappedPositionPath(Object handle) {
        super(getPositionPathClass());
        setHandle(handle);
    }

    private static Class<?> getPositionPathClass() {
        return MinecraftReflection.getPositionPathClass()
                .orElseThrow(() -> new UnsupportedOperationException("PositionPath requires Minecraft 26.3 or newer"));
    }

    private static void initialize() {
        if (initialized) {
            return;
        }

        Class<?> pathClass = getPositionPathClass();
        Class<?> vec3Class = MinecraftReflection.getVec3DClass();
        Class<?> linearClass = null;
        Class<?> stepClass = null;

        for (Class<?> nested : pathClass.getDeclaredClasses()) {
            if (!pathClass.isAssignableFrom(nested)) {
                continue;
            }

            Field steps = getListField(nested);
            if (steps != null) {
                STEPPED_CLASS = nested;
                stepClass = (Class<?>) ((ParameterizedType) steps.getGenericType()).getActualTypeArguments()[0];
            } else {
                linearClass = nested;
            }
        }

        Preconditions.checkState(linearClass != null && STEPPED_CLASS != null, "Unable to find PositionPath implementations");

        LINEAR_CONSTRUCTOR = Accessors.getConstructorAccessor(linearClass, vec3Class);
        STEPPED_CONSTRUCTOR = Accessors.getConstructorAccessor(STEPPED_CLASS, List.class);
        STEP_CONSTRUCTOR = Accessors.getConstructorAccessor(stepClass, vec3Class, int.class);

        LINEAR_MODIFIER = new StructureModifier<>(linearClass);
        STEPPED_MODIFIER = new StructureModifier<>(STEPPED_CLASS);
        STEP_MODIFIER = new StructureModifier<>(stepClass);
        initialized = true;
    }

    private static Field getListField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (List.class.equals(field.getType())) {
                return field;
            }
        }
        return null;
    }

    public static WrappedPositionPath fromHandle(Object handle) {
        return new WrappedPositionPath(handle);
    }

    /**
     * Creates a linear path.
     *
     * @param position - the absolute position.
     * @return the path.
     */
    public static WrappedPositionPath linear(Vector position) {
        Preconditions.checkNotNull(position, "position cannot be NULL");
        initialize();
        return new WrappedPositionPath(LINEAR_CONSTRUCTOR.invoke(BukkitConverters.getVectorConverter().getGeneric(position)));
    }

    /**
     * Creates a stepped path ending at the position of the last step.
     *
     * @param steps - the steps in order, must not be empty.
     * @return the path.
     */
    public static WrappedPositionPath stepped(List<Step> steps) {
        Preconditions.checkArgument(steps != null && !steps.isEmpty(), "steps cannot be empty");
        initialize();

        EquivalentConverter<Vector> vectorConverter = BukkitConverters.getVectorConverter();
        List<Object> handles = new ArrayList<>(steps.size());
        for (Step step : steps) {
            handles.add(STEP_CONSTRUCTOR.invoke(vectorConverter.getGeneric(step.getPosition()), step.getTickOffset()));
        }

        return new WrappedPositionPath(STEPPED_CONSTRUCTOR.invoke(Collections.unmodifiableList(handles)));
    }

    public boolean isStepped() {
        initialize();
        return STEPPED_CLASS.isInstance(handle);
    }

    /**
     * @return the final position of the path.
     */
    public Vector getEndPosition() {
        StructureModifier<Object> modifier = isStepped() ? STEPPED_MODIFIER : LINEAR_MODIFIER;
        return modifier.withTarget(handle)
                .withType(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter())
                .read(0);
    }

    /**
     * @return the steps of a stepped path.
     * @throws IllegalStateException if the path is linear.
     */
    public List<Step> getSteps() {
        Preconditions.checkState(isStepped(), "Linear paths have no steps");

        List<?> handles = STEPPED_MODIFIER.withTarget(handle).<List<?>>withType(List.class).read(0);
        List<Step> steps = new ArrayList<>(handles.size());
        for (Object stepHandle : handles) {
            StructureModifier<Object> step = STEP_MODIFIER.withTarget(stepHandle);
            Vector position = step.withType(MinecraftReflection.getVec3DClass(), BukkitConverters.getVectorConverter()).read(0);
            steps.add(new Step(position, step.<Integer>withType(int.class).read(0)));
        }

        return Collections.unmodifiableList(steps);
    }

    public static EquivalentConverter<WrappedPositionPath> getConverter() {
        return Converters.ignoreNull(Converters.handle(AbstractWrapper::getHandle,
                WrappedPositionPath::fromHandle, WrappedPositionPath.class));
    }

    public static final class Step {

        private final Vector position;
        private final int tickOffset;

        public Step(Vector position, int tickOffset) {
            this.position = Preconditions.checkNotNull(position, "position cannot be NULL").clone();
            this.tickOffset = tickOffset;
        }

        public Vector getPosition() {
            return position.clone();
        }

        public int getTickOffset() {
            return tickOffset;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Step)) return false;

            Step that = (Step) obj;
            return tickOffset == that.tickOffset && position.equals(that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(position, tickOffset);
        }

        @Override
        public String toString() {
            return "Step[position=" + position + ", tickOffset=" + tickOffset + "]";
        }
    }
}
