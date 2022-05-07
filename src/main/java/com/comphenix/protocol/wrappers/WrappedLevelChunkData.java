package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wrapper classes for ClientboundLevelChunkWithLightPacket
 *
 * @author Etrayed
 */
public final class WrappedLevelChunkData {

    private WrappedLevelChunkData() {}

    /**
     * Wrapper for ClientboundLevelChunkPacketData
     */
    public static class ChunkData {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getLevelChunkPacketDataClass();

        private final NbtCompound heightmapsTag;

        private final byte[] buffer;

        private final List<BlockEntityInfo> blockEntityInfo;

        /**
         *
         * @param heightmapsTag   Heightmap information
         * @param buffer          The actual chunk data
         * @param blockEntityInfo All block entities
         */
        public ChunkData(NbtCompound heightmapsTag, byte[] buffer, List<BlockEntityInfo> blockEntityInfo) {
            this.heightmapsTag = heightmapsTag;
            this.buffer = buffer;
            this.blockEntityInfo = blockEntityInfo;
        }

        public NbtCompound getHeightmapsTag() {
            return heightmapsTag;
        }

        public ByteBuf getBuffer() {
            return Unpooled.wrappedBuffer(buffer);
        }

        public List<BlockEntityInfo> getBlockEntityInfo() {
            return blockEntityInfo;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            ChunkData chunkData = (ChunkData) o;
            return Objects.equal(heightmapsTag, chunkData.heightmapsTag) && Arrays.equals(buffer, chunkData.buffer) && Objects.equal(blockEntityInfo, chunkData.blockEntityInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(heightmapsTag, buffer, blockEntityInfo);
        }

        @Override
        public String toString() {
            return "ChunkData{" +
                    "heightmapsTag=" + heightmapsTag +
                    ", buffer=" + Arrays.toString(buffer) +
                    ", blockEntityInfo=" + blockEntityInfo +
                    '}';
        }

        private static ConstructorAccessor levelChunkPacketDataConstructor;

        private static FieldAccessor blockEntitiesDataAccessor, heightmapsAccessor, bufferAccessor;

        public static EquivalentConverter<ChunkData> getConverter() {
            if(blockEntitiesDataAccessor == null) {
                blockEntitiesDataAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                        .getField(FuzzyFieldContract.newBuilder().typeExact(List.class).build()));
            }

            return new EquivalentConverter<ChunkData>() {

                @Override
                public Object getGeneric(ChunkData specific) {
                    if(levelChunkPacketDataConstructor == null) {
                        levelChunkPacketDataConstructor = Accessors.getConstructorAccessor(HANDLE_TYPE, MinecraftReflection.getPacketDataSerializerClass(), int.class, int.class);
                    }

                    ByteBuf byteBuf = Unpooled.buffer();

                    try (ByteBufOutputStream outputStream = new ByteBufOutputStream(byteBuf)) {
                        NbtBinarySerializer.DEFAULT.serialize(specific.heightmapsTag, outputStream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    WirePacket.writeVarInt(byteBuf, specific.buffer.length);

                    byteBuf.writeBytes(specific.buffer);

                    WirePacket.writeVarInt(byteBuf, 0); // we will inject block entity info later

                    Object generic = levelChunkPacketDataConstructor.invoke(MinecraftReflection.getPacketDataSerializer(byteBuf), 0, 0);

                    //noinspection unchecked
                    specific.blockEntityInfo.stream().map(BlockEntityInfo.getConverter()::getGeneric).forEach(((List) blockEntitiesDataAccessor.get(generic))::add);

                    return generic;
                }

                @Override
                public ChunkData getSpecific(Object generic) {
                    if(heightmapsAccessor == null) {
                        heightmapsAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                                .getField(FuzzyFieldContract.newBuilder().typeExact(MinecraftReflection.getNBTCompoundClass()).build()));
                        bufferAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                                .getField(FuzzyFieldContract.newBuilder().typeExact(byte[].class).build()));
                    }

                    return new ChunkData(NbtFactory.fromNMSCompound(heightmapsAccessor.get(generic)), (byte[]) bufferAccessor.get(generic),
                            ((List<?>) blockEntitiesDataAccessor.get(generic)).stream()
                                    .map(BlockEntityInfo.getConverter()::getSpecific)
                                    .collect(Collectors.toList()));
                }

                @Override
                public Class<ChunkData> getSpecificType() {
                    return ChunkData.class;
                }
            };
        }
    }

