package com.comphenix.protocol.wrappers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import com.comphenix.protocol.reflect.EquivalentConverter;
import org.jetbrains.annotations.Nullable;

import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.collect.Lists;

/**
 * Wrapper classes for ClientboundLevelChunkWithLightPacket
 *
 * @author Etrayed
 */
public final class WrappedLevelChunkData {

    private WrappedLevelChunkData() {
    }

    /**
     * Wrapper for ClientboundLevelChunkPacketData
     */
    public static final class ChunkData extends AbstractWrapper {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getLevelChunkPacketDataClass();

        private static final ConstructorAccessor LEVEL_CHUNK_PACKET_DATA_CONSTRUCTOR;

        private static final FieldAccessor BLOCK_ENTITIES_DATA_ACCESSOR;
        private static final FieldAccessor HEIGHTMAPS_ACCESSOR;
        private static final EquivalentConverter<Map<EnumWrappers.HeightmapType, long[]>> HEIGHTMAPS_CONVERTER;
        private static final FieldAccessor BUFFER_ACCESSOR;

        static {
            FuzzyReflection reflection = FuzzyReflection.fromClass(HANDLE_TYPE, true);

            LEVEL_CHUNK_PACKET_DATA_CONSTRUCTOR = Accessors.getConstructorAccessor(reflection.getConstructor(FuzzyMethodContract.newBuilder()
            		.parameterDerivedOf(MinecraftReflection.getPacketDataSerializerClass())
            		.parameterExactType(int.class)
            		.parameterExactType(int.class)
            		.build()));
            BLOCK_ENTITIES_DATA_ACCESSOR = Accessors.getFieldAccessor(reflection.getField(FuzzyFieldContract.newBuilder()
                    .typeExact(List.class)
                    .build()));
            if (MinecraftVersion.v1_21_5.atOrAbove()) {
                HEIGHTMAPS_ACCESSOR = Accessors.getFieldAccessor(reflection.getField(FuzzyFieldContract.newBuilder()
                        .typeExact(Map.class)
                        .build()));
                HEIGHTMAPS_CONVERTER = BukkitConverters.getMapConverter(EnumWrappers.getHeightmapTypeConverter(), Converters.passthrough(long[].class));
            } else {
                HEIGHTMAPS_ACCESSOR = Accessors.getFieldAccessor(reflection.getField(FuzzyFieldContract.newBuilder()
                        .typeExact(MinecraftReflection.getNBTCompoundClass())
                        .build()));
                HEIGHTMAPS_CONVERTER = null;
            }
            BUFFER_ACCESSOR = Accessors.getFieldAccessor(reflection.getField(FuzzyFieldContract.newBuilder().typeExact(byte[].class).build()));
        }

        public ChunkData(Object handle) {
            super(HANDLE_TYPE);

            setHandle(handle);
        }

        /**
         * The heightmap of this chunk.
         * <p>
         * Removed in Minecraft 1.21.5.
         *
         * @return an NBT-Tag
         * @deprecated Use {@link ChunkData#getHeightmaps()} instead.
         */
        @Deprecated
        public NbtCompound getHeightmapsTag() {
            return NbtFactory.fromNMSCompound(HEIGHTMAPS_ACCESSOR.get(handle));
        }

        /**
         * Sets the heightmap tag of this chunk.
         * <p>
         * Removed in Minecraft 1.21.5.
         *
         * @param heightmapsTag the new heightmaps tag.
         * @deprecated Use {@link ChunkData#setHeightmaps(Map)} instead.
         */
        @Deprecated
        public void setHeightmapsTag(NbtCompound heightmapsTag) {
            HEIGHTMAPS_ACCESSOR.set(handle, NbtFactory.fromBase(heightmapsTag).getHandle());
        }

        /**
         * The heightmap of this chunk.
         *
         * @return a map containing the heightmaps
         */
        public Map<EnumWrappers.HeightmapType, long[]> getHeightmaps() {
            return HEIGHTMAPS_CONVERTER.getSpecific(HEIGHTMAPS_ACCESSOR.get(handle));
        }

