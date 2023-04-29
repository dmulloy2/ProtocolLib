package com.comphenix.protocol.wrappers.codecs;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AbstractWrapper;

public class WrappedCodec extends AbstractWrapper {
    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getCodecClass();
    private static final Class<?> ENCODER_CLASS = MinecraftReflection.getLibraryClass("com.mojang.serialization.Encoder");
    private static final Class<?> DECODER_CLASS = MinecraftReflection.getLibraryClass("com.mojang.serialization.Decoder");
    private static final MethodAccessor ENCODE_START_ACCESSOR = Accessors.getMethodAccessor(ENCODER_CLASS, "encodeStart", MinecraftReflection.getDynamicOpsClass(), Object.class);
    private static final MethodAccessor PARSE_ACCESSOR = Accessors.getMethodAccessor(DECODER_CLASS, "parse", MinecraftReflection.getDynamicOpsClass(), Object.class);

    private WrappedCodec(Object handle) {
        super(HANDLE_TYPE);
        this.setHandle(handle);
    }

    public static WrappedCodec fromHandle(Object handle) {
        return new WrappedCodec(handle);
    }

    public WrappedDataResult encode(Object object, WrappedDynamicOps ops) {
        return WrappedDataResult.fromHandle(ENCODE_START_ACCESSOR.invoke(handle, ops.getHandle(), object));
    }

    public WrappedDataResult parse(Object value, WrappedDynamicOps ops) {
        return WrappedDataResult.fromHandle(PARSE_ACCESSOR.invoke(handle, ops.getHandle(), value));
    }
}