    /**
     * Wrapper for ClientboundLightUpdatePacketData
     */
    public static class LightData {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getLightUpdatePacketDataClass();

        private final BitSet skyYMask, blockYMask, emptySkyYMask, emptyBlockYMask;

        private final List<byte[]> skyUpdates, blockUpdates;

        private final boolean trustEdges;

        public LightData(BitSet skyYMask, BitSet blockYMask, BitSet emptySkyYMask, BitSet emptyBlockYMask,
                         List<byte[]> skyUpdates, List<byte[]> blockUpdates, boolean trustEdges) {
            this.skyYMask = skyYMask;
            this.blockYMask = blockYMask;
            this.emptySkyYMask = emptySkyYMask;
            this.emptyBlockYMask = emptyBlockYMask;
            this.skyUpdates = skyUpdates;
            this.blockUpdates = blockUpdates;
            this.trustEdges = trustEdges;
        }

        public BitSet getSkyYMask() {
            return skyYMask;
        }

        public BitSet getBlockYMask() {
            return blockYMask;
        }

        public BitSet getEmptySkyYMask() {
            return emptySkyYMask;
        }

        public BitSet getEmptyBlockYMask() {
            return emptyBlockYMask;
        }

        public List<byte[]> getSkyUpdates() {
            return skyUpdates;
        }

        public List<byte[]> getBlockUpdates() {
            return blockUpdates;
        }

        public boolean isTrustEdges() {
            return trustEdges;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            LightData lightData = (LightData) o;
            return trustEdges == lightData.trustEdges && Objects.equal(skyYMask, lightData.skyYMask) && Objects.equal(blockYMask, lightData.blockYMask) && Objects.equal(emptySkyYMask, lightData.emptySkyYMask) && Objects.equal(emptyBlockYMask, lightData.emptyBlockYMask) && Arrays.deepEquals(skyUpdates.toArray(), lightData.skyUpdates.toArray()) && Arrays.deepEquals(blockUpdates.toArray(), lightData.blockUpdates.toArray());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(skyYMask, blockYMask, emptySkyYMask, emptyBlockYMask, skyUpdates, blockUpdates, trustEdges);
        }

        @Override
        public String toString() {
            return "LightData{" +
                    "skyYMask=" + skyYMask +
                    ", blockYMask=" + blockYMask +
                    ", emptySkyYMask=" + emptySkyYMask +
                    ", emptyBlockYMask=" + emptyBlockYMask +
                    ", skyUpdates=" + skyUpdates +
                    ", blockUpdates=" + blockUpdates +
                    ", trustEdges=" + trustEdges +
                    '}';
        }

        private static ConstructorAccessor lightUpdatePacketDataConstructor;

        private static List<FieldAccessor> bitSetAccessors, byteArrayListAccessors;

        private static FieldAccessor trustEdgesAccessor;

