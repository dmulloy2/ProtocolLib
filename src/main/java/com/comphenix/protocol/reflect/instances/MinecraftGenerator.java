package com.comphenix.protocol.reflect.instances;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MinecraftGenerator {

    // system unique id representation
    public static final UUID SYS_UUID;
    // minecraft default types
    public static final Object AIR_ITEM_STACK;
    private static Object DEFAULT_ENTITY_TYPES; // modern servers only (older servers will use an entity type id)
    // minecraft method accessors
    private static final MethodAccessor NON_NULL_LIST_CREATE;
    // fast util mappings for paper relocation
    private static final Map<Class<?>, ConstructorAccessor> FAST_MAP_CONSTRUCTORS;

    static {
        try {
            SYS_UUID = new UUID(0L, 0L);
            AIR_ITEM_STACK = BukkitConverters.getItemStackConverter().getGeneric(new ItemStack(Material.AIR));
            FAST_MAP_CONSTRUCTORS = new HashMap<>();
            NON_NULL_LIST_CREATE = MinecraftReflection.getNonNullListCreateAccessor();
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to create static fields in MinecraftGenerator", ex);
        }
    }

    public static final InstanceProvider INSTANCE = type -> {
        if (type != null) {
            if (type == UUID.class) {
                return SYS_UUID;
            } else if (type.isEnum()) {
                return type.getEnumConstants()[0];
            } else if (type == MinecraftReflection.getItemStackClass()) {
                return AIR_ITEM_STACK;
            } else if (type == MinecraftReflection.getEntityTypes()) {
                if (DEFAULT_ENTITY_TYPES == null) {
                    // try to initialize now
                    try {
                        DEFAULT_ENTITY_TYPES = BukkitConverters.getEntityTypeConverter().getGeneric(EntityType.AREA_EFFECT_CLOUD);
                    } catch (Exception ignored) {
                        // not available in this version of minecraft
                    }
                }
                return DEFAULT_ENTITY_TYPES;
            } else if (Map.class.isAssignableFrom(type)) {
                ConstructorAccessor ctor = FAST_MAP_CONSTRUCTORS.computeIfAbsent(type, __ -> {
                    try {
                        String name = type.getName();
                        if (name.contains("it.unimi.dsi.fastutil")) {
                            Class<?> clz = Class.forName(name.replace("Map", "OpenHashMap"));
                            return Accessors.getConstructorAccessorOrNull(clz);
                        }
                    } catch (Exception ignored) {
                    }
                    return null;
                });
                if (ctor != null) {
                    try {
                        return ctor.invoke();
                    } catch (Exception ignored) {
                    }
                }
            } else if (NON_NULL_LIST_CREATE != null && type == MinecraftReflection.getNonNullListClass()) {
                return NON_NULL_LIST_CREATE.invoke(null);
            }
        }

        return null;
    };
}
