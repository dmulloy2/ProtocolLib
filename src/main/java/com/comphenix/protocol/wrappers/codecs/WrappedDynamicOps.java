package com.comphenix.protocol.wrappers.codecs;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AbstractWrapper;

public class WrappedDynamicOps extends AbstractWrapper {
    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDynamicOpsClass();
    public static final FieldAccessor NBT_ACCESSOR = Accessors.getFieldAccessor(MinecraftReflection.getNbtOpsClass(), MinecraftReflection.getNbtOpsClass(), false);
    public static final FieldAccessor[] JSON_ACCESSORS = Accessors.getFieldAccessorArray(MinecraftReflection.getJsonOpsClass(), MinecraftReflection.getJsonOpsClass(), false);
    private WrappedDynamicOps(Object handle) {
        super(HANDLE_TYPE);
        this.setHandle(handle);
    }
    public static WrappedDynamicOps fromHandle(Object handle) {
        return new WrappedDynamicOps(handle);
    }
    public static WrappedDynamicOps json(boolean compressed) {
        return fromHandle(JSON_ACCESSORS[compressed ? 1 : 0].get(null));
    }

    public static WrappedDynamicOps nbt() {
        return fromHandle(NBT_ACCESSOR);
    }
}
