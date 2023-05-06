package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.util.BitSet;

/**
 * @author Lukas Alt
 * @since 06.05.2023
 */
public class WrappedFilterMask extends AbstractWrapper {
    public final static Class<?> HANDLE_CLASS = MinecraftReflection.getMinecraftClass("network.chat.FilterMask");
    private final static Class<?> TYPE_CLASS = MinecraftReflection.getMinecraftClass("network.chat.FilterMask.Type");
    private static FieldAccessor MASK_ACCESSOR = Accessors.getFieldAccessor(HANDLE_CLASS, BitSet.class, true);
    private static FieldAccessor TYPE_ACCESSOR = Accessors.getFieldAccessor(HANDLE_CLASS, TYPE_CLASS, true);
    private static ConstructorAccessor CONSTRUCTOR_ACCESSOR;

    public WrappedFilterMask(Object handle) {
        super(HANDLE_CLASS);
        this.setHandle(handle);
    }

    public static WrappedFilterMask newInstance(BitSet bitSet, Type type) {
        if(CONSTRUCTOR_ACCESSOR == null) {
            CONSTRUCTOR_ACCESSOR = Accessors.getConstructorAccessor(HANDLE_CLASS, BitSet.class, TYPE_CLASS);
        }
        return new WrappedFilterMask(CONSTRUCTOR_ACCESSOR.invoke(bitSet, type));
    }

    public BitSet getMask() {
        return (BitSet) MASK_ACCESSOR.get(handle);
    }

    public void setMask(BitSet bitSet) {
        MASK_ACCESSOR.set(handle, bitSet);
    }

    public Type getType() {
        return (Type) TYPE_ACCESSOR.get(handle);
    }

    public void setType(Type type) {
        TYPE_ACCESSOR.set(handle, type);
    }

    public enum Type {
        PASS_THROUGH,
        FULLY_FILTERED,
        PARTIALLY_FILTERED;

        public static EquivalentConverter<Type> getConverter() {
            return new EnumWrappers.IndexedEnumConverter<>(TYPE_CLASS, Type.class);
        }
    }

    public static EquivalentConverter<WrappedFilterMask> getConverter() {
        return new EquivalentConverter<WrappedFilterMask>() {
            @Override
            public Object getGeneric(WrappedFilterMask specific) {
                return specific.getHandle();
            }

            @Override
            public WrappedFilterMask getSpecific(Object generic) {
                return new WrappedFilterMask(generic);
            }

            @Override
            public Class<WrappedFilterMask> getSpecificType() {
                return WrappedFilterMask.class;
            }
        };
    }
}