        /**
         * Sets the heightmap tag of this chunk.
         * <p>
         * Removed in Minecraft 1.21.5.
         *
         * @param heightmaps the new heightmaps.
         */
        public void setHeightmaps(Map<EnumWrappers.HeightmapType, long[]> heightmaps) {
            HEIGHTMAPS_ACCESSOR.set(handle, HEIGHTMAPS_CONVERTER.getGeneric(heightmaps));
        }

        /**
         * The actual structural data of this chunk as bytes.
         *
         * @return a byte array containing the chunks structural data.
         */
        public byte[] getBuffer() {
            return (byte[]) BUFFER_ACCESSOR.get(handle);
        }

        /**
         * Sets the structural data of this chunk.
         *
         * @param buffer the new buffer.
         */
        public void setBuffer(byte[] buffer) {
            BUFFER_ACCESSOR.set(handle, buffer);
        }

        /**
         * All block entities of this chunk. Supports removal and other edits.
         *
         * @return a mutable (remove only) list containing {@link  BlockEntityInfo}
         */
        public List<BlockEntityInfo> getBlockEntityInfo() {
            //noinspection StaticPseudoFunctionalStyleMethod
            return Lists.transform((List<?>) BLOCK_ENTITIES_DATA_ACCESSOR.get(handle), BlockEntityInfo::new);
        }

        /**
         * Sets the block entities of this chunk. Supports removal and other edits.
         *
         * @param blockEntityInfo the new list of block entities
         */
        public void setBlockEntityInfo(List<BlockEntityInfo> blockEntityInfo) {
            List handleList = new ArrayList<>(blockEntityInfo.size());

            for (BlockEntityInfo info : blockEntityInfo) {
                //noinspection unchecked
                handleList.add(info.getHandle());
            }

            BLOCK_ENTITIES_DATA_ACCESSOR.set(handle, handleList);
        }

        /**
         * Creates a new wrapper using predefined values.
         * <p>
         * Removed in Minecraft 1.21.5.
         *
         * @param heightmapsTag   the heightmaps tag
         * @param buffer          the buffer
         * @param blockEntityInfo a list of wrapped block entities
         * @return a newly created wrapper
         * @deprecated Use {@link ChunkData#fromValues(Map, byte[], List)} instead.
         */
        @Deprecated
        public static ChunkData fromValues(NbtCompound heightmapsTag, byte[] buffer, List<BlockEntityInfo> blockEntityInfo) {
            ChunkData data = new ChunkData(LEVEL_CHUNK_PACKET_DATA_CONSTRUCTOR.invoke(StructureCache.newNullDataSerializer(), 0, 0));

            data.setHeightmapsTag(heightmapsTag);
            data.setBuffer(buffer);
            data.setBlockEntityInfo(blockEntityInfo);

            return new ChunkData(data);
        }

        /**
         * Creates a new wrapper using predefined values.
         *
         * @param heightmaps      the heightmaps
         * @param buffer          the buffer
         * @param blockEntityInfo a list of wrapped block entities
         * @return a newly created wrapper
         */
        public static ChunkData fromValues(Map<EnumWrappers.HeightmapType, long[]> heightmaps, byte[] buffer, List<BlockEntityInfo> blockEntityInfo) {
            ChunkData data = new ChunkData(LEVEL_CHUNK_PACKET_DATA_CONSTRUCTOR.invoke(StructureCache.newNullDataSerializer(), 0, 0));

            data.setHeightmaps(heightmaps);
            data.setBuffer(buffer);
            data.setBlockEntityInfo(blockEntityInfo);

            return new ChunkData(data);
        }
    }

    /**
     * Wrapper for ClientboundLightUpdatePacketData
     */
    public static class LightData extends AbstractWrapper {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getLightUpdatePacketDataClass();

        private static final ConstructorAccessor LIGHT_UPDATE_PACKET_DATA_CONSTRUCTOR;

        private static final FieldAccessor[] BIT_SET_ACCESSORS;
        private static final FieldAccessor[] BYTE_ARRAY_LIST_ACCESSORS;

        private static final FieldAccessor TRUST_EDGES_ACCESSOR;

