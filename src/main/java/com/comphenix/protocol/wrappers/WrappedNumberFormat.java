package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper around the NumberFormat NMS classes.
 *
 * @author vytskalt
 * @since 1.20.4
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class WrappedNumberFormat extends AbstractWrapper {
    private static final Object BLANK;
    private static final ConstructorAccessor FIXED_CONSTRUCTOR, STYLED_CONSTRUCTOR;

    static {
        if (!isSupported()) {
            BLANK = null;
            FIXED_CONSTRUCTOR = null;
            STYLED_CONSTRUCTOR = null;
        } else {
            Class<?> blankClass = MinecraftReflection.getBlankFormatClass().get();
            FuzzyReflection fuzzyBlank = FuzzyReflection.fromClass(blankClass, true);
            BLANK = Accessors.getFieldAccessor(fuzzyBlank.getFieldByType("INSTANCE", blankClass)).get(null);

            FIXED_CONSTRUCTOR = Accessors.getConstructorAccessor(
                    MinecraftReflection.getFixedFormatClass().get(),
                    MinecraftReflection.getIChatBaseComponentClass()
            );

            STYLED_CONSTRUCTOR = Accessors.getConstructorAccessor(
                    MinecraftReflection.getStyledFormatClass().get(),
                    MinecraftReflection.getComponentStyleClass()
            );
        }
    }

    /**
     * @return Whether the NumberFormat classes exist on the current server version
     */
    public static boolean isSupported() {
        return MinecraftReflection.getNumberFormatClass().isPresent();
    }

    public static WrappedNumberFormat fromHandle(Object handle) {
        throwIfUnsupported();
        if (MinecraftReflection.getBlankFormatClass().get().isInstance(handle)) {
            return new Blank(handle);
        } else if (MinecraftReflection.getFixedFormatClass().get().isInstance(handle)) {
            return new Fixed(handle);
        } else if (MinecraftReflection.getStyledFormatClass().get().isInstance(handle)) {
            return new Styled(handle);
        } else {
            throw new IllegalArgumentException("handle is not a NumberFormat instance, but " + handle.getClass());
        }
    }

    public static Blank blank() {
        throwIfUnsupported();
        return new Blank(WrappedNumberFormat.BLANK);
    }

    public static Fixed fixed(@NotNull WrappedChatComponent content) {
        throwIfUnsupported();
        Object handle = FIXED_CONSTRUCTOR.invoke(content.getHandle());
        return new Fixed(handle);
    }

    public static Styled styled(@NotNull WrappedComponentStyle style) {
        throwIfUnsupported();
        Object handle = STYLED_CONSTRUCTOR.invoke(style.getHandle());
        return new Styled(handle);
    }

    private static void throwIfUnsupported() {
        if (!isSupported()) {
            throw new IllegalStateException("NumberFormat classes don't exist on this server version");
        }
    }

    private WrappedNumberFormat(Class<?> handleType) {
        super(handleType);
    }

    public static class Blank extends WrappedNumberFormat {
        private Blank(Object handle) {
            super(MinecraftReflection.getBlankFormatClass().get());
            setHandle(handle);
        }
    }

    public static class Fixed extends WrappedNumberFormat {
        private final StructureModifier<Object> modifier;

        private Fixed(Object handle) {
            super(MinecraftReflection.getFixedFormatClass().get());
            setHandle(handle);
            this.modifier = new StructureModifier<>(handle.getClass()).withTarget(handle);
        }

        public WrappedChatComponent getContent() {
            Object handle = modifier.withType(MinecraftReflection.getIChatBaseComponentClass()).read(0);
            return WrappedChatComponent.fromHandle(handle);
        }
    }

    public static class Styled extends WrappedNumberFormat {
        private final StructureModifier<Object> modifier;

        private Styled(Object handle) {
            super(MinecraftReflection.getStyledFormatClass().get());
            setHandle(handle);
            this.modifier = new StructureModifier<>(handle.getClass()).withTarget(handle);
        }

        public WrappedComponentStyle getStyle() {
            Object handle = modifier.withType(MinecraftReflection.getComponentStyleClass()).read(0);
            return new WrappedComponentStyle(handle);
        }
    }
}
