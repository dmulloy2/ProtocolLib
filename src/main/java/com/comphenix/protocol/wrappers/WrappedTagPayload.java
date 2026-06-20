package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Wrapper for {@code TagNetworkSerialization.NetworkPayload}.
 */
public final class WrappedTagPayload {

    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getMinecraftClass(
            "tags.TagNetworkSerialization$NetworkPayload");
    private static final ConstructorAccessor CONSTRUCTOR = Accessors.getConstructorAccessor(HANDLE_TYPE, Map.class);
    private static final FieldAccessor TAGS = Accessors.getFieldAccessor(HANDLE_TYPE, Map.class, true);
    private static final ConstructorAccessor INT_ARRAY_LIST_CONSTRUCTOR =
            Accessors.getConstructorAccessor(MinecraftReflection.getIntArrayListClass(), Collection.class);
    private static final MethodAccessor CREATE_REGISTRY_KEY = Accessors.getMethodAccessor(
            MinecraftReflection.getResourceKey(),
            "createRegistryKey",
            MinecraftReflection.getMinecraftKeyClass());

    public static final EquivalentConverter<MinecraftKey> REGISTRY_KEY_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(MinecraftKey specific) {
                    return CREATE_REGISTRY_KEY.invoke(null, MinecraftKey.getConverter().getGeneric(specific));
                }

                @Override
                public MinecraftKey getSpecific(Object generic) {
                    return WrappedResourceKey.fromHandle(generic).getLocation();
                }

                @Override
                public Class<MinecraftKey> getSpecificType() {
                    return MinecraftKey.class;
                }
            });

    public static final EquivalentConverter<WrappedTagPayload> CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(WrappedTagPayload specific) {
                    Map<Object, Object> tags = new HashMap<>();
                    for (Map.Entry<MinecraftKey, List<Integer>> entry : specific.tags.entrySet()) {
                        tags.put(
                                MinecraftKey.getConverter().getGeneric(entry.getKey()),
                                INT_ARRAY_LIST_CONSTRUCTOR.invoke(entry.getValue()));
                    }
                    return CONSTRUCTOR.invoke(tags);
                }

                @Override
                public WrappedTagPayload getSpecific(Object generic) {
                    Map<?, ?> genericTags = (Map<?, ?>) TAGS.get(generic);
                    Map<MinecraftKey, List<Integer>> tags = new HashMap<>();
                    for (Map.Entry<?, ?> entry : genericTags.entrySet()) {
                        List<Integer> ids = new ArrayList<>();
                        for (Object id : (Iterable<?>) entry.getValue()) {
                            ids.add((Integer) id);
                        }
                        tags.put(MinecraftKey.getConverter().getSpecific(entry.getKey()), ids);
                    }
                    return new WrappedTagPayload(tags);
                }

                @Override
                public Class<WrappedTagPayload> getSpecificType() {
                    return WrappedTagPayload.class;
                }
            });

    private final Map<MinecraftKey, List<Integer>> tags;

    public WrappedTagPayload(Map<MinecraftKey, List<Integer>> tags) {
        this.tags = copy(tags);
    }

    public Map<MinecraftKey, List<Integer>> getTags() {
        return copy(tags);
    }

    private static Map<MinecraftKey, List<Integer>> copy(Map<MinecraftKey, List<Integer>> tags) {
        Map<MinecraftKey, List<Integer>> copy = new HashMap<>();
        for (Map.Entry<MinecraftKey, List<Integer>> entry : tags.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WrappedTagPayload that)) {
            return false;
        }
        return Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags);
    }

    @Override
    public String toString() {
        return "WrappedTagPayload{tags=" + tags + '}';
    }
}