        static {
            FuzzyReflection reflection = FuzzyReflection.fromClass(HANDLE_TYPE, true);

            LIGHT_UPDATE_PACKET_DATA_CONSTRUCTOR = Accessors.getConstructorAccessor(reflection.getConstructor(FuzzyMethodContract.newBuilder()
            		.parameterDerivedOf(MinecraftReflection.getPacketDataSerializerClass())
            		.parameterExactType(int.class)
            		.parameterExactType(int.class)
            		.build()));
            BIT_SET_ACCESSORS = Accessors.getFieldAccessorArray(HANDLE_TYPE, BitSet.class, true);
            BYTE_ARRAY_LIST_ACCESSORS = Accessors.getFieldAccessorArray(HANDLE_TYPE, List.class, true);

            if(MinecraftVersion.TRAILS_AND_TAILS.atOrAbove()) {
                TRUST_EDGES_ACCESSOR = null;
            } else {
                TRUST_EDGES_ACCESSOR = Accessors.getFieldAccessor(reflection.getField(FuzzyFieldContract.newBuilder()
                        .typeExact(boolean.class)
                        .build()));
            }
        }

        private boolean dummyTrustEdges = false;

        public LightData(Object handle) {
            super(HANDLE_TYPE);

            setHandle(handle);
        }

        /**
         * The sky light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getSkyYMask() {
            return (BitSet) BIT_SET_ACCESSORS[0].get(handle);
        }

        /**
         * Sets the sky light mask
         *
         * @param skyYMask the new mask
         */
        public void setSkyYMask(BitSet skyYMask) {
            BIT_SET_ACCESSORS[0].set(handle, skyYMask);
        }

        /**
         * The block light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getBlockYMask() {
            return (BitSet) BIT_SET_ACCESSORS[1].get(handle);
        }

        /**
         * Sets the block light mask
         *
         * @param blockYMask the new mask
         */
        public void setBlockYMask(BitSet blockYMask) {
            BIT_SET_ACCESSORS[1].set(handle, blockYMask);
        }

        /**
         * The empty sky light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getEmptySkyYMask() {
            return (BitSet) BIT_SET_ACCESSORS[2].get(handle);
        }

        /**
         * Sets the empty sky light mask
         *
         * @param emptySkyYMask the new mask
         */
        public void setEmptySkyYMask(BitSet emptySkyYMask) {
            BIT_SET_ACCESSORS[2].set(handle, emptySkyYMask);
        }

        /**
         * The empty block light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getEmptyBlockYMask() {
            return (BitSet) BIT_SET_ACCESSORS[3].get(handle);
        }

        /**
         * Sets the empty block light mask
         *
         * @param emptyBlockYMask the new mask
         */
        public void setEmptyBlockYMask(BitSet emptyBlockYMask) {
            BIT_SET_ACCESSORS[3].set(handle, emptyBlockYMask);
        }

        /**
         * A mutable list of sky light arrays.
         *
         * @return a mutable list of byte arrays.
         */
        public List<byte[]> getSkyUpdates() {
            //noinspection unchecked
            return (List<byte[]>) BYTE_ARRAY_LIST_ACCESSORS[0].get(handle);
        }

        /**
         * A mutable list of block light arrays.
         *
         * @return a mutable list of byte arrays.
         */
        public List<byte[]> getBlockUpdates() {
            //noinspection unchecked
            return (List<byte[]>) BYTE_ARRAY_LIST_ACCESSORS[1].get(handle);
        }

        /**
         * Whether edges can be trusted for light updates or not.
         * @deprecated Removed in 1.20
         * @return {@code true} if edges can be trusted, {@code false} otherwise.
         */
        @Deprecated
        public boolean isTrustEdges() {
            if(MinecraftVersion.TRAILS_AND_TAILS.atOrAbove()) {
                return dummyTrustEdges; // ensure backwards compatability and prevent inconsistent states
            }
            return (boolean) TRUST_EDGES_ACCESSOR.get(handle);
        }

        /**
         * Sets whether edges can be trusted for light updates or not.
         * @deprecated Removed in 1.20
         * @param trustEdges the new value
         */
        @Deprecated
        public void setTrustEdges(boolean trustEdges) {
            if(MinecraftVersion.TRAILS_AND_TAILS.atOrAbove()) {
                dummyTrustEdges = trustEdges; // ensure backwards compatability and prevent inconsistent states
                return;
            }
            TRUST_EDGES_ACCESSOR.set(handle, trustEdges);
        }

