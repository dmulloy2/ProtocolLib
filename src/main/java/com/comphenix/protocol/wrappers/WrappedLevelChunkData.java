package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.ZeroBuffer;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

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

        /**
         * The heightmap of this chunk.
         *
         * @return an NBT-Tag
         */
        public NbtCompound getHeightmapsTag() {
            return heightmapsTag;
        }

        /**
         * The actual structural data of this chunk as bytes.
         *
         * @return a byte array containing the chunks structural data.
         */
        public ByteBuf getBuffer() {
            return Unpooled.wrappedBuffer(buffer);
        }

        /**
         * All block entities of this chunk.
         *
         * @return a list containing {@link  BlockEntityInfo}
         */
        public List<BlockEntityInfo> getBlockEntityInfo() {
            return blockEntityInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ChunkData chunkData = (ChunkData) o;
            return Objects.equals(heightmapsTag, chunkData.heightmapsTag) && Arrays.equals(buffer, chunkData.buffer) && Objects.equals(blockEntityInfo, chunkData.blockEntityInfo);
        }

        @Override
        public int hashCode() {
            int hash = 1;

            hash = 31 * hash + Objects.hashCode(heightmapsTag);
            hash = 31 * hash + Arrays.hashCode(buffer);
            hash = 31 * hash + Objects.hashCode(blockEntityInfo);

            return hash;
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

        private static FieldAccessor blockEntitiesDataAccessor;
        private static FieldAccessor heightmapsAccessor;
        private static FieldAccessor bufferAccessor;

        public static EquivalentConverter<ChunkData> getConverter() {
            if (blockEntitiesDataAccessor == null) {
                blockEntitiesDataAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                        .getField(FuzzyFieldContract.newBuilder().typeExact(List.class).build()));
                heightmapsAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                        .getField(FuzzyFieldContract.newBuilder().typeExact(MinecraftReflection.getNBTCompoundClass()).build()));
                bufferAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                        .getField(FuzzyFieldContract.newBuilder().typeExact(byte[].class).build()));
            }

            return new EquivalentConverter<ChunkData>() {

                @Override
                public Object getGeneric(ChunkData specific) {
                    if(levelChunkPacketDataConstructor == null) {
                        levelChunkPacketDataConstructor = Accessors.getConstructorAccessor(HANDLE_TYPE, MinecraftReflection.getPacketDataSerializerClass(), int.class, int.class);
                    }

                    ConstructorAccessor trickySerializer = StructureCache.getTrickDataSerializerOrNull();

                    if(trickySerializer == null) {
                        throw new UnsupportedOperationException("TrickySerializer is not supported");
                    }

                    Object instance = levelChunkPacketDataConstructor.invoke(trickySerializer.invoke(new ZeroBuffer()), 0, 0);

                    bufferAccessor.set(instance, specific.buffer);
                    heightmapsAccessor.set(instance, NbtFactory.fromBase(specific.heightmapsTag).getHandle());

                    for (BlockEntityInfo entityInfo : specific.blockEntityInfo) {
                        //noinspection unchecked
                        ((List) blockEntitiesDataAccessor.get(instance)).add(BlockEntityInfo.getConverter().getGeneric(entityInfo));
                    }

                    return instance;
                }

                @Override
                public ChunkData getSpecific(Object generic) {
                    List<?> genericBlockEntities = (List<?>) blockEntitiesDataAccessor.get(generic);
                    List<WrappedLevelChunkData.BlockEntityInfo> wrappedEntityInfo;

                    if (genericBlockEntities.isEmpty()) {
                        wrappedEntityInfo = Collections.emptyList();
                    } else {
                        wrappedEntityInfo = new ArrayList<>(genericBlockEntities.size());

                        for (Object genericBE : ((List<?>) blockEntitiesDataAccessor.get(generic))) {
                            wrappedEntityInfo.add(BlockEntityInfo.getConverter().getSpecific(genericBE));
                        }

                        wrappedEntityInfo = Collections.unmodifiableList(wrappedEntityInfo);
                    }

                    return new ChunkData(NbtFactory.fromNMSCompound(heightmapsAccessor.get(generic)),
                            (byte[]) bufferAccessor.get(generic), wrappedEntityInfo);
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

        private final BitSet skyYMask;
        private final BitSet blockYMask;
        private final BitSet emptySkyYMask;
        private final BitSet emptyBlockYMask;

        private final List<byte[]> skyUpdates;
        private final List<byte[]> blockUpdates;

        private final boolean trustEdges;

        /**
         *
         * @param skyYMask        the sky light mask
         * @param blockYMask      the block light mask
         * @param emptySkyYMask   the empty sky light mask
         * @param emptyBlockYMask the empty block light mask
         * @param skyUpdates      a list of sky light arrays
         * @param blockUpdates    a list of block light arrays
         * @param trustEdges      whether edges can be trusted for light updates
         */
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

        /**
         * The sky light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getSkyYMask() {
            return skyYMask;
        }

        /**
         * The block light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getBlockYMask() {
            return blockYMask;
        }

        /**
         * The empty sky light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getEmptySkyYMask() {
            return emptySkyYMask;
        }

        /**
         * The empty block light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getEmptyBlockYMask() {
            return emptyBlockYMask;
        }

        /**
         * A list of sky light arrays.
         *
         * @return a list of byte arrays.
         */
        public List<byte[]> getSkyUpdates() {
            return skyUpdates;
        }

        /**
         * A list of block light arrays.
         *
         * @return a list of byte arrays.
         */
        public List<byte[]> getBlockUpdates() {
            return blockUpdates;
        }

        /**
         * Whether edges can be trusted for light updates or not.
         *
         * @return {@code true} if edges can be trusted, {@code false} otherwise.
         */
        public boolean isTrustEdges() {
            return trustEdges;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LightData lightData = (LightData) o;
            return trustEdges == lightData.trustEdges
                    && Objects.equals(skyYMask, lightData.skyYMask)
                    && Objects.equals(blockYMask, lightData.blockYMask)
                    && Objects.equals(emptySkyYMask, lightData.emptySkyYMask)
                    && Objects.equals(emptyBlockYMask, lightData.emptyBlockYMask)
                    && Arrays.deepEquals(skyUpdates.toArray(), lightData.skyUpdates.toArray())
                    && Arrays.deepEquals(blockUpdates.toArray(), lightData.blockUpdates.toArray());
        }

        @Override
        public int hashCode() {
            return Objects.hash(skyYMask, blockYMask, emptySkyYMask, emptyBlockYMask, skyUpdates, blockUpdates, trustEdges);
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

        private static List<FieldAccessor> bitSetAccessors;
        private static List<FieldAccessor> byteArrayListAccessors;

        private static FieldAccessor trustEdgesAccessor;

        public static EquivalentConverter<LightData> getConverter() {
            if (bitSetAccessors == null) {
                bitSetAccessors = asFieldAccessors(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                        .getFieldList(FuzzyFieldContract.newBuilder().typeExact(BitSet.class).build()));
                byteArrayListAccessors = asFieldAccessors(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                        .getFieldList(FuzzyFieldContract.newBuilder().typeExact(List.class).build()));
                trustEdgesAccessor = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                        .getField(FuzzyFieldContract.newBuilder().typeExact(boolean.class).build()));
            }

            return new EquivalentConverter<LightData>() {

                @Override
                public Object getGeneric(LightData specific) {
                    if (lightUpdatePacketDataConstructor == null) {
                        lightUpdatePacketDataConstructor = Accessors.getConstructorAccessor(HANDLE_TYPE, MinecraftReflection.getPacketDataSerializerClass(), int.class, int.class);
                    }

                    Object instance = lightUpdatePacketDataConstructor.invoke(MinecraftReflection.getPacketDataSerializer(new ZeroBuffer()), 0, 0);

                    trustEdgesAccessor.set(instance, specific.trustEdges);
                    bitSetAccessors.get(0).set(instance, specific.skyYMask);
                    bitSetAccessors.get(1).set(instance, specific.blockYMask);
                    bitSetAccessors.get(2).set(instance, specific.emptySkyYMask);
                    bitSetAccessors.get(3).set(instance, specific.emptyBlockYMask);
                    byteArrayListAccessors.get(0).set(instance, specific.skyUpdates);
                    byteArrayListAccessors.get(1).set(instance, specific.blockUpdates);

                    return instance;
                }

                @Override
                public LightData getSpecific(Object generic) {
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

        private static List<FieldAccessor> asFieldAccessors(List<Field> fields) {
            List<FieldAccessor> accessors = new ArrayList<>(fields.size());

            for (Field field : fields) {
                accessors.add(Accessors.getFieldAccessor(field));
            }

            return accessors;
        }
    }

    /**
     * Represents an immutable BlockEntityInfo in the MAP_CHUNK packet.
     *
     * @author Etrayed
     */
    public static class BlockEntityInfo {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getBlockEntityInfoClass();

        private static final FieldAccessor PACKED_XZ_ACCESSOR;
        private static final FieldAccessor Y_ACCESSOR;
        private static final FieldAccessor TYPE_ACCESSOR;
        private static final FieldAccessor TAG_ACCESSOR;

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

        private final int sectionX;
        private final int sectionZ;
        private final int y;

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
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BlockEntityInfo info = (BlockEntityInfo) o;
            return sectionX == info.sectionX && sectionZ == info.sectionZ && y == info.y
                    && Objects.equals(typeKey, info.typeKey) && Objects.equals(additionalData, info.additionalData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sectionX, sectionZ, y, typeKey, additionalData);
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
                    if (blockEntityInfoConstructor == null) {
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
