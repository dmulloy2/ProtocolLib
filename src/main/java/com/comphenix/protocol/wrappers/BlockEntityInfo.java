package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Objects;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Represents an immutable BlockEntityInfo in the MAP_CHUNK packet.
 *
 * @author Etrayed
 */
public class BlockEntityInfo {

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
        BlockEntityInfo that = (BlockEntityInfo) o;
        return sectionX == that.sectionX && sectionZ == that.sectionZ && y == that.y && Objects.equal(typeKey, that.typeKey) && Objects.equal(additionalData, that.additionalData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sectionX, sectionZ, y, typeKey, additionalData);
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
                        specific.additionalData == null ? null : specific.additionalData.getHandle()
                );
            }

            @Override
            public BlockEntityInfo getSpecific(Object generic) {
                int packedXZ = (int) PACKED_XZ_ACCESSOR.get(generic);
                Object tagHandle = TAG_ACCESSOR.get(generic);
                NbtCompound compound = tagHandle == null ? null : NbtFactory.fromNMSCompound(tagHandle);

                return new BlockEntityInfo(packedXZ >> 4, packedXZ, (int) Y_ACCESSOR.get(generic),
                        WrappedRegistry.getBlockEntityTypeRegistry().getKey(TYPE_ACCESSOR.get(generic)), compound);
            }

            @Override
            public Class<BlockEntityInfo> getSpecificType() {
                return BlockEntityInfo.class;
            }
        };
    }
}
