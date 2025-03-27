package com.comphenix.protocol.injector.packet.internal;

import java.util.Arrays;
import java.util.List;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AbstractWrapper;
import com.comphenix.protocol.wrappers.WrappedStreamCodec;

/**
 * Class only works for version 1.20.5+
 */
public class IdCodecWrapper extends AbstractWrapper {

    private static final Class<?> ID_CODEC_CLASS;
    private static final FieldAccessor BY_ID_ACCESSOR;

    private static final Class<?> ID_CODEC_ENTRY_CLASS;
    private static final MethodAccessor ENTRY_TYPE_ACCESSOR;
    private static final MethodAccessor ENTRY_SERIALIZER_ACCESSOR;

    static {
        ID_CODEC_CLASS = MinecraftReflection.getMinecraftClass("network.codec.IdDispatchCodec");
        BY_ID_ACCESSOR = Accessors.getFieldAccessor(FuzzyReflection.fromClass(ID_CODEC_CLASS, true)
                .getField(FuzzyFieldContract.newBuilder().typeDerivedOf(List.class).build()));

        ID_CODEC_ENTRY_CLASS = Arrays.stream(ID_CODEC_CLASS.getNestMembers())
            .filter(Class::isRecord)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to find entry class for: " + ID_CODEC_CLASS));

        FuzzyReflection entryReflection = FuzzyReflection.fromClass(ID_CODEC_ENTRY_CLASS, true);
        ENTRY_TYPE_ACCESSOR = Accessors.getMethodAccessor(entryReflection
                .getMethodByReturnTypeAndParameters("type", Object.class, new Class[0]));
        ENTRY_SERIALIZER_ACCESSOR = Accessors.getMethodAccessor(entryReflection
                .getMethodByReturnTypeAndParameters("serializer", MinecraftReflection.getStreamCodecClass(), new Class[0]));
    }

    public IdCodecWrapper(Object handle) {
        super(ID_CODEC_CLASS);
        setHandle(handle);
    }

    public List<Entry> getById() {
        // list represents a packet-id to {type, codec} lookup
        List<?> byId = (List<?>) BY_ID_ACCESSOR.get(this.handle);
        return byId.stream().map(Entry::new).toList();
    }

    public class Entry extends AbstractWrapper {

        public Entry(Object handle) {
            super(ID_CODEC_ENTRY_CLASS);
            setHandle(handle);
        }

        public Object type() {
            return ENTRY_TYPE_ACCESSOR.invoke(this.handle);
        }

        public WrappedStreamCodec serializer() {
            Object serializer = ENTRY_SERIALIZER_ACCESSOR.invoke(this.handle);
            return new WrappedStreamCodec(serializer);
        }
    }
}
