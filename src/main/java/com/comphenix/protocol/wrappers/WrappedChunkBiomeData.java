package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.util.Arrays;
import java.util.Objects;

/**
 * Wrapper for {@code ClientboundChunksBiomesPacket.ChunkBiomeData}.
 */
public final class WrappedChunkBiomeData {

    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getMinecraftClass(
            "network.protocol.game.ClientboundChunksBiomesPacket$ChunkBiomeData");
    private static final ConstructorAccessor CONSTRUCTOR = Accessors.getConstructorAccessor(
            HANDLE_TYPE,
            MinecraftReflection.getChunkCoordIntPair(),
            byte[].class);
    private static final MethodAccessor GET_POS = Accessors.getMethodAccessor(HANDLE_TYPE, "pos");
    private static final MethodAccessor GET_BUFFER = Accessors.getMethodAccessor(HANDLE_TYPE, "buffer");

    public static final EquivalentConverter<WrappedChunkBiomeData> CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(WrappedChunkBiomeData specific) {
                    return CONSTRUCTOR.invoke(
                            ChunkCoordIntPair.getConverter().getGeneric(specific.position),
                            specific.buffer.clone());
                }

                @Override
                public WrappedChunkBiomeData getSpecific(Object generic) {
                    return new WrappedChunkBiomeData(
                            ChunkCoordIntPair.getConverter().getSpecific(GET_POS.invoke(generic)),
                            ((byte[]) GET_BUFFER.invoke(generic)).clone());
                }

                @Override
                public Class<WrappedChunkBiomeData> getSpecificType() {
                    return WrappedChunkBiomeData.class;
                }
            });

    private final ChunkCoordIntPair position;
    private final byte[] buffer;

    public WrappedChunkBiomeData(ChunkCoordIntPair position, byte[] buffer) {
        this.position = position;
        this.buffer = buffer.clone();
    }

    public ChunkCoordIntPair getPosition() {
        return position;
    }

    public byte[] getBuffer() {
        return buffer.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WrappedChunkBiomeData that)) {
            return false;
        }
        return Objects.equals(position, that.position) && Arrays.equals(buffer, that.buffer);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(position);
        result = 31 * result + Arrays.hashCode(buffer);
        return result;
    }

    @Override
    public String toString() {
        return "WrappedChunkBiomeData{position=" + position + ", buffer=" + Arrays.toString(buffer) + '}';
    }
}
