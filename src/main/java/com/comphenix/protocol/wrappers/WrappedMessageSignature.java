package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * @author Lukas Alt
 * @since 24.04.2023
 */
public class WrappedMessageSignature extends AbstractWrapper {
    private final static Class<?> HANDLE_TYPE = MinecraftReflection.getMessageSignatureClass();
    private static ConstructorAccessor CONSTRUCTOR;
    private StructureModifier<Object> modifier;

    /**
     * Construct a new NMS wrapper.
     *
     * @param handle - the NMS handle
     */
    public WrappedMessageSignature(Object handle) {
        super(HANDLE_TYPE);
        this.setHandle(handle);
    }

    public WrappedMessageSignature(byte[] bytes) {
        super(HANDLE_TYPE);
        if(CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(HANDLE_TYPE, byte[].class);
        }
        this.setHandle(CONSTRUCTOR.invoke(bytes));
    }

    @Override
    protected void setHandle(Object handle) {
        super.setHandle(handle);
        this.modifier = new StructureModifier<>(HANDLE_TYPE).withTarget(handle);
    }

    public byte[] getBytes() {
        return modifier.<byte[]>withType(byte[].class).read(0);
    }

    public void setBytes(byte[] bytes) {
        modifier.<byte[]>withType(byte[].class).write(0, bytes);
    }
}