        /**
         * Constructs new LightData from values
         * @param trustEdges Ignored
         * @param skyYMask skyYMask
         * @param blockYMask blockYMask
         * @param emptySkyYMask emptySkyYMask
         * @param emptyBlockYMask emptyBlockYMask
         * @param skyUpdates skyUpdates
         * @param blockUpdates blockUpdates
         * @deprecated Parameter trustEdges was removed in 1.20
         * @return new light data
         */
        @Deprecated
        public static LightData fromValues(BitSet skyYMask, BitSet blockYMask, BitSet emptySkyYMask, BitSet emptyBlockYMask,
                                           List<byte[]> skyUpdates, List<byte[]> blockUpdates, boolean trustEdges) {
            LightData lightData = fromValues(skyYMask, blockYMask, emptySkyYMask, emptyBlockYMask, skyUpdates, blockUpdates);
            lightData.setTrustEdges(trustEdges);
            return lightData;
        }

        /**
         * Constructs new LightData from values
         * @param skyYMask skyYMask
         * @param blockYMask blockYMask
         * @param emptySkyYMask emptySkyYMask
         * @param emptyBlockYMask emptyBlockYMask
         * @param skyUpdates skyUpdates
         * @param blockUpdates blockUpdates
         * @return new light data
         */
        public static LightData fromValues(BitSet skyYMask, BitSet blockYMask, BitSet emptySkyYMask, BitSet emptyBlockYMask,
                                           List<byte[]> skyUpdates, List<byte[]> blockUpdates) {
            LightData data = new LightData(LIGHT_UPDATE_PACKET_DATA_CONSTRUCTOR.invoke(StructureCache.newNullDataSerializer(), 0, 0));

            data.setSkyYMask(skyYMask);
            data.setBlockYMask(blockYMask);
            data.setEmptySkyYMask(emptySkyYMask);
            data.setEmptyBlockYMask(emptyBlockYMask);
            data.getSkyUpdates().addAll(skyUpdates);
            data.getBlockUpdates().addAll(blockUpdates);

            return data;
        }
    }

    /**
     * Represents an immutable BlockEntityInfo in the MAP_CHUNK packet.
     *
     * @author Etrayed
     */
    public static class BlockEntityInfo extends AbstractWrapper {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getBlockEntityInfoClass();
        private static final WrappedRegistry REGISTRY = WrappedRegistry.getRegistry(MinecraftReflection.getBlockEntityTypeClass());

        private static final ConstructorAccessor BLOCK_ENTITY_INFO_CONSTRUCTOR;

        private static final FieldAccessor PACKED_XZ_ACCESSOR;
        private static final FieldAccessor Y_ACCESSOR;
        private static final FieldAccessor TYPE_ACCESSOR;
        private static final FieldAccessor TAG_ACCESSOR;

        static {
            FuzzyReflection reflection = FuzzyReflection.fromClass(HANDLE_TYPE, true);
            List<Field> posFields = reflection.getFieldList(FuzzyFieldContract.newBuilder().typeExact(int.class).build());

            BLOCK_ENTITY_INFO_CONSTRUCTOR = Accessors.getConstructorAccessor(HANDLE_TYPE, int.class, int.class,
                    MinecraftReflection.getBlockEntityTypeClass(), MinecraftReflection.getNBTCompoundClass());
            PACKED_XZ_ACCESSOR = Accessors.getFieldAccessor(posFields.get(0));
            Y_ACCESSOR = Accessors.getFieldAccessor(posFields.get(1));
            TYPE_ACCESSOR = Accessors.getFieldAccessor(reflection.getField(FuzzyFieldContract.newBuilder()
                    .typeExact(MinecraftReflection.getBlockEntityTypeClass())
                    .build()));
            TAG_ACCESSOR = Accessors.getFieldAccessor(reflection.getField(FuzzyFieldContract.newBuilder()
                    .typeExact(MinecraftReflection.getNBTCompoundClass())
                    .build()));
        }

