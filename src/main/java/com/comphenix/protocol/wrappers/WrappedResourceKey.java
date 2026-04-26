package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Wrapper around the NMS {@code ResourceKey<T>} class.
 *
 * <p>A {@code ResourceKey} pairs a registry identifier (e.g. {@code minecraft:item})
 * with an entry location (e.g. {@code minecraft:diamond}) to form a fully-qualified
 * reference into a Minecraft registry.
 *
 * <p>All reflective lookups are done with {@link FuzzyReflection} so they are
 * independent of Mojang vs. Yarn/intermediary method names.
 */
public final class WrappedResourceKey {

    // ResourceKey#location() / ResourceKey#identifier() → ResourceLocation
    private static final MethodAccessor GET_LOCATION;

    // static ResourceKey#create(ResourceKey<Registry<T>>, ResourceLocation) → ResourceKey<T>
    private static final MethodAccessor CREATE;

    static {
        Class<?> resourceKeyClass    = MinecraftReflection.getResourceKey();
        Class<?> resourceLocClass    = MinecraftReflection.getMinecraftKeyClass();
        FuzzyReflection fuzzy        = FuzzyReflection.fromClass(resourceKeyClass, false);

        // Resolve location() / identifier() by name first (mapping-specific names are known),
        // then fall back to fuzzy if neither exists.  We must NOT pick registry() by accident —
        // that also returns ResourceLocation but is the registry namespace, not the entry path.
        MethodAccessor getLocation = null;
        for (String candidate : new String[]{"location", "identifier", "getLocation"}) {
            try {
                Method m = resourceKeyClass.getMethod(candidate);
                if (m.getReturnType().equals(resourceLocClass)) {
                    getLocation = Accessors.getMethodAccessor(m);
                    break;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        if (getLocation == null) {
            // Last-resort fuzzy: pick the method whose simple name contains "location" or "identifier"
            getLocation = Accessors.getMethodAccessor(
                    fuzzy.getMethodList(FuzzyMethodContract.newBuilder()
                            .parameterCount(0)
                            .banModifier(Modifier.STATIC)
                            .returnTypeExact(resourceLocClass)
                            .build())
                    .stream()
                    .filter(m -> {
                        String n = m.getName().toLowerCase();
                        return n.contains("location") || n.contains("identifier");
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Cannot find ResourceKey#location() method"))
            );
        }
        GET_LOCATION = getLocation;

        // Find the static two-arg factory: create(ResourceKey<Registry<T>>, ResourceLocation)
        CREATE = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
                .parameterCount(2)
                .requireModifier(Modifier.STATIC)
                .parameterExactArray(resourceKeyClass, resourceLocClass)
                .returnTypeExact(resourceKeyClass)
                .build()));
    }

    private final Object handle;

    private WrappedResourceKey(Object handle) {
        this.handle = handle;
    }

    // -------------------------------------------------------------------------
    // Factory / unwrap
    // -------------------------------------------------------------------------

    /**
     * Wraps an existing NMS {@code ResourceKey} handle.
     *
     * @param handle the NMS object; must be an instance of {@code ResourceKey}
     * @return a new wrapper
     */
    public static WrappedResourceKey fromHandle(Object handle) {
        if (handle == null) throw new NullPointerException("handle");
        return new WrappedResourceKey(handle);
    }

    /**
     * Creates a new {@code ResourceKey<T>} pointing into the given registry.
     *
     * @param registryKey the NMS {@code ResourceKey<Registry<T>>} that identifies the registry
     *                    (e.g. the value of {@code Registries.ITEM})
     * @param location    the entry identifier within that registry
     * @return a wrapper around the newly created {@code ResourceKey<T>}
     */
    public static WrappedResourceKey of(Object registryKey, MinecraftKey location) {
        Object resourceLocation = MinecraftKey.getConverter().getGeneric(location);
        return new WrappedResourceKey(CREATE.invoke(null, registryKey, resourceLocation));
    }

    /**
     * Returns the raw NMS {@code ResourceKey} handle.
     */
    public Object getHandle() {
        return handle;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the entry location part of this key (e.g. {@code minecraft:diamond}).
     * This is the {@code ResourceLocation} / {@code Identifier} component, NOT the
     * registry identifier.
     */
    public MinecraftKey getLocation() {
        Object resourceLocation = GET_LOCATION.invoke(handle);
        return MinecraftKey.getConverter().getSpecific(resourceLocation);
    }

    // -------------------------------------------------------------------------
    // EquivalentConverter factory
    // -------------------------------------------------------------------------

    /**
     * Returns an {@link EquivalentConverter} that converts
     * between {@link MinecraftKey} (the entry location) and a NMS
     * {@code ResourceKey<T>} belonging to the supplied registry key.
     *
     * @param registryKey the NMS {@code ResourceKey<Registry<T>>} (e.g. {@code Registries.ITEM})
     */
    public static EquivalentConverter<MinecraftKey> converterFor(Object registryKey) {
        return new EquivalentConverter<MinecraftKey>() {
            @Override
            public MinecraftKey getSpecific(Object generic) {
                return WrappedResourceKey.fromHandle(generic).getLocation();
            }

            @Override
            public Object getGeneric(MinecraftKey specific) {
                return WrappedResourceKey.of(registryKey, specific).getHandle();
            }

            @Override
            public Class<MinecraftKey> getSpecificType() {
                return MinecraftKey.class;
            }
        };
    }

    @Override
    public String toString() {
        return "WrappedResourceKey{" + handle + "}";
    }
}

