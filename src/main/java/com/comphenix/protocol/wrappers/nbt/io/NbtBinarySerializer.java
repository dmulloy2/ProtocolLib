package com.comphenix.protocol.wrappers.nbt.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;

public class NbtBinarySerializer {

    /**
     * Retrieve a default instance of the NBT binary serializer.
     */
    public static final NbtBinarySerializer DEFAULT = new NbtBinarySerializer();
    private static final Class<?> NBT_BASE_CLASS = MinecraftReflection.getNBTBaseClass();

    /**
     * Method selected for loading/writing NBT compounds.
     */
    private static CodecMethod codecMethod;

    private static MethodAccessor getNbtLoadMethod(Class<?>... parameters) {
        Method method = getUtilityClass().getMethodByReturnTypeAndParameters("load", NBT_BASE_CLASS, parameters);
        return Accessors.getMethodAccessor(method);
    }

    private static FuzzyReflection getUtilityClass() {
        return FuzzyReflection.fromClass(MinecraftReflection.getNbtCompressedStreamToolsClass(), true);
    }

    private static CodecMethod getCodecMethod() {
        if (codecMethod == null) {
            // Save the selected method
            if (MinecraftVersion.CONFIG_PHASE_PROTOCOL_UPDATE.atOrAbove()) {
                codecMethod = new LoadMethodConfigPhaseUpdate();
            } else {
                codecMethod = new LoadMethodSkinUpdate();
            }
        }
        return codecMethod;
    }

    /**
     * Write the content of a wrapped NBT tag to a stream.
     *
     * @param <T>         Type
     * @param value       - the NBT tag to write.
     * @param destination - the destination stream.
     */
    public <T> void serialize(NbtBase<T> value, DataOutput destination) {
        getCodecMethod().writeNbt(NbtFactory.fromBase(value).getHandle(), destination);
    }

    /**
     * Load an NBT tag from a stream.
     *
     * @param <TType> Type
     * @param source  - the input stream.
     * @return An NBT tag.
     */
    public <TType> NbtWrapper<TType> deserialize(DataInput source) {
        try {
            return NbtFactory.fromNMS(getCodecMethod().loadNbt(source), null);
        } catch (Exception e) {
            throw new FieldAccessException("Unable to read NBT from " + source, e);
        }
    }

    /**
     * Load an NBT compound from a stream.
     *
     * @param source - the input stream.
     * @return An NBT compound.
     */
    public NbtCompound deserializeCompound(DataInput source) {
        // I always seem to override generics ...
        return (NbtCompound) (NbtBase<?>) this.deserialize(source);
    }

    /**
     * Load an NBT list from a stream.
     *
     * @param <T>    Type
     * @param source - the input stream.
     * @return An NBT list.
     */
    @SuppressWarnings("unchecked")
    public <T> NbtList<T> deserializeList(DataInput source) {
        return (NbtList<T>) (NbtBase<?>) this.deserialize(source);
    }

    private interface CodecMethod {

        /**
         * Load an NBT compound from a given stream.
         *
         * @param input - the input stream.
         * @return The loaded NBT compound.
         */
        Object loadNbt(DataInput input);

        /**
         * Write an NBT compound to the given stream.
         *
         * @param nbt    the nbt to write.
         * @param target the target to write the compound to.
         */
        void writeNbt(Object nbt, DataOutput target);
    }

    /**
     * Load an NBT compound from the NBTCompressedStreamTools static method since 1.7.
     */
    private static class LoadMethodSkinUpdate implements CodecMethod {

        private final Class<?> readLimitClass = MinecraftReflection.getNBTReadLimiterClass();
        private final Object readLimiter = FuzzyReflection.fromClass(this.readLimitClass).getSingleton();
        private final MethodAccessor readNbt = getNbtLoadMethod(DataInput.class, int.class, this.readLimitClass);
        private final MethodAccessor writeNBT = Accessors.getMethodAccessor(getUtilityClass().getMethodByParameters("writeNBT", MinecraftReflection.getNBTBaseClass(), DataOutput.class));

        @Override
        public Object loadNbt(DataInput input) {
            return this.readNbt.invoke(null, input, 0, this.readLimiter);
        }

        @Override
        public void writeNbt(Object nbt, DataOutput target) {
            this.writeNBT.invoke(null, nbt, target);
        }
    }

    /**
     * Load an NBT compound from the NBTCompressedStreamTools static method since 1.20.2.
     */
    private static class LoadMethodConfigPhaseUpdate implements CodecMethod {

        private final Class<?> readLimitClass = MinecraftReflection.getNBTReadLimiterClass();
        private final Object readLimiter = FuzzyReflection.fromClass(this.readLimitClass).getSingleton();

        private final MethodAccessor readNbt;
        private final MethodAccessor writeNbt;

        public LoadMethodConfigPhaseUpdate() {
            // there are now two methods with the same signature: readAnyTag/readUnnamedTag & writeAnyTag/writeUnnamedTag
            // we can only find the correct method here by using the method name... thanks Mojang
            String readNbtMethodName = MinecraftReflection.isMojangMapped()
                ? "readAnyTag"
                : "b";

            String writeNbtMethodName = MinecraftReflection.isMojangMapped()
                ? "writeAnyTag"
                : "a";

            Method readNbtMethod = getUtilityClass().getMethod(FuzzyMethodContract.newBuilder()
                    .nameExact(readNbtMethodName)
                    .returnTypeExact(MinecraftReflection.getNBTBaseClass())
                    .parameterExactArray(DataInput.class, this.readLimitClass)
                    .build());
            this.readNbt = Accessors.getMethodAccessor(readNbtMethod);

            Method writeNbtMethod = getUtilityClass().getMethod(FuzzyMethodContract.newBuilder()
                    .nameExact(writeNbtMethodName)
                    .parameterExactArray(MinecraftReflection.getNBTBaseClass(), DataOutput.class)
                    .build());
            this.writeNbt = Accessors.getMethodAccessor(writeNbtMethod);
        }

        @Override
        public Object loadNbt(DataInput input) {
            return this.readNbt.invoke(null, input, this.readLimiter);
        }

        @Override
        public void writeNbt(Object nbt, DataOutput target) {
            this.writeNbt.invoke(null, nbt, target);
        }
    }
}
