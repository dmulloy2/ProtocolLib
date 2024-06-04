package com.comphenix.protocol.wrappers.codecs;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AbstractWrapper;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

public class WrappedDataResult extends AbstractWrapper {
    private final static Class<?> HANDLE_TYPE = MinecraftReflection.getLibraryClass("com.mojang.serialization.DataResult");
    private final static Optional<Class<?>> PARTIAL_DATA_RESULT_CLASS = MinecraftReflection.getOptionalLibraryClass("com.mojang.serialization.DataResult$PartialResult");
    private final static MethodAccessor ERROR_ACCESSOR = Accessors.getMethodAccessor(HANDLE_TYPE, "error");
    private final static MethodAccessor RESULT_ACCESSOR = Accessors.getMethodAccessor(HANDLE_TYPE, "result");
    private static MethodAccessor PARTIAL_RESULT_MESSAGE_ACCESSOR;

    static {
        if (PARTIAL_DATA_RESULT_CLASS.isPresent()) {
            PARTIAL_RESULT_MESSAGE_ACCESSOR = Accessors.getMethodAccessor(PARTIAL_DATA_RESULT_CLASS.get(), "message");
        }
    }

    /**
     * Construct a new NMS wrapper.
     **/
    public WrappedDataResult(Object handle) {
        super(HANDLE_TYPE);
        this.setHandle(handle);
    }

    public static WrappedDataResult fromHandle(Object handle) {
        return new WrappedDataResult(handle);
    }

    public Optional<Object> getResult() {
        return (Optional) RESULT_ACCESSOR.invoke(this.handle);
    }

    public Optional<String> getErrorMessage() {
        return (Optional) ERROR_ACCESSOR.invoke(this.handle);
    }

    public Object getOrThrow(Function<String, Throwable> errorHandler) {
        Optional<String> err = getErrorMessage();
        if (err.isPresent()) {
            return errorHandler.apply((String) PARTIAL_RESULT_MESSAGE_ACCESSOR.invoke(err.get()));
        }

        Optional<Object> result = getResult();
        if (result.isPresent()) {
            return result.get();
        }

        throw new NoSuchElementException();
    }
}
