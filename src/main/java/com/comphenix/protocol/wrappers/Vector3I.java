/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

public class Vector3I extends AbstractWrapper {
    public final static Class<?> HANDLE_TYPE = MinecraftReflection.getMinecraftClass("core.BaseBlockPosition", "core.Vec3i");
    private static FieldAccessor FIELD_X_ACCESSOR = Accessors.getFieldAccessorArray(HANDLE_TYPE, int.class, true)[0];
    private static FieldAccessor FIELD_Y_ACCESSOR = Accessors.getFieldAccessorArray(HANDLE_TYPE, int.class, true)[1];
    private static FieldAccessor FIELD_Z_ACCESSOR = Accessors.getFieldAccessorArray(HANDLE_TYPE, int.class, true)[2];
    private static ConstructorAccessor CONSTRUCTOR = null;

    public Vector3I(Object handle) {
        super(HANDLE_TYPE);
        this.setHandle(handle);
    }

    public static Vector3I newInstance(int x, int y, int z) {
        if (CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(HANDLE_TYPE, int.class, int.class, int.class);
        }
        return new Vector3I(CONSTRUCTOR.invoke(x, y, z));
    }


    public int getX() {
        return (int) FIELD_X_ACCESSOR.get(handle);
    }

    public Vector3I setX(int x) {
        FIELD_X_ACCESSOR.set(handle, x);
        return this;
    }

    public int getY() {
        return (int) FIELD_Y_ACCESSOR.get(handle);
    }

    public Vector3I setY(int y) {
        FIELD_Y_ACCESSOR.set(handle, y);
        return this;
    }

    public int getZ() {
        return (int) FIELD_Z_ACCESSOR.get(handle);
    }

    public Vector3I setZ(int z) {
        FIELD_Z_ACCESSOR.set(handle, z);
        return this;
    }

    public static EquivalentConverter<Vector3I> getConverter() {
        return Converters.ignoreNull(Converters.handle(Vector3I::getHandle, Vector3I::new, Vector3I.class));
    }
}