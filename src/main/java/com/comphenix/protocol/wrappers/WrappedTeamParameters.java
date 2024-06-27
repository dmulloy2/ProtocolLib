package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper around the team parameters NMS class.
 *
 * @author vytskalt
 * @since 1.17
 */
public class WrappedTeamParameters extends AbstractWrapper {
    public static Class<?> getNmsClassOrThrow() {
        return MinecraftReflection.getTeamParametersClass()
                .orElseThrow(() -> new IllegalStateException("Team parameters class doesn't exist on this server version"));
    }

    /**
     * @return Whether the team parameters class exists on the current server version
     */
    public static boolean isSupported() {
        return MinecraftReflection.getTeamParametersClass().isPresent();
    }

    public static Builder newBuilder() {
        return newBuilder(null);
    }

    public static Builder newBuilder(@Nullable WrappedTeamParameters template) {
        return new Builder(template);
    }

    private final StructureModifier<Object> modifier;

    public WrappedTeamParameters(Object handle) {
        super(getNmsClassOrThrow());
        setHandle(handle);
        this.modifier = new StructureModifier<>(getNmsClassOrThrow()).withTarget(handle);
    }

    @NotNull
    public WrappedChatComponent getDisplayName() {
        return readComponent(0);
    }

    @NotNull
    public WrappedChatComponent getPrefix() {
        return readComponent(1);
    }

    @NotNull
    public WrappedChatComponent getSuffix() {
        return readComponent(2);
    }

    @NotNull
    public String getNametagVisibility() {
        return modifier.<String>withType(String.class).read(0);
    }

    @NotNull
    public String getCollisionRule() {
        return modifier.<String>withType(String.class).read(1);
    }

    @NotNull
    public EnumWrappers.ChatFormatting getColor() {
        Object handle = modifier.withType(EnumWrappers.getChatFormattingClass()).read(0);
        return EnumWrappers.getChatFormattingConverter().getSpecific(handle);
    }

    public int getOptions() {
        return (int) modifier.withType(int.class).read(0);
    }

    private WrappedChatComponent readComponent(int index) {
        Object handle = modifier.withType(MinecraftReflection.getIChatBaseComponentClass()).read(index);
        return WrappedChatComponent.fromHandle(handle);
    }

    private void writeComponent(int index, WrappedChatComponent component) {
        modifier.withType(MinecraftReflection.getIChatBaseComponentClass()).write(index, component.getHandle());
    }

    public static class Builder {
        private WrappedChatComponent displayName, prefix, suffix;
        private String nametagVisibility, collisionRule;
        private EnumWrappers.ChatFormatting color;
        private int options;

        private Builder(@Nullable WrappedTeamParameters template) {
            if (template != null) {
                this.displayName = template.getDisplayName();
                this.prefix = template.getPrefix();
                this.suffix = template.getSuffix();
                this.nametagVisibility = template.getNametagVisibility();
                this.collisionRule = template.getCollisionRule();
                this.color = template.getColor();
                this.options = template.getOptions();
            }
        }

        public Builder displayName(@NotNull WrappedChatComponent displayName) {
            Preconditions.checkNotNull(displayName);
            this.displayName = displayName;
            return this;
        }

        public Builder prefix(@NotNull WrappedChatComponent prefix) {
            Preconditions.checkNotNull(prefix);
            this.prefix = prefix;
            return this;
        }

        public Builder suffix(@NotNull WrappedChatComponent suffix) {
            Preconditions.checkNotNull(suffix);
            this.suffix = suffix;
            return this;
        }

        public Builder nametagVisibility(@NotNull String nametagVisibility) {
            Preconditions.checkNotNull(nametagVisibility);
            this.nametagVisibility = nametagVisibility;
            return this;
        }

        public Builder collisionRule(@NotNull String collisionRule) {
            Preconditions.checkNotNull(collisionRule);
            this.collisionRule = collisionRule;
            return this;
        }

        public Builder color(@NotNull EnumWrappers.ChatFormatting color) {
            Preconditions.checkNotNull(color);
            this.color = color;
            return this;
        }

        public Builder options(int options) {
            Preconditions.checkNotNull(collisionRule);
            this.options = options;
            return this;
        }

        public WrappedTeamParameters build() {
            Preconditions.checkNotNull(displayName, "Display name not set");
            Preconditions.checkNotNull(prefix, "Prefix not set");
            Preconditions.checkNotNull(suffix, "Suffix not set");
            Preconditions.checkNotNull(nametagVisibility, "Nametag visibility not set");
            Preconditions.checkNotNull(collisionRule, "Collision rule not set");
            Preconditions.checkNotNull(color, "Color not set");

            Object handle = StructureCache.newInstance(getNmsClassOrThrow());

            WrappedTeamParameters wrapped = new WrappedTeamParameters(handle);
            wrapped.writeComponent(0, displayName);
            wrapped.writeComponent(1, prefix);
            wrapped.writeComponent(2, suffix);
            wrapped.modifier.withType(String.class).write(0, nametagVisibility);
            wrapped.modifier.withType(String.class).write(1, collisionRule);
            wrapped.modifier.withType(EnumWrappers.getChatFormattingClass()).write(0, EnumWrappers.getChatFormattingConverter().getGeneric(color));
            wrapped.modifier.withType(int.class).write(0, options);
            return wrapped;
        }
    }
}
