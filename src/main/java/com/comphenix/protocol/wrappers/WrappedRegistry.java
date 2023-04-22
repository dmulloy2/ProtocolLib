package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WrappedRegistry {
    // map of NMS class to registry instance
    private static final Map<Class<?>, WrappedRegistry> REGISTRY;

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

		FuzzyReflection fuzzy = FuzzyReflection.fromClass(iRegistry, false);
		GET = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
				.newBuilder()
				.parameterCount(1)
				.returnDerivedOf(Object.class)
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

    private WrappedRegistry(Object handle) {
        this.handle = handle;
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
		return REGISTRY.get(type);
	}
}
