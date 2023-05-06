package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.annotation.Nonnull;

public class WrappedPacketDataSerializer extends AbstractWrapper {
    public final static Class<?> PACKET_DATA_SERIALIZER_CLASS = MinecraftReflection.getPacketDataSerializerClass();
    private final static FieldAccessor BUFFER_ACCESSOR = Accessors.getFieldAccessor(PACKET_DATA_SERIALIZER_CLASS, MinecraftReflection.getByteBufClass(), true);
    private static ConstructorAccessor CONSTRUCTOR;
    /**
     * Construct a new NMS wrapper.
     *
     * @param handle - the NMS handle.
     */
    public WrappedPacketDataSerializer(Object handle) {
        super(PACKET_DATA_SERIALIZER_CLASS);
        this.setHandle(handle);
    }

    public ByteBuf getBuffer() {
        return (ByteBuf) BUFFER_ACCESSOR.get(handle);
    }

    public void setBuffer(@Nonnull ByteBuf buf) {
        BUFFER_ACCESSOR.set(handle, buf);
    }

    public static WrappedPacketDataSerializer fromHandle(Object handle) {
        return new WrappedPacketDataSerializer(handle);
    }

    public static WrappedPacketDataSerializer newBuffer() {
        if(CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(PACKET_DATA_SERIALIZER_CLASS, ByteBuf.class);
        }
        return fromHandle(CONSTRUCTOR.invoke(Unpooled.buffer()));
    }

    public static EquivalentConverter<WrappedPacketDataSerializer> getConverter() {
        return new EquivalentConverter<WrappedPacketDataSerializer>() {
            @Override
            public Object getGeneric(WrappedPacketDataSerializer specific) {
                return specific.getHandle();
            }

            @Override
            public WrappedPacketDataSerializer getSpecific(Object generic) {
                return WrappedPacketDataSerializer.fromHandle(generic);
            }

            @Override
            public Class<WrappedPacketDataSerializer> getSpecificType() {
                return WrappedPacketDataSerializer.class;
            }
        };
    }
}
