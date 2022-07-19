package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.primitives.Longs;

/**
 * Wrapper representing the signature data associated to signed data by the player. This includes signed chat messages
 * and login encryption acknowledgments.
 */
public class WrappedSaltedSignature extends AbstractWrapper {

    private static ConstructorAccessor CONSTRUCTOR;

    private final StructureModifier<Object> modifier;

    /**
     * Construct a wrapper from a NMS handle
     * 
     * @param handle NMS Signature object
     */
    public WrappedSaltedSignature(Object handle) {
        super(MinecraftReflection.getSaltedSignatureClass());

        this.setHandle(handle);
        this.modifier = new StructureModifier<>(MinecraftReflection.getSaltedSignatureClass()).withTarget(handle);
    }

    /**
     * Construct a wrapper and NMS handle containing the given values
     * @param salt salt/nonce for this signature
     * @param signature binary cryptographic signature
     */
    public WrappedSaltedSignature(long salt, byte[] signature) {
        super(MinecraftReflection.getSaltedSignatureClass());

        if (CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(
                this.getHandleType(),
                Long.TYPE, byte[].class);
        }

        this.setHandle(CONSTRUCTOR.invoke(salt, signature));
        this.modifier = new StructureModifier<>(MinecraftReflection.getSaltedSignatureClass()).withTarget(this.handle);
    }

    /**
     * @return if a cryptographic signature data is present
     */
    public boolean isSigned() {
        return getSignature().length > 0;
    }

    /**
     * @return cryptographic salt/nonce
     */
    public long getSalt() {
        return (long) modifier.withType(Long.TYPE).read(0);
    }

    /**
     * @param salt cryptographic salt/nonce
     */
    public void setSalt(long salt) {
        modifier.withType(Long.TYPE).write(0, salt);
    }

    /**
     * @return binary signature data associated to the salt and message 
     */
    public byte[] getSignature() {
        return modifier.<byte[]>withType(byte[].class).read(0);
    }

    /**
     * @param signature binary signature data associated to the salt and message
     */
    public void setSignature(byte[] signature) {
        modifier.<byte[]>withType(byte[].class).write(0, signature);
    }

    /**
     * @return the long salt represented in 8 bytes
     */
    public byte[] getSaltBytes() {
        return Longs.toByteArray(getSalt());
    }
}
