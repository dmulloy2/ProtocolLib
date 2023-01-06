package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

public class WrappedVillagerData extends AbstractWrapper implements ClonableWrapper {
    private static final Class<?> NMS_CLASS = MinecraftReflection.getNullableNMS(
            "world.entity.npc.VillagerData","VillagerData");
    private static final Class<?> TYPE_CLASS = MinecraftReflection.getNullableNMS(
            "world.entity.npc.VillagerType", "VillagerType");
    private static final Class<?> PROF_CLASS = MinecraftReflection.getNullableNMS(
            "world.entity.npc.VillagerProfession", "VillagerProfession");

    private static EquivalentConverter<Type> TYPE_CONVERTER;
    private static EquivalentConverter<Profession> PROF_CONVERTER;

    static {
        if (NMS_CLASS != null) {
            TYPE_CONVERTER = new EnumWrappers.FauxEnumConverter<>(Type.class, TYPE_CLASS);
            PROF_CONVERTER = new EnumWrappers.FauxEnumConverter<>(Profession.class, PROF_CLASS);
        }
    }

    public enum Type {
        DESERT, JUNGLE, PLAINS, SAVANNA, SNOW, SWAMP, TAIGA
    }

    public enum Profession {
        NONE, ARMORER, BUTCHER, CARTOGRAPHER, CLERIC, FARMER, FISHERMAN,
        FLETCHER, LEATHERWORKER, LIBRARIAN, MASON, NITWIT, SHEPHERD,
        TOOLSMITH, WEAPONSMITH
    }

    private StructureModifier<Object> modifier;

    private WrappedVillagerData(Object handle) {
        super(NMS_CLASS);
        setHandle(handle);

        modifier = new StructureModifier<>(NMS_CLASS).withTarget(handle);
    }

    public static WrappedVillagerData fromHandle(Object handle) {
        return new WrappedVillagerData(handle);
    }

    private static ConstructorAccessor CONSTRUCTOR;

    public static WrappedVillagerData fromValues(Type type, Profession profession, int level) {
        Object genericType = TYPE_CONVERTER.getGeneric(type);
        Object genericProf = PROF_CONVERTER.getGeneric(profession);

        if (CONSTRUCTOR == null) {
            CONSTRUCTOR = Accessors.getConstructorAccessor(NMS_CLASS, TYPE_CLASS, PROF_CLASS, int.class);
        }

        Object handle = CONSTRUCTOR.invoke(genericType, genericProf, level);
        return fromHandle(handle);
    }

    public static Class<?> getNmsClass() {
        return NMS_CLASS;
    }

    public int getLevel() {
        return modifier.<Integer>withType(int.class).read(0);
    }

    public Type getType() {
        return modifier.withType(TYPE_CLASS, TYPE_CONVERTER).read(0);
    }

    public Profession getProfession() {
        return modifier.withType(PROF_CLASS, PROF_CONVERTER).read(0);
    }

    @Override
    public WrappedVillagerData deepClone() {
        return WrappedVillagerData.fromValues(getType(), getProfession(), getLevel());
    }
}
