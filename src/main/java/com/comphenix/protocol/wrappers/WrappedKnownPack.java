package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.util.Objects;

/**
 * Wrapper for {@code net.minecraft.server.packs.repository.KnownPack}.
 */
public final class WrappedKnownPack {

    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getMinecraftClass("server.packs.repository.KnownPack");
    private static final ConstructorAccessor CONSTRUCTOR = Accessors.getConstructorAccessor(
            HANDLE_TYPE,
            String.class,
            String.class,
            String.class);
    private static final MethodAccessor GET_NAMESPACE = Accessors.getMethodAccessor(HANDLE_TYPE, "namespace");
    private static final MethodAccessor GET_ID = Accessors.getMethodAccessor(HANDLE_TYPE, "id");
    private static final MethodAccessor GET_VERSION = Accessors.getMethodAccessor(HANDLE_TYPE, "version");

    public static final EquivalentConverter<WrappedKnownPack> CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(WrappedKnownPack specific) {
                    return CONSTRUCTOR.invoke(specific.namespace, specific.id, specific.version);
                }

                @Override
                public WrappedKnownPack getSpecific(Object generic) {
                    return new WrappedKnownPack(
                            (String) GET_NAMESPACE.invoke(generic),
                            (String) GET_ID.invoke(generic),
                            (String) GET_VERSION.invoke(generic));
                }

                @Override
                public Class<WrappedKnownPack> getSpecificType() {
                    return WrappedKnownPack.class;
                }
            });

    private final String namespace;
    private final String id;
    private final String version;

    public WrappedKnownPack(String namespace, String id, String version) {
        this.namespace = namespace;
        this.id = id;
        this.version = version;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WrappedKnownPack that)) {
            return false;
        }
        return Objects.equals(namespace, that.namespace)
                && Objects.equals(id, that.id)
                && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, id, version);
    }

    @Override
    public String toString() {
        return "WrappedKnownPack{"
                + "namespace='" + namespace + '\''
                + ", id='" + id + '\''
                + ", version='" + version + '\''
                + '}';
    }
}
