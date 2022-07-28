package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.ZeroBuffer;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.collect.Lists;
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
    public static final class ChunkData extends AbstractWrapper {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getLevelChunkPacketDataClass();

        private static final FieldAccessor BLOCK_ENTITIES_DATA_ACCESSOR;
        private static final FieldAccessor HEIGHTMAPS_ACCESSOR;
        private static final FieldAccessor BUFFER_ACCESSOR;

        static {
            BLOCK_ENTITIES_DATA_ACCESSOR = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getField(FuzzyFieldContract.newBuilder().typeExact(List.class).build()));
            HEIGHTMAPS_ACCESSOR = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getField(FuzzyFieldContract.newBuilder().typeExact(MinecraftReflection.getNBTCompoundClass()).build()));
            BUFFER_ACCESSOR = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getField(FuzzyFieldContract.newBuilder().typeExact(byte[].class).build()));
        }

        public ChunkData(Object handle) {
            super(HANDLE_TYPE);

            setHandle(handle);
        }

        /**
         * The heightmap of this chunk.
         *
         * @return an NBT-Tag
         */
        public NbtCompound getHeightmapsTag() {
            return NbtFactory.fromNMSCompound(HEIGHTMAPS_ACCESSOR.get(handle));
        }

        /**
         * Sets the heightmap tag of this chunk.
         *
         * @param heightmapsTag the new heightmaps tag.
         */
        public void setHeightmapsTag(NbtCompound heightmapsTag) {
            HEIGHTMAPS_ACCESSOR.set(handle, NbtFactory.fromBase(heightmapsTag).getHandle());
        }

        /**
         * The actual structural data of this chunk as bytes.
         *
         * @return a byte array containing the chunks structural data.
         */
        public ByteBuf getBuffer() {
            return Unpooled.wrappedBuffer((byte[]) BUFFER_ACCESSOR.get(handle));
        }

        /**
         * Sets the structural data of this chunk using a {@link ByteBuf}.
         *
         * @param buffer the new buffer.
         */
        public void setBuffer(ByteBuf buffer) {
            byte[] extracted = new byte[buffer.readableBytes()];

            buffer.readBytes(extracted);

            setBuffer(extracted);
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
         * @return a mutable list containing {@link  BlockEntityInfo}
         */
        public List<BlockEntityInfo> getBlockEntityInfo() {
            //noinspection StaticPseudoFunctionalStyleMethod
            return Lists.transform((List<?>) BLOCK_ENTITIES_DATA_ACCESSOR.get(handle), BlockEntityInfo::new);
        }

        private static ConstructorAccessor levelChunkPacketDataConstructor;

        /**
         * Creates a new wrapper using predefined values.
         *
         * @param heightmapsTag   the heightmaps tag
         * @param buffer          the buffer
         * @param blockEntityInfo a list of wrapped block entities
         * @return a newly created wrapper
         */
        public static ChunkData fromValues(NbtCompound heightmapsTag, byte[] buffer, List<BlockEntityInfo> blockEntityInfo) {
            if(levelChunkPacketDataConstructor == null) {
                levelChunkPacketDataConstructor = Accessors.getConstructorAccessor(HANDLE_TYPE, MinecraftReflection.getPacketDataSerializerClass(), int.class, int.class);
            }

            ConstructorAccessor trickySerializer = StructureCache.getTrickDataSerializerOrNull();

            if(trickySerializer == null) {
                throw new UnsupportedOperationException("TrickySerializer is not supported");
            }

            Object instance = levelChunkPacketDataConstructor.invoke(trickySerializer.invoke(new ZeroBuffer()), 0, 0);

            BUFFER_ACCESSOR.set(instance, buffer);
            HEIGHTMAPS_ACCESSOR.set(instance, NbtFactory.fromBase(heightmapsTag).getHandle());

            for (BlockEntityInfo entityInfo : blockEntityInfo) {
                //noinspection unchecked
                ((List) BLOCK_ENTITIES_DATA_ACCESSOR.get(instance)).add(entityInfo.getHandle());
            }

            return new ChunkData(instance);
        }
    }

    /**
     * Wrapper for ClientboundLightUpdatePacketData
     */
    public static class LightData extends AbstractWrapper {

        private static final Class<?> HANDLE_TYPE = MinecraftReflection.getLightUpdatePacketDataClass();

        private static final List<FieldAccessor> BIT_SET_ACCESSORS;
        private static final List<FieldAccessor> BYTE_ARRAY_LIST_ACCESSORS;

        private static final FieldAccessor TRUST_EDGES_ACCESSOR;

        static {
            BIT_SET_ACCESSORS = asFieldAccessors(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getFieldList(FuzzyFieldContract.newBuilder().typeExact(BitSet.class).build()));
            BYTE_ARRAY_LIST_ACCESSORS = asFieldAccessors(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getFieldList(FuzzyFieldContract.newBuilder().typeExact(List.class).build()));
            TRUST_EDGES_ACCESSOR = Accessors.getFieldAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
                    .getField(FuzzyFieldContract.newBuilder().typeExact(boolean.class).build()));
        }

        private static List<FieldAccessor> asFieldAccessors(List<Field> fields) {
            List<FieldAccessor> accessors = new ArrayList<>(fields.size());

            for (Field field : fields) {
                accessors.add(Accessors.getFieldAccessor(field));
            }

            return accessors;
        }

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
            return (BitSet) BIT_SET_ACCESSORS.get(0).get(handle);
        }

        /**
         * Sets the sky light mask
         *
         * @param skyYMask the new mask
         */
        public void setSkyYMask(BitSet skyYMask) {
            BIT_SET_ACCESSORS.get(0).set(handle, skyYMask);
        }

        /**
         * The block light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getBlockYMask() {
            return (BitSet) BIT_SET_ACCESSORS.get(1).get(handle);
        }

        /**
         * Sets the block light mask
         *
         * @param blockYMask the new mask
         */
        public void setBlockYMask(BitSet blockYMask) {
            BIT_SET_ACCESSORS.get(1).set(handle, blockYMask);
        }

        /**
         * The empty sky light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getEmptySkyYMask() {
            return (BitSet) BIT_SET_ACCESSORS.get(2).get(handle);
        }

        /**
         * Sets the empty sky light mask
         *
         * @param emptySkyYMask the new mask
         */
        public void setEmptySkyYMask(BitSet emptySkyYMask) {
            BIT_SET_ACCESSORS.get(2).set(handle, emptySkyYMask);
        }

        /**
         * The empty block light mask.
         *
         * @return a {@link BitSet}
         */
        public BitSet getEmptyBlockYMask() {
            return (BitSet) BIT_SET_ACCESSORS.get(3).get(handle);
        }

        /**
         * Sets the empty block light mask
         *
         * @param emptyBlockYMask the new mask
         */
        public void setEmptyBlockYMask(BitSet emptyBlockYMask) {
            BIT_SET_ACCESSORS.get(3).set(handle, emptyBlockYMask);
        }

        /**
         * A mutable list of sky light arrays.
         *
         * @return a mutable list of byte arrays.
         */
        public List<byte[]> getSkyUpdates() {
            //noinspection unchecked
            return (List<byte[]>) BYTE_ARRAY_LIST_ACCESSORS.get(0).get(handle);
        }

        /**
         * A mutable list of block light arrays.
         *
         * @return a mutable list of byte arrays.
         */
        public List<byte[]> getBlockUpdates() {
            //noinspection unchecked
            return (List<byte[]>) BYTE_ARRAY_LIST_ACCESSORS.get(1).get(handle);
        }

        /**
         * Whether edges can be trusted for light updates or not.
         *
         * @return {@code true} if edges can be trusted, {@code false} otherwise.
         */
        public boolean isTrustEdges() {
            return (boolean) TRUST_EDGES_ACCESSOR.get(handle);
        }

        /**
         * Sets whether edges can be trusted for light updates or not.
         *
         * @param trustEdges the new value
         */
        public void setTrustEdges(boolean trustEdges) {
            TRUST_EDGES_ACCESSOR.set(handle, trustEdges);
        }

        private static ConstructorAccessor lightUpdatePacketDataConstructor;

        public static LightData fromValues(BitSet skyYMask, BitSet blockYMask, BitSet emptySkyYMask, BitSet emptyBlockYMask,
                                           List<byte[]> skyUpdates, List<byte[]> blockUpdates, boolean trustEdges) {
            if (lightUpdatePacketDataConstructor == null) {
                lightUpdatePacketDataConstructor = Accessors.getConstructorAccessor(HANDLE_TYPE, MinecraftReflection.getPacketDataSerializerClass(), int.class, int.class);
            }

            Object instance = lightUpdatePacketDataConstructor.invoke(MinecraftReflection.getPacketDataSerializer(new ZeroBuffer()), 0, 0);

            TRUST_EDGES_ACCESSOR.set(instance, trustEdges);
            BIT_SET_ACCESSORS.get(0).set(instance, skyYMask);
            BIT_SET_ACCESSORS.get(1).set(instance, blockYMask);
            BIT_SET_ACCESSORS.get(2).set(instance, emptySkyYMask);
            BIT_SET_ACCESSORS.get(3).set(instance, emptyBlockYMask);
            BYTE_ARRAY_LIST_ACCESSORS.get(0).set(instance, skyUpdates);
            BYTE_ARRAY_LIST_ACCESSORS.get(1).set(instance, blockUpdates);

            return new LightData(instance);
        }
    }

    /**
     * Represents an immutable BlockEntityInfo in the MAP_CHUNK packet.
     *
     * @author Etrayed
     */
    public static class BlockEntityInfo extends AbstractWrapper {

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
            return WrappedRegistry.getBlockEntityTypeRegistry().getKey(TYPE_ACCESSOR.get(handle));
        }

        /**
         * Sets the registry key of the block entity type
         *
         * @param typeKey the new block entity type key
         */
        public void setTypeKey(MinecraftKey typeKey) {
            TYPE_ACCESSOR.set(handle, WrappedRegistry.getBlockEntityTypeRegistry().get(typeKey));
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
         * @param sectionX       the section-relative X-coordinate of the block entity.
         * @param sectionZ       the section-relative Z-coordinate of the block entity.
         * @param y              the Y-coordinate of the block entity.
         * @param typeKey        the minecraft key of the block entity type.
         */
        public static BlockEntityInfo fromValues(int sectionX, int sectionZ, int y, MinecraftKey typeKey) {
            return fromValues(sectionX, sectionZ, y, typeKey, null);
        }

        private static ConstructorAccessor blockEntityInfoConstructor;

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
            if (blockEntityInfoConstructor == null) {
                blockEntityInfoConstructor = Accessors.getConstructorAccessor(HANDLE_TYPE, int.class, int.class,
                        MinecraftReflection.getBlockEntityTypeClass(), MinecraftReflection.getNBTCompoundClass());
            }

            return new BlockEntityInfo(blockEntityInfoConstructor.invoke(
                    sectionX << 4 | sectionZ,
                    y,
                    WrappedRegistry.getBlockEntityTypeRegistry().get(typeKey),
                    additionalData == null ? null : NbtFactory.fromBase(additionalData).getHandle()
            ));
        }
    }
}