        public BlockEntityInfo(Object handle) {
            super(HANDLE_TYPE);

            setHandle(handle);
        }

        /**
         * The section-relative X-coordinate of the block entity.
         *
         * @return the unpacked X-coordinate.
         */
        public int getSectionX() {
            return (int) PACKED_XZ_ACCESSOR.get(handle) >> 4;
        }

        /**
         * Sets the section-relative X-coordinate of the block entity
         *
         * @param sectionX the section-relative x coordinate
         */
        public void setSectionX(int sectionX) {
            PACKED_XZ_ACCESSOR.set(handle, sectionX << 4 | getSectionZ());
        }

        /**
         * The section-relative Z-coordinate of the block entity.
         *
         * @return the unpacked Z-coordinate.
         */
        public int getSectionZ() {
            return (int) PACKED_XZ_ACCESSOR.get(handle) & 0xF;
        }

        /**
         * Sets the section-relative Z-coordinate of the block entity
         *
         * @param sectionZ the section-relative z coordinate
         */
        public void setSectionZ(int sectionZ) {
            PACKED_XZ_ACCESSOR.set(handle, getSectionX() << 4 | sectionZ);
        }

        /**
         * The Y-coordinate of the block entity.
         *
         * @return the Y-coordinate.
         */
        public int getY() {
            return (int) Y_ACCESSOR.get(handle);
        }

        /**
         * Sets the Y-coordinate of the block entity.
         *
         * @param y the new y coordinate
         */
        public void setY(int y) {
            Y_ACCESSOR.set(handle, y);
        }

        /**
         * The registry key of the block entity type.
         *
         * @return the registry key.
         */
        public MinecraftKey getTypeKey() {
            return REGISTRY.getKey(TYPE_ACCESSOR.get(handle));
        }

        /**
         * Sets the registry key of the block entity type
         *
         * @param typeKey the new block entity type key
         */
        public void setTypeKey(MinecraftKey typeKey) {
            TYPE_ACCESSOR.set(handle, REGISTRY.get(typeKey));
        }

        /**
         * The NBT-Tag of this block entity containing additional information. (ex. text lines for a sign)
         *
         * @return the NBT-Tag or {@code null}.
         */
        @Nullable
        public NbtCompound getAdditionalData() {
            Object tagHandle = TAG_ACCESSOR.get(handle);

            return tagHandle == null ? null : NbtFactory.fromNMSCompound(tagHandle);
        }

        /**
         * Edits the additional data specified for this block entity.
         *
         * @param additionalData the additional data for this block entity, can be {@code null}
         */
        public void setAdditionalData(@Nullable NbtCompound additionalData) {
            TAG_ACCESSOR.set(handle, additionalData == null ? null : NbtFactory.fromBase(additionalData).getHandle());
        }

        /**
         * Creates a wrapper using raw values
         *
         * @param sectionX the section-relative X-coordinate of the block entity.
         * @param sectionZ the section-relative Z-coordinate of the block entity.
         * @param y        the Y-coordinate of the block entity.
         * @param typeKey  the minecraft key of the block entity type.
         */
        public static BlockEntityInfo fromValues(int sectionX, int sectionZ, int y, MinecraftKey typeKey) {
            return fromValues(sectionX, sectionZ, y, typeKey, null);
        }

        /**
         * Creates a wrapper using raw values
         *
         * @param sectionX       the section-relative X-coordinate of the block entity.
         * @param sectionZ       the section-relative Z-coordinate of the block entity.
         * @param y              the Y-coordinate of the block entity.
         * @param typeKey        the minecraft key of the block entity type.
         * @param additionalData An NBT-Tag containing additional information. Can be {@code null}.
         */
        public static BlockEntityInfo fromValues(int sectionX, int sectionZ, int y, MinecraftKey typeKey, @Nullable NbtCompound additionalData) {
            return new BlockEntityInfo(BLOCK_ENTITY_INFO_CONSTRUCTOR.invoke(
                    sectionX << 4 | sectionZ,
                    y,
                    REGISTRY.get(typeKey),
                    additionalData == null ? null : NbtFactory.fromBase(additionalData).getHandle()
            ));
        }
    }
}