        public static EquivalentConverter<LightData> getConverter() {
            return new EquivalentConverter<LightData>() {

                @Override
                public Object getGeneric(LightData specific) {
                    if(lightUpdatePacketDataConstructor == null) {
                        lightUpdatePacketDataConstructor = Accessors.getConstructorAccessor(HANDLE_TYPE, MinecraftReflection.getPacketDataSerializerClass(), int.class, int.class);
                    }

                    ByteBuf byteBuf = Unpooled.buffer();

                    byteBuf.writeBoolean(specific.trustEdges);

                    serializeBitSet(byteBuf, specific.skyYMask);
                    serializeBitSet(byteBuf, specific.blockYMask);
                    serializeBitSet(byteBuf, specific.emptySkyYMask);
                    serializeBitSet(byteBuf, specific.emptyBlockYMask);
                    serializeByteArrayList(byteBuf, specific.skyUpdates);
                    serializeByteArrayList(byteBuf, specific.blockUpdates);

                    return lightUpdatePacketDataConstructor.invoke(MinecraftReflection.getPacketDataSerializer(byteBuf), 0, 0);
                }

                private void serializeByteArrayList(ByteBuf byteBuf, List<byte[]> bytes) {
                    WirePacket.writeVarInt(byteBuf, bytes.size());

                    for (byte[] entry : bytes) {
                        WirePacket.writeVarInt(byteBuf, entry.length);

                        byteBuf.writeBytes(entry);
                    }
                }

                private void serializeBitSet(ByteBuf byteBuf, BitSet bitSet) {
                    long[] toSerialize = bitSet.toLongArray();

                    WirePacket.writeVarInt(byteBuf, toSerialize.length);

                    for (long l : toSerialize) {
                        byteBuf.writeLong(l);
                    }
                }

                @Override
                public LightData getSpecific(Object generic) {
                    if(bitSetAccessors == null) {
                        bitSetAccessors = FuzzyReflection.fromClass(HANDLE_TYPE, true)
                                .getFieldList(FuzzyFieldContract.newBuilder().typeExact(BitSet.class).build()).stream()
                                .map(Accessors::getFieldAccessor)
                                .collect(Collectors.toList());
                        byteArrayListAccessors = FuzzyReflection.fromClass(HANDLE_TYPE, true)
                                .getFieldList(FuzzyFieldContract.newBuilder().typeExact(List.class).build()).stream()
                                .map(Accessors::getFieldAccessor)
                                .collect(Collectors.toList());
                        trustEdgesAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                                .getField(FuzzyFieldContract.newBuilder().typeExact(boolean.class).build()));
                    }

                    //noinspection unchecked
                    return new LightData((BitSet) bitSetAccessors.get(0).get(generic), (BitSet) bitSetAccessors.get(1).get(generic),
                            (BitSet) bitSetAccessors.get(2).get(generic), (BitSet) bitSetAccessors.get(3).get(generic),
                            (List<byte[]>) byteArrayListAccessors.get(0).get(generic), (List<byte[]>) byteArrayListAccessors.get(1).get(generic),
                            (Boolean) trustEdgesAccessor.get(generic));
                }

                @Override
                public Class<LightData> getSpecificType() {
                    return LightData.class;
                }
            };
        }
    }

    /**
     * Represents an immutable BlockEntityInfo in the MAP_CHUNK packet.
     *
     * @author Etrayed
     */
    public static class BlockEntityInfo {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getBlockEntityInfoClass();

        private static final FieldAccessor PACKED_XZ_ACCESSOR, Y_ACCESSOR, TYPE_ACCESSOR, TAG_ACCESSOR;

        static {
            List<Field> posFields = FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getFieldList(FuzzyFieldContract.newBuilder().typeExact(int.class).build());

            PACKED_XZ_ACCESSOR = Accessors.getFieldAccessor(posFields.get(0));
            Y_ACCESSOR = Accessors.getFieldAccessor(posFields.get(1));
            TYPE_ACCESSOR = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getField(FuzzyFieldContract.newBuilder().typeExact(MinecraftReflection.getBlockEntityTypeClass()).build()));
            TAG_ACCESSOR = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getField(FuzzyFieldContract.newBuilder().typeExact(MinecraftReflection.getNBTCompoundClass()).build()));
        }

        private final int sectionX, sectionZ, y;

        private final MinecraftKey typeKey;

        @Nullable
        private final NbtCompound additionalData;

        /**
         * @param sectionX       the section-relative X-coordinate of the block entity.
         * @param sectionZ       the section-relative Z-coordinate of the block entity.
         * @param y              the Y-coordinate of the block entity.
         * @param typeKey        the minecraft key of the block entity type.
         */
        public BlockEntityInfo(int sectionX, int sectionZ, int y, MinecraftKey typeKey) {
            this(sectionX, sectionZ, y, typeKey, null);
        }

        /**
         * @param sectionX       the section-relative X-coordinate of the block entity.
         * @param sectionZ       the section-relative Z-coordinate of the block entity.
         * @param y              the Y-coordinate of the block entity.
         * @param typeKey        the minecraft key of the block entity type.
         * @param additionalData An NBT-Tag containing additional information. Can be {@code null}.
         */
        public BlockEntityInfo(int sectionX, int sectionZ, int y, MinecraftKey typeKey, @Nullable NbtCompound additionalData) {
            this.sectionX = sectionX;
            this.sectionZ = sectionZ;
            this.y = y;
            this.typeKey = typeKey;
            this.additionalData = additionalData;
        }

        /**
         * The section-relative X-coordinate of the block entity.
         *
         * @return the unpacked X-coordinate.
         */
        public int getSectionX() {
            return sectionX;
        }

        /**
         * The section-relative Y-coordinate of the block entity.
         *
         * @return the unpacked Y-coordinate.
         */
        public int getSectionZ() {
            return sectionZ;
        }

        /**
         * The Y-coordinate of the block entity.
         *
         * @return the Y-coordinate.
         */
        public int getY() {
            return y;
        }

        /**
         * The registry key of the block entity type.
         *
         * @return the registry key.
         */
        public MinecraftKey getTypeKey() {
            return typeKey;
        }

        /**
         * The NBT-Tag of this block entity containing additional information. (ex. text lines for a sign)
         *
         * @return the NBT-Tag or {@code null}.
         */
        @Nullable
        public NbtCompound getAdditionalData() {
            return additionalData;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            BlockEntityInfo info = (BlockEntityInfo) o;
            return sectionX == info.sectionX && sectionZ == info.sectionZ && y == info.y
                    && Objects.equal(typeKey, info.typeKey) && Objects.equal(additionalData, info.additionalData);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(sectionX, sectionZ, y, typeKey.getFullKey(), additionalData);
        }

        @Override
        public String toString() {
            return "BlockEntityInfo{" +
                    "sectionX=" + sectionX +
                    ", sectionZ=" + sectionZ +
                    ", y=" + y +
                    ", typeKey=" + typeKey.getFullKey() +
                    ", additionalData=" + additionalData +
                    '}';
        }

        private static ConstructorAccessor blockEntityInfoConstructor;

        public static EquivalentConverter<BlockEntityInfo> getConverter() {
            return new EquivalentConverter<BlockEntityInfo>() {

                @Override
                public Object getGeneric(BlockEntityInfo specific) {
                    if(blockEntityInfoConstructor == null) {
                        blockEntityInfoConstructor = Accessors.getConstructorAccessor(HANDLE_TYPE, int.class, int.class,
                                MinecraftReflection.getBlockEntityTypeClass(), MinecraftReflection.getNBTCompoundClass());
                    }

                    return blockEntityInfoConstructor.invoke(
                            specific.sectionX << 4 | specific.sectionZ,
                            specific.y,
                            WrappedRegistry.getBlockEntityTypeRegistry().get(specific.typeKey),
                            specific.additionalData == null ? null : NbtFactory.fromBase(specific.additionalData).getHandle()
                    );
                }

                @Override
                public BlockEntityInfo getSpecific(Object generic) {
                    int packedXZ = (int) PACKED_XZ_ACCESSOR.get(generic);
                    Object tagHandle = TAG_ACCESSOR.get(generic);
                    NbtCompound compound = tagHandle == null ? null : NbtFactory.fromNMSCompound(tagHandle);

                    return new BlockEntityInfo(packedXZ >> 4, packedXZ & 0xF, (int) Y_ACCESSOR.get(generic),
                            WrappedRegistry.getBlockEntityTypeRegistry().getKey(TYPE_ACCESSOR.get(generic)), compound);
                }

                @Override
                public Class<BlockEntityInfo> getSpecificType() {
                    return BlockEntityInfo.class;
                }
            };
        }
    }
}
