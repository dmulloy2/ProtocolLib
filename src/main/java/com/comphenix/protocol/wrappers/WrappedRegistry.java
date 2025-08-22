package com.comphenix.protocol.wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

import com.google.common.collect.ImmutableMap;

public class WrappedRegistry {
    // map of NMS class to registry instance
    private static final Map<Class<?>, WrappedRegistry> BY_CLASS;
    private static final Map<String, WrappedRegistry> BY_KEY;

    private static final MethodAccessor GET_BY_KEY;
    private static final MethodAccessor GET_ITEM_ID;
    private static final MethodAccessor GET_ITEM_KEY;
    private static final MethodAccessor GET_REGISTRY_KEY;

    private static final MethodAccessor GET_HOLDER;

    static {
        Class<?> iRegistry = MinecraftReflection.getIRegistry();
        FuzzyReflection fuzzy = FuzzyReflection.fromClass(iRegistry, false);

        GET_BY_KEY = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
            .newBuilder()
            .parameterCount(1)
            .returnTypeMatches(FuzzyMatchers.and(FuzzyMatchers.assignable(Object.class), FuzzyMatchers.except(Optional.class)))
            .requireModifier(Modifier.ABSTRACT)
            .parameterExactType(MinecraftReflection.getMinecraftKeyClass())
            .build()));
        GET_ITEM_ID = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
            .newBuilder()
            .parameterCount(1)
            .returnTypeExact(int.class)
            .requireModifier(Modifier.ABSTRACT)
            .parameterDerivedOf(Object.class)
            .build()));
        GET_ITEM_KEY = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
            .newBuilder()
            .parameterCount(1)
            .returnTypeExact(MinecraftReflection.getMinecraftKeyClass())
            .build()));
        GET_REGISTRY_KEY = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
            .newBuilder()
            .parameterCount(0)
            .returnTypeExact(MinecraftReflection.getResourceKey())
            .build()));

        MethodAccessor getHolder = null;

        if (MinecraftVersion.FEATURE_PREVIEW_UPDATE.atOrAbove()) {
            try {
                getHolder = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
                    .newBuilder()
                    .parameterCount(1)
                    .banModifier(Modifier.STATIC)
                    .returnTypeExact(MinecraftReflection.getHolderClass())
                    .requireModifier(Modifier.PUBLIC)
                    .build()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        GET_HOLDER = getHolder;

        Map<Class<?>, WrappedRegistry> byClass = new HashMap<>();
        Map<String, WrappedRegistry> byKey = new HashMap<>();

        // get the possible registry fields

        Class<?> builtInRegistries = MinecraftReflection.getBuiltInRegistries();
        Set<Field> registries = FuzzyReflection.combineArrays(
                iRegistry == null ? null : iRegistry.getFields(),
                builtInRegistries == null ? null : builtInRegistries.getFields());

        if (iRegistry != null && !registries.isEmpty()) {
            for (Field field : registries) {
                try {
                    // make sure it's actually a registry
                    if (!iRegistry.isAssignableFrom(field.getType())) {
                        continue;
                    }

                    Type genType = field.getGenericType();
                    if (!(genType instanceof ParameterizedType par)) {
                        continue;
                    }

                    Class<?> registryClass = null;

                    Type paramType = par.getActualTypeArguments()[0];
                    if (paramType instanceof Class<?> paramClass) {
                        // for example Registry<Item>
                        registryClass = paramClass;
                    } else if (paramType instanceof ParameterizedType) {
                        // for example Registry<EntityType<?>>
                        par = (ParameterizedType) paramType;
                        paramType = par.getActualTypeArguments()[0];
                        if (paramType instanceof WildcardType wildcard) {
                            // some registry types are even more nested, but we don't want them
                            // for example Registry<Codec<ChunkGenerator>>, Registry<Codec<WorldChunkManager>>
                            if (wildcard.getUpperBounds().length != 1 || wildcard.getLowerBounds().length > 0) {
                                continue;
                            }

                            // we only want types with an undefined upper bound (aka. ?)
                            if (wildcard.getUpperBounds()[0] != Object.class) {
                                continue;
                            }
                        }

                        paramType = par.getRawType();
                        if (paramType instanceof Class<?> paramClass) {
                            // there might be duplicate registries, like the codec registries
                            // we don't want them to be registered here
                            registryClass = paramClass;
                        }
                    }

                    Object handle = field.get(null);
                    ResourceKey key = ResourceKey.fromGeneric(GET_REGISTRY_KEY.invoke(handle));
                    WrappedRegistry wrapped = new WrappedRegistry(handle, key);

                    if (registryClass != null) {
                        byClass.put(registryClass, wrapped);
                    }

                    String registryName = key.getLocation().getFullKey();
                    byKey.put(registryName, wrapped);
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }

        BY_CLASS = ImmutableMap.copyOf(byClass);
        BY_KEY = ImmutableMap.copyOf(byKey);
    }

    private final Object handle;
    private final ResourceKey key;

    private WrappedRegistry(Object handle, ResourceKey key) {
        this.handle = handle;
        this.key = key;
    }

    public Object get(MinecraftKey key) {
        return GET_BY_KEY.invoke(handle, MinecraftKey.getConverter().getGeneric(key));
    }

    public Object get(String key) {
        return get(new MinecraftKey(key));
    }

    public MinecraftKey getKey(Object generic) {
        return MinecraftKey.getConverter().getSpecific(GET_ITEM_KEY.invoke(handle, generic));
    }

    public ResourceKey getKey() {
        return key;
    }

    public int getId(MinecraftKey key) {
        return getId(get(key));
    }

    public int getId(String key) {
        return getId(new MinecraftKey(key));
    }

    public int getId(Object entry) {
        return (int) GET_ITEM_ID.invoke(this.handle, entry);
    }

    public Object getHolder(Object generic) {
        return GET_HOLDER.invoke(handle, generic);
    }

    public static WrappedRegistry getAttributeRegistry() {
        return getRegistry(MinecraftReflection.getAttributeBase());
    }

    public static WrappedRegistry getDimensionRegistry() {
        return getRegistry(MinecraftReflection.getDimensionManager());
    }

    public static WrappedRegistry getSoundRegistry() {
        return getRegistry(MinecraftReflection.getSoundEffectClass());
    }

    public static WrappedRegistry getDataComponentTypeRegistry() {
        return getByKey("minecraft:data_component_type");
    }

    public static WrappedRegistry getRegistry(Class<?> type) {
        return BY_CLASS.get(type);
    }

    public static WrappedRegistry getByKey(String key) {
        return BY_KEY.get(key);
    }
}
