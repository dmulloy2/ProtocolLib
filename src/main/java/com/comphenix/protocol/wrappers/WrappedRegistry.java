package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftRegistryAccess;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WrappedRegistry {
    // map of NMS class to registry instance (static/built-in registries)
    private static final Map<Class<?>, WrappedRegistry> REGISTRY;

    // map of NMS class to ResourceKey for dynamic (datapack) registries
    private static final Map<Class<?>, Object> DYNAMIC_REGISTRY_KEYS;

    // cache of dynamically resolved registries
    private static final Map<Class<?>, WrappedRegistry> DYNAMIC_REGISTRY_CACHE = new ConcurrentHashMap<>();

    // RegistryAccess.lookup(ResourceKey) -> Optional<Registry>
    private static final MethodAccessor REGISTRY_ACCESS_LOOKUP;

    private static final MethodAccessor GET;
    private static final MethodAccessor GET_ID;
    private static final MethodAccessor GET_KEY;

    private static final MethodAccessor GET_HOLDER;

    static {
        Map<Class<?>, WrappedRegistry> regMap = new HashMap<>();

        // get the possible registry fields
        Class<?> iRegistry = MinecraftReflection.getIRegistry();
        Class<?> builtInRegistries = MinecraftReflection.getBuiltInRegistries();
        Set<Field> registries = FuzzyReflection.combineArrays(
                iRegistry == null ? null : iRegistry.getFields(),
                builtInRegistries == null ? null : builtInRegistries.getFields());

        if (iRegistry != null && !registries.isEmpty()) {
            for (Field field : registries) {
                try {
                    // make sure it's actually a registry
                    if (iRegistry.isAssignableFrom(field.getType())) {
                        Type genType = field.getGenericType();
                        if (genType instanceof ParameterizedType) {
                            ParameterizedType par = (ParameterizedType) genType;
                            Type paramType = par.getActualTypeArguments()[0];
                            if (paramType instanceof Class) {
                                // for example Registry<Item>
                                regMap.put((Class<?>) paramType, new WrappedRegistry(field.get(null)));
                            } else if (paramType instanceof ParameterizedType) {
                                // for example Registry<EntityType<?>>
                                par = (ParameterizedType) paramType;
                                paramType = par.getActualTypeArguments()[0];
                                if (paramType instanceof WildcardType) {
                                    // some registry types are even more nested, but we don't want them
                                    // for example Registry<Codec<ChunkGenerator>>, Registry<Codec<WorldChunkManager>>
                                    WildcardType wildcard = (WildcardType) paramType;
                                    if (wildcard.getUpperBounds().length != 1 || wildcard.getLowerBounds().length > 0) {
                                        continue;
                                    }

                                    // we only want types with an undefined upper bound (aka. ?)
                                    if (wildcard.getUpperBounds()[0] != Object.class) {
                                        continue;
                                    }
                                }

                                paramType = par.getRawType();
                                if (paramType instanceof Class<?>) {
                                    // there might be duplicate registries, like the codec registries
                                    // we don't want them to be registered here
                                    regMap.put((Class<?>) paramType, new WrappedRegistry(field.get(null)));
                                }
                            }
                        }
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }

        REGISTRY = ImmutableMap.copyOf(regMap);

        // Build dynamic registry key map from Registries class (ResourceKey<Registry<T>> fields)
        // These cover datapack registries like DamageType that are not in BuiltInRegistries
        Map<Class<?>, Object> dynKeyMap = new HashMap<>();
        try {
            Class<?> registriesClass = MinecraftReflection.getNullableNMS("core.registries.Registries");
            if (registriesClass != null) {
                for (Field field : registriesClass.getFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) continue;
                    // looking for ResourceKey<Registry<T>> fields
                    Type genType = field.getGenericType();
                    if (!(genType instanceof ParameterizedType)) continue;
                    ParameterizedType outer = (ParameterizedType) genType;
                    // outer raw type = ResourceKey
                    if (outer.getActualTypeArguments().length != 1) continue;
                    Type innerType = outer.getActualTypeArguments()[0];
                    if (!(innerType instanceof ParameterizedType)) continue;
                    ParameterizedType registryType = (ParameterizedType) innerType;
                    // registryType raw type should be Registry/IRegistry
                    if (!(registryType.getRawType() instanceof Class<?>)) continue;
                    Class<?> rawRegistry = (Class<?>) registryType.getRawType();
                    if (iRegistry != null && !iRegistry.isAssignableFrom(rawRegistry)) continue;
                    if (registryType.getActualTypeArguments().length != 1) continue;
                    Type entryType = registryType.getActualTypeArguments()[0];
                    Class<?> entryClass = null;
                    if (entryType instanceof Class<?>) {
                        entryClass = (Class<?>) entryType;
                    } else if (entryType instanceof ParameterizedType) {
                        Type raw = ((ParameterizedType) entryType).getRawType();
                        if (raw instanceof Class<?>) entryClass = (Class<?>) raw;
                    }
                    if (entryClass != null && !regMap.containsKey(entryClass)) {
                        try {
                            dynKeyMap.put(entryClass, field.get(null));
                        } catch (ReflectiveOperationException ignored) {
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        DYNAMIC_REGISTRY_KEYS = ImmutableMap.copyOf(dynKeyMap);

        // Set up RegistryAccess.lookup(ResourceKey) -> Optional<Registry>
        MethodAccessor registryAccessLookup = null;
        try {
            Class<?> registryAccessClass = MinecraftReflection.getRegistryAccessClass();
            if (registryAccessClass != null) {
                registryAccessLookup = Accessors.getMethodAccessor(
                        FuzzyReflection.fromClass(registryAccessClass, false).getMethod(
                                FuzzyMethodContract.newBuilder()
                                        .parameterCount(1)
                                        .returnTypeExact(Optional.class)
                                        .build()
                        )
                );
            }
        } catch (Exception ignored) {
        }
        REGISTRY_ACCESS_LOOKUP = registryAccessLookup;

        FuzzyReflection fuzzy = FuzzyReflection.fromClass(iRegistry, false);
        GET = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
                .newBuilder()
                .parameterCount(1)
                .returnTypeMatches(FuzzyMatchers.and(FuzzyMatchers.assignable(Object.class), FuzzyMatchers.except(Optional.class)))
                .requireModifier(Modifier.ABSTRACT)
                .parameterExactType(MinecraftReflection.getMinecraftKeyClass())
                .build()));
        GET_ID = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
                .newBuilder()
                .parameterCount(1)
                .returnTypeExact(int.class)
                .requireModifier(Modifier.ABSTRACT)
                .parameterDerivedOf(Object.class)
                .build()));
        GET_KEY = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
                .newBuilder()
                .parameterCount(1)
                .returnTypeExact(MinecraftReflection.getMinecraftKeyClass())
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
    }

    private final Object handle;

    /** The NMS {@code ResourceKey<Registry<T>>} for this registry, if known. */
    @Nullable
    private final Object registryKey;

    private WrappedRegistry(Object handle) {
        this(handle, null);
    }

    private WrappedRegistry(Object handle, @Nullable Object registryKey) {
        this.handle = handle;
        this.registryKey = registryKey;
    }

    public Object get(MinecraftKey key) {
        return GET.invoke(handle, MinecraftKey.getConverter().getGeneric(key));
    }

    public Object get(String key) {
        return get(new MinecraftKey(key));
    }

    public MinecraftKey getKey(Object generic) {
        return MinecraftKey.getConverter().getSpecific(GET_KEY.invoke(handle, generic));
    }

    public int getId(MinecraftKey key) {
        return getId(get(key));
    }

    public int getId(String key) {
        return getId(new MinecraftKey(key));
    }

    public int getId(Object entry) {
        return (int) GET_ID.invoke(this.handle, entry);
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

    public static WrappedRegistry getRegistry(Class<?> type) {
        WrappedRegistry registry = REGISTRY.get(type);
        if (registry != null) {
            return registry;
        }

        // Fall back to dynamic (datapack) registry lookup via RegistryAccess
        return DYNAMIC_REGISTRY_CACHE.computeIfAbsent(type, t -> {
            Object resourceKey = DYNAMIC_REGISTRY_KEYS.get(t);
            if (resourceKey == null || REGISTRY_ACCESS_LOOKUP == null) {
                return null;
            }

            Object registryAccess = MinecraftRegistryAccess.get();
            if (registryAccess == null) {
                return null;
            }

            Optional<?> optRegistry = (Optional<?>) REGISTRY_ACCESS_LOOKUP.invoke(registryAccess, resourceKey);
            if (optRegistry == null || !optRegistry.isPresent()) {
                return null;
            }

            return new WrappedRegistry(optRegistry.get(), resourceKey);
        });
    }

    // -------------------------------------------------------------------------
    // ResourceKey helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the NMS {@code ResourceKey<Registry<T>>} that identifies this registry,
     * or {@code null} if it is not known (e.g. for built-in registries resolved only
     * by type scanning).
     */
    @Nullable
    public Object getRegistryKey() {
        return registryKey;
    }

    /**
     * Converts a {@link MinecraftKey} entry location to the corresponding NMS
     * {@code ResourceKey<T>} scoped to this registry.
     *
     * @param key the entry identifier (e.g. {@code minecraft:diamond})
     * @return the NMS {@code ResourceKey<T>} handle
     * @throws IllegalStateException if this registry's own key is not known
     */
    public Object toResourceKey(MinecraftKey key) {
        if (registryKey == null) {
            throw new IllegalStateException("Registry key is not known for this WrappedRegistry instance");
        }
        return WrappedResourceKey.of(registryKey, key).getHandle();
    }

    /**
     * Converts a NMS {@code ResourceKey<T>} back to the {@link MinecraftKey}
     * representing the entry location.
     *
     * @param nmsResourceKey the NMS {@code ResourceKey} object
     * @return the entry identifier
     */
    public static MinecraftKey fromResourceKey(Object nmsResourceKey) {
        return WrappedResourceKey.fromHandle(nmsResourceKey).getLocation();
    }

    /**
     * Returns an {@link EquivalentConverter} between
     * {@link MinecraftKey} and NMS {@code ResourceKey<T>} for this registry.
     *
     * @throws IllegalStateException if this registry's own key is not known
     */
    public EquivalentConverter<MinecraftKey> resourceKeyConverter() {
        if (registryKey == null) {
            throw new IllegalStateException("Registry key is not known for this WrappedRegistry instance");
        }
        return WrappedResourceKey.converterFor(registryKey);
    }

    /**
     * Returns an {@link EquivalentConverter} between
     * {@link MinecraftKey} and the NMS registry value type {@code T} (e.g. {@code MenuType<?>},
     * {@code SoundEvent}). The converter resolves both directions through this registry's
     * {@code get(ResourceLocation)} / {@code getKey(value)} methods.
     *
     * <p>Use this for fields whose declared NMS type is the registry value itself, as opposed
     * to {@code ResourceKey<T>} (use {@link #resourceKeyConverter()}) or {@code Holder<T>}
     * (use {@link Converters#holder}).
     */
    public EquivalentConverter<MinecraftKey> valueConverter() {
        final WrappedRegistry self = this;
        return new EquivalentConverter<>() {
            @Override
            public Object getGeneric(MinecraftKey specific) {
                return specific == null ? null : self.get(specific);
            }

            @Override
            public MinecraftKey getSpecific(Object generic) {
                return generic == null ? null : self.getKey(generic);
            }

            @Override
            public Class<MinecraftKey> getSpecificType() {
                return MinecraftKey.class;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Named registry accessors
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link WrappedRegistry} backed by the NMS {@code GameRule} registry
     * ({@code Registries.GAME_RULE}).  The returned instance supports
     * {@link #toResourceKey(MinecraftKey)} and {@link #resourceKeyConverter()}.
     *
     * @return the game-rule registry, or {@code null} if unavailable at this time
     */
    @Nullable
    public static WrappedRegistry getGameRuleRegistry() {
        return getRegistryByNmsKey("core.registries.Registries", "GAME_RULE");
    }

    /**
     * Looks up a registry by the name of a static {@code ResourceKey<Registry<T>>}
     * field on the NMS {@code Registries} class.
     *
     * @param registriesClassName the NMS class name (e.g. {@code "core.registries.Registries"})
     * @param fieldName           the field name on that class (e.g. {@code "GAME_RULE"})
     * @return a {@link WrappedRegistry} with both the handle and the registry key set,
     *         or {@code null} if the lookup fails
     */
    @Nullable
    public static WrappedRegistry getRegistryByNmsKey(String registriesClassName, String fieldName) {
        try {
            Class<?> registriesClass = MinecraftReflection.getMinecraftClass(registriesClassName);
            Object regKey = registriesClass.getField(fieldName).get(null);

            // Try the dynamic cache first (covers datapack registries and game_rule)
            if (REGISTRY_ACCESS_LOOKUP != null) {
                Object registryAccess = MinecraftRegistryAccess.get();
                if (registryAccess != null) {
                    Optional<?> opt = (Optional<?>) REGISTRY_ACCESS_LOOKUP.invoke(registryAccess, regKey);
                    if (opt != null && opt.isPresent()) {
                        return new WrappedRegistry(opt.get(), regKey);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
