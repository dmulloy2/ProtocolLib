package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.utility.MinecraftReflection;

public class WrappedVillagerData extends AbstractWrapper implements ClonableWrapper {
    private static final Class<?> NMS_CLASS = MinecraftReflection.getMinecraftClass("VillagerData");
    private static final Class<?> TYPE_CLASS = MinecraftReflection.getMinecraftClass("VillagerType");
    private static final Class<?> PROF_CLASS = MinecraftReflection.getMinecraftClass("VillagerProfession");

    private static final EquivalentConverter<Type> TYPE_CONVERTER = new EnumWrappers.FauxEnumConverter<>(Type.class, TYPE_CLASS);
    private static final EquivalentConverter<Profession> PROF_CONVERTER = new EnumWrappers.FauxEnumConverter<>(Profession.class, PROF_CLASS);

    public enum Type {
        DESERT, JUNGLE, PLAINS, SAVANNA, SNOW, SWAMP, TAIGA;
    }

    public enum Profession {
        NONE, ARMORER, BUTCHER, CARTOGRAPHER, CLERIC, FARMER, FISHERMAN,
        FLETCHER, LEATHERWORKER, LIBRARIAN, MASON, NITWIT, SHEPHERD,
        TOOLSMITH, WEAPONSMITH;
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

    public static WrappedVillagerData fromValues(Type type, Profession profession, int level) {
        Object genericType = TYPE_CONVERTER.getGeneric(type);
        Object genericProf = PROF_CONVERTER.getGeneric(profession);

        Object handle = Accessors.getConstructorAccessor(NMS_CLASS, TYPE_CLASS, PROF_CLASS, int.class)
                .invoke(genericType, genericProf, level);
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
