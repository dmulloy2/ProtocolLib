package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.util.Objects;
import java.util.Optional;

/**
 * Wrapper for {@code ChatType.Bound}.
 */
public final class WrappedChatTypeBound {

    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getMinecraftClass("network.chat.ChatType$Bound");
    private static final ConstructorAccessor CONSTRUCTOR = Accessors.getConstructorAccessor(
            HANDLE_TYPE,
            MinecraftReflection.getHolderClass(),
            MinecraftReflection.getIChatBaseComponentClass(),
            Optional.class);
    private static final MethodAccessor GET_CHAT_TYPE = Accessors.getMethodAccessor(HANDLE_TYPE, "chatType");
    private static final MethodAccessor GET_NAME = Accessors.getMethodAccessor(HANDLE_TYPE, "name");
    private static final MethodAccessor GET_TARGET_NAME = Accessors.getMethodAccessor(HANDLE_TYPE, "targetName");

    private static final EquivalentConverter<MinecraftKey> CHAT_TYPE_HOLDER_CONVERTER =
            new EquivalentConverter<>() {
                @Override
                public Object getGeneric(MinecraftKey specific) {
                    return chatTypeHolderConverter().getGeneric(specific);
                }

                @Override
                public MinecraftKey getSpecific(Object generic) {
                    return chatTypeHolderConverter().getSpecific(generic);
                }

                @Override
                public Class<MinecraftKey> getSpecificType() {
                    return MinecraftKey.class;
                }
            };

    public static final EquivalentConverter<WrappedChatTypeBound> CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(WrappedChatTypeBound specific) {
                    return CONSTRUCTOR.invoke(
                            CHAT_TYPE_HOLDER_CONVERTER.getGeneric(specific.chatType),
                            BukkitConverters.getWrappedChatComponentConverter().getGeneric(specific.name),
                            Converters.optional(BukkitConverters.getWrappedChatComponentConverter())
                                    .getGeneric(specific.targetName));
                }

                @Override
                public WrappedChatTypeBound getSpecific(Object generic) {
                    return new WrappedChatTypeBound(
                            CHAT_TYPE_HOLDER_CONVERTER.getSpecific(GET_CHAT_TYPE.invoke(generic)),
                            BukkitConverters.getWrappedChatComponentConverter().getSpecific(GET_NAME.invoke(generic)),
                            Converters.optional(BukkitConverters.getWrappedChatComponentConverter())
                                    .getSpecific(GET_TARGET_NAME.invoke(generic)));
                }

                @Override
                public Class<WrappedChatTypeBound> getSpecificType() {
                    return WrappedChatTypeBound.class;
                }
            });

    private final MinecraftKey chatType;
    private final WrappedChatComponent name;
    private final Optional<WrappedChatComponent> targetName;

    public WrappedChatTypeBound(MinecraftKey chatType, WrappedChatComponent name,
            Optional<WrappedChatComponent> targetName) {
        this.chatType = chatType;
        this.name = name;
        this.targetName = targetName;
    }

    public MinecraftKey getChatType() {
        return chatType;
    }

    public WrappedChatComponent getName() {
        return name;
    }

    public Optional<WrappedChatComponent> getTargetName() {
        return targetName;
    }

    private static EquivalentConverter<MinecraftKey> chatTypeHolderConverter() {
        WrappedRegistry registry = WrappedRegistry.getRegistryByNmsKey("core.registries.Registries", "CHAT_TYPE");
        if (registry == null) {
            throw new IllegalStateException("ChatType registry is not available");
        }
        return Converters.holder(registry.valueConverter(), registry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WrappedChatTypeBound that)) {
            return false;
        }
        return Objects.equals(chatType, that.chatType)
                && Objects.equals(name, that.name)
                && Objects.equals(targetName, that.targetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatType, name, targetName);
    }
}
