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

/**
 * Wrapper around the NMS {@code VecDelta} type, introduced in Minecraft 26.3.
 * <p>
 * Used by {@code REL_ENTITY_MOVE} and {@code REL_ENTITY_MOVE_LOOK} to store the position change. A delta is either
 * linear (a single offset) or stepped (several offsets reached at given ticks). Offsets use the same encoding as the
 * short fields of older versions.
 * <pre>{@code
 * packet.getVecDeltas().write(0, WrappedVecDelta.linear(deltaX, deltaY, deltaZ));
 * }</pre>
 */
public final class WrappedVecDelta extends AbstractWrapper {

    private static Class<?> STEPPED_CLASS;

    private static ConstructorAccessor LINEAR_CONSTRUCTOR;
    private static ConstructorAccessor STEPPED_CONSTRUCTOR;
    private static ConstructorAccessor STEP_CONSTRUCTOR;

    private static StructureModifier<Object> LINEAR_MODIFIER;
    private static StructureModifier<Object> STEPPED_MODIFIER;
    private static StructureModifier<Object> STEP_MODIFIER;

    private static volatile boolean initialized;

    private WrappedVecDelta(Object handle) {
        super(getVecDeltaClass());
        setHandle(handle);
    }

    private static Class<?> getVecDeltaClass() {
        return MinecraftReflection.getVecDeltaClass()
                .orElseThrow(() -> new UnsupportedOperationException("VecDelta requires Minecraft 26.3 or newer"));
    }

    private static void initialize() {
        if (initialized) {
            return;
        }

        Class<?> vecDeltaClass = getVecDeltaClass();
        Class<?> linearClass = null;
        Class<?> stepClass = null;

        for (Class<?> nested : vecDeltaClass.getDeclaredClasses()) {
            if (!vecDeltaClass.isAssignableFrom(nested)) {
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

        Preconditions.checkState(linearClass != null && STEPPED_CLASS != null, "Unable to find VecDelta implementations");

        LINEAR_CONSTRUCTOR = Accessors.getConstructorAccessor(linearClass, short.class, short.class, short.class);
        STEPPED_CONSTRUCTOR = Accessors.getConstructorAccessor(STEPPED_CLASS, List.class);
        STEP_CONSTRUCTOR = Accessors.getConstructorAccessor(stepClass, short.class, short.class, short.class, int.class);

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

    public static WrappedVecDelta fromHandle(Object handle) {
        return new WrappedVecDelta(handle);
    }

    /**
     * Creates a linear delta.
     *
     * @param deltaX - the encoded x offset.
     * @param deltaY - the encoded y offset.
     * @param deltaZ - the encoded z offset.
     * @return the delta.
     */
    public static WrappedVecDelta linear(short deltaX, short deltaY, short deltaZ) {
        initialize();
        return new WrappedVecDelta(LINEAR_CONSTRUCTOR.invoke(deltaX, deltaY, deltaZ));
    }

    /**
     * Creates a stepped delta.
     *
     * @param steps - the steps in order, must not be empty.
     * @return the delta.
     */
    public static WrappedVecDelta stepped(List<Step> steps) {
        Preconditions.checkArgument(steps != null && !steps.isEmpty(), "steps cannot be empty");
        initialize();

        List<Object> handles = new ArrayList<>(steps.size());
        for (Step step : steps) {
            handles.add(STEP_CONSTRUCTOR.invoke(step.getDeltaX(), step.getDeltaY(), step.getDeltaZ(), step.getTicks()));
        }

        return new WrappedVecDelta(STEPPED_CONSTRUCTOR.invoke(Collections.unmodifiableList(handles)));
    }

    public boolean isStepped() {
        initialize();
        return STEPPED_CLASS.isInstance(handle);
    }

    /**
     * @return the encoded x offset of a linear delta.
     * @throws IllegalStateException if the delta is stepped.
     */
    public short getDeltaX() {
        return readLinear(0);
    }

    /**
     * @return the encoded y offset of a linear delta.
     * @throws IllegalStateException if the delta is stepped.
     */
    public short getDeltaY() {
        return readLinear(1);
    }

    /**
     * @return the encoded z offset of a linear delta.
     * @throws IllegalStateException if the delta is stepped.
     */
    public short getDeltaZ() {
        return readLinear(2);
    }

    private short readLinear(int index) {
        Preconditions.checkState(!isStepped(), "Stepped deltas have no single offset");
        return LINEAR_MODIFIER.withTarget(handle).<Short>withType(short.class).read(index);
    }

    /**
     * @return the steps of a stepped delta.
     * @throws IllegalStateException if the delta is linear.
     */
    public List<Step> getSteps() {
        Preconditions.checkState(isStepped(), "Linear deltas have no steps");

        List<?> handles = STEPPED_MODIFIER.withTarget(handle).<List<?>>withType(List.class).read(0);
        List<Step> steps = new ArrayList<>(handles.size());
        for (Object stepHandle : handles) {
            StructureModifier<Object> step = STEP_MODIFIER.withTarget(stepHandle);
            StructureModifier<Short> shorts = step.withType(short.class);
            steps.add(new Step(shorts.read(0), shorts.read(1), shorts.read(2), step.<Integer>withType(int.class).read(0)));
        }

        return Collections.unmodifiableList(steps);
    }

    public static EquivalentConverter<WrappedVecDelta> getConverter() {
        return Converters.ignoreNull(Converters.handle(AbstractWrapper::getHandle,
                WrappedVecDelta::fromHandle, WrappedVecDelta.class));
    }

    public static final class Step {

        private final short deltaX;
        private final short deltaY;
        private final short deltaZ;
        private final int ticks;

        public Step(short deltaX, short deltaY, short deltaZ, int ticks) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.deltaZ = deltaZ;
            this.ticks = ticks;
        }

        public short getDeltaX() {
            return deltaX;
        }

        public short getDeltaY() {
            return deltaY;
        }

        public short getDeltaZ() {
            return deltaZ;
        }

        public int getTicks() {
            return ticks;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Step)) return false;

            Step that = (Step) obj;
            return deltaX == that.deltaX && deltaY == that.deltaY && deltaZ == that.deltaZ && ticks == that.ticks;
        }

        @Override
        public int hashCode() {
            return Objects.hash(deltaX, deltaY, deltaZ, ticks);
        }

        @Override
        public String toString() {
            return "Step[deltaX=" + deltaX + ", deltaY=" + deltaY + ", deltaZ=" + deltaZ + ", ticks=" + ticks + "]";
        }
    }
}
