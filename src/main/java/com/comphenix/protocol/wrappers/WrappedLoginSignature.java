package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.primitives.Longs;

public class WrappedLoginSignature extends AbstractWrapper {

    private static ConstructorAccessor CONSTRUCTOR;

    private final StructureModifier<Object> modifier;

    /**
     * Construct a new NMS wrapper.
     */
    public WrappedLoginSignature(Object handle) {
        super(MinecraftReflection.getLoginSignatureClass());

        this.setHandle(handle);
        this.modifier = new StructureModifier<>(MinecraftReflection.getLoginSignatureClass()).withTarget(handle);
    }

    public WrappedLoginSignature(long salt, byte[] signature) {
        super(MinecraftReflection.getLoginSignatureClass());

        if (CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(
                this.getHandleType(),
                Long.TYPE, byte[].class);
        }

        this.setHandle(CONSTRUCTOR.invoke(salt, signature));
        this.modifier = new StructureModifier<>(MinecraftReflection.getLoginSignatureClass()).withTarget(this.handle);
    }

    public boolean isSigned() {
        return getSignature().length > 0;
    }

    public long getSalt() {
        return (long) modifier.withType(Long.TYPE).read(0);
    }

    public void setSalt(long salt) {
        modifier.withType(Long.TYPE).write(0, salt);
    }

    public byte[] getSignature() {
        return modifier.<byte[]>withType(byte[].class).read(0);
    }

    public void setSignature(byte[] signature) {
        modifier.<byte[]>withType(byte[].class).write(0, signature);
    }

    public byte[] getSaltBytes() {
        return Longs.toByteArray(getSalt());
    }
}
