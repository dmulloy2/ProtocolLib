package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetStructureBlockPacket} (game phase, serverbound).
 *
 * <p>NMS field order: pos, updateType, mode, name, offset, size, mirror, rotation, data,
 * ignoreEntities, strict, showAir, showBoundingBox, integrity, seed
 */
public class WrappedServerboundSetStructureBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.STRUCT;

    /** Mirrors {@code StructureBlockEntity.UpdateType}. Constants must match NMS names exactly. */
    public enum UpdateType { UPDATE_DATA, SAVE_AREA, LOAD_AREA, SCAN_AREA }

    /** Mirrors {@code StructureMode}. Constants must match NMS names exactly. */
    public enum StructureMode { SAVE, LOAD, CORNER, DATA }

    /** Mirrors {@code Mirror}. Constants must match NMS names exactly. */
    public enum Mirror { NONE, LEFT_RIGHT, FRONT_BACK }

    /** Mirrors {@code Rotation}. Constants must match NMS names exactly. */
    public enum Rotation { NONE, CLOCKWISE_90, CLOCKWISE_180, COUNTERCLOCKWISE_90 }

    // Locate each NMS enum class by its simple name, not by field index,
    // so this survives minor field reordering across versions.
    private static final Class<?> UPDATE_TYPE_NMS_CLASS = findEnumField("UpdateType");
    private static final Class<?> STRUCTURE_MODE_NMS_CLASS = findEnumField("StructureMode");
    private static final Class<?> MIRROR_NMS_CLASS = findEnumField("Mirror");
    private static final Class<?> ROTATION_NMS_CLASS = findEnumField("Rotation");
    // Vec3i — the size field; find it by its declared type name (Vec3i, not BlockPos)
    private static final Class<?> VEC3I_NMS_CLASS = findFieldByTypeName("Vec3i");
    // Global (declaration-order) index of the size field among non-static fields.
    // BlockPos pos(0), updateType(1), mode(2), name(3), offset(4), size(5), ...
    private static final int SIZE_FIELD_INDEX = computeSizeFieldIndex();

    private static Class<?> findEnumField(String simpleClassName) {
        return Arrays.stream(TYPE.getPacketClass().getDeclaredFields())
                .map(Field::getType)
                .filter(t -> t.isEnum() && t.getSimpleName().equals(simpleClassName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Enum field " + simpleClassName + " not found in " + TYPE.getPacketClass().getSimpleName()));
    }

    private static Class<?> findFieldByTypeName(String simpleClassName) {
        return Arrays.stream(TYPE.getPacketClass().getDeclaredFields())
                .map(Field::getType)
                .filter(t -> t.getSimpleName().equals(simpleClassName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Field of type " + simpleClassName + " not found"));
    }

    private static int computeSizeFieldIndex() {
        Field[] fields = Arrays.stream(TYPE.getPacketClass().getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toArray(Field[]::new);
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getType().getSimpleName().equals("Vec3i")) return i;
        }
        throw new IllegalStateException("Vec3i size field not found");
    }

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(UPDATE_TYPE_NMS_CLASS, new EnumWrappers.EnumConverter<>(UPDATE_TYPE_NMS_CLASS, UpdateType.class))
            .withParam(STRUCTURE_MODE_NMS_CLASS, new EnumWrappers.EnumConverter<>(STRUCTURE_MODE_NMS_CLASS, StructureMode.class))
            .withParam(String.class)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(VEC3I_NMS_CLASS, BlockPosition.getConverter())
            .withParam(MIRROR_NMS_CLASS, new EnumWrappers.EnumConverter<>(MIRROR_NMS_CLASS, Mirror.class))
            .withParam(ROTATION_NMS_CLASS, new EnumWrappers.EnumConverter<>(ROTATION_NMS_CLASS, Rotation.class))
            .withParam(String.class)
            .withParam(boolean.class)
            .withParam(boolean.class)
            .withParam(boolean.class)
            .withParam(boolean.class)
            .withParam(float.class)
            .withParam(long.class);

    public WrappedServerboundSetStructureBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    /**
     * Creates a fully-initialised packet via {@code EquivalentConstructor}.
     * {@code size} is passed as a {@link BlockPosition} (which extends Vec3i).
     */
    public WrappedServerboundSetStructureBlockPacket(BlockPosition pos, UpdateType updateType,
            StructureMode mode, String name, BlockPosition offset, BlockPosition size,
            Mirror mirror, Rotation rotation, String data,
            boolean ignoreEntities, boolean strict, boolean showAir, boolean showBoundingBox,
            float integrity, long seed) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(
                pos, updateType, mode, name, offset, size,
                mirror, rotation, data,
                ignoreEntities, strict, showAir, showBoundingBox,
                integrity, seed)));
    }

    public WrappedServerboundSetStructureBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public UpdateType getUpdateType() {
        return handle.getEnumModifier(UpdateType.class, UPDATE_TYPE_NMS_CLASS).read(0);
    }

    public void setUpdateType(UpdateType updateType) {
        handle.getEnumModifier(UpdateType.class, UPDATE_TYPE_NMS_CLASS).write(0, updateType);
    }

    public StructureMode getMode() {
        return handle.getEnumModifier(StructureMode.class, STRUCTURE_MODE_NMS_CLASS).read(0);
    }

    public void setMode(StructureMode mode) {
        handle.getEnumModifier(StructureMode.class, STRUCTURE_MODE_NMS_CLASS).write(0, mode);
    }

    public String getName() {
        return handle.getStrings().read(0);
    }

    public void setName(String name) {
        handle.getStrings().write(0, name);
    }

    public BlockPosition getOffset() {
        return handle.getBlockPositionModifier().read(1);
    }

    public void setOffset(BlockPosition offset) {
        handle.getBlockPositionModifier().write(1, offset);
    }

    /**
     * Returns the structure size as a {@link BlockPosition}.
     * Internally stored as {@code Vec3i}; {@code BlockPos} is a subtype so the
     * conversion is lossless.
     */
    public BlockPosition getSize() {
        return BlockPosition.getConverter().getSpecific(handle.getModifier().read(SIZE_FIELD_INDEX));
    }

    /** Sets the structure size (passed as {@link BlockPosition}, stored as {@code Vec3i}). */
    public void setSize(BlockPosition size) {
        handle.getModifier().write(SIZE_FIELD_INDEX, BlockPosition.getConverter().getGeneric(size));
    }

    public Mirror getMirror() {
        return handle.getEnumModifier(Mirror.class, MIRROR_NMS_CLASS).read(0);
    }

    public void setMirror(Mirror mirror) {
        handle.getEnumModifier(Mirror.class, MIRROR_NMS_CLASS).write(0, mirror);
    }

    public Rotation getRotation() {
        return handle.getEnumModifier(Rotation.class, ROTATION_NMS_CLASS).read(0);
    }

    public void setRotation(Rotation rotation) {
        handle.getEnumModifier(Rotation.class, ROTATION_NMS_CLASS).write(0, rotation);
    }

    public String getData() {
        return handle.getStrings().read(1);
    }

    public void setData(String data) {
        handle.getStrings().write(1, data);
    }

    public boolean isIgnoreEntities() {
        return handle.getBooleans().read(0);
    }

    public void setIgnoreEntities(boolean ignoreEntities) {
        handle.getBooleans().write(0, ignoreEntities);
    }

    public boolean isStrict() {
        return handle.getBooleans().read(1);
    }

    public void setStrict(boolean strict) {
        handle.getBooleans().write(1, strict);
    }

    public boolean isShowAir() {
        return handle.getBooleans().read(2);
    }

    public void setShowAir(boolean showAir) {
        handle.getBooleans().write(2, showAir);
    }

    public boolean isShowBoundingBox() {
        return handle.getBooleans().read(3);
    }

    public void setShowBoundingBox(boolean showBoundingBox) {
        handle.getBooleans().write(3, showBoundingBox);
    }

    public float getIntegrity() {
        return handle.getFloat().read(0);
    }

    public void setIntegrity(float integrity) {
        handle.getFloat().write(0, integrity);
    }

    public long getSeed() {
        return handle.getLongs().read(0);
    }

    public void setSeed(long seed) {
        handle.getLongs().write(0, seed);
    }
}
