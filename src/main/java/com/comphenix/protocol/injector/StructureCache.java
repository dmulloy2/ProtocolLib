/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package com.comphenix.protocol.injector;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.packet.KnownPacketData;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.reflect.instances.InstanceCreator;
import com.comphenix.protocol.utility.ByteBuddyFactory;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftRegistryAccess;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.ZeroBuffer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedStreamCodec;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Caches structure modifiers.
 *
 * @author Kristian
 */
public class StructureCache {

    // Structure modifiers
    private static final Map<Class<?>, Optional<Supplier<Object>>> CACHED_INSTANCE_CREATORS = new ConcurrentHashMap<>();
    private static final Map<PacketType, StructureModifier<Object>> STRUCTURE_MODIFIER_CACHE = new ConcurrentHashMap<>();

    // packet data serializer which always returns an empty nbt tag compound
    private static final Object TRICK_INIT_LOCK = new Object();
    private static boolean TRICK_TRIED = false;

    private static Function<ByteBuf, Object> TRICKED_DATA_SERIALIZER_BASE;
    private static Supplier<Object> TRICKED_DATA_SERIALIZER_JSON;

    /**
     * @deprecated Renamed to {@link #newInstance(Class)}.
     */
    @Deprecated
    public static Object newPacket(Class<?> packetClass) {
        return newInstance(packetClass);
    }

    public static boolean canCreateInstance(Class<?> clazz) {
        Optional<Supplier<Object>> creator = CACHED_INSTANCE_CREATORS.computeIfAbsent(clazz, x ->
            Optional.ofNullable(determineBestCreator(clazz)));
        return creator.isPresent();
    }

    public static Object newInstance(Class<?> clazz) {
        Optional<Supplier<Object>> creator = CACHED_INSTANCE_CREATORS.computeIfAbsent(clazz, x ->
            Optional.ofNullable(determineBestCreator(clazz)));
        if (!creator.isPresent()) {
            throw new IllegalArgumentException("Cannot create instance of " + clazz);
        }
        return creator.get().get();
    }

    static Supplier<Object> determineBestCreator(Class<?> clazz) {
        // certain packets are singletons which can't really be created
        if (MinecraftReflection.isPacketClass(clazz)) {
            FuzzyReflection fuzzy = FuzzyReflection.fromClass(clazz, false);
            List<Field> singletons = fuzzy.getFieldList(FuzzyFieldContract.newBuilder()
                .typeExact(clazz)
                .requireModifier(Modifier.STATIC)
                .requireModifier(Modifier.PUBLIC)
                .build());
            if (singletons.size() == 1) {
                FieldAccessor accessor = Accessors.getFieldAccessor(singletons.get(0));
                try {
                    accessor.get(null);
                    return () -> accessor.get(null);
                } catch (Exception ignored) {
                }
            }
        }

        try {
            InstanceCreator creator = InstanceCreator.forClass(clazz);
            if (creator.get() != null) {
                return creator;
            }
        } catch (Exception ignored) {
        }

        WrappedStreamCodec streamCodec = PacketRegistry.getStreamCodec(clazz);

        // use the new stream codec for versions above 1.20.5 if possible
        if (streamCodec != null && tryInitTrickDataSerializer()) {
            try {
                // first try with the base accessor
                Object serializer = TRICKED_DATA_SERIALIZER_BASE.apply(new ZeroBuffer());
                streamCodec.decode(serializer); // throwaway instance, for testing

                // method is working
                return () -> streamCodec.decode(serializer);
            } catch (Exception ex) {
                try {
                    byte[] data;
                    if (clazz.equals(PacketType.Play.Server.MAP_CHUNK.getPacketClass())) {
                        data = KnownPacketData.MAP_CHUNK;
                    } else if (clazz.equals(PacketType.Play.Server.SCOREBOARD_OBJECTIVE.getPacketClass())) {
                        data = KnownPacketData.SCOREBOARD_OBJECTIVE;
                    } else {
                        throw ex;
                    }

                    Object serializer = TRICKED_DATA_SERIALIZER_BASE.apply(Unpooled.copiedBuffer(data));
                    streamCodec.decode(serializer);

                    return () -> {
                        ((ByteBuf) serializer).readerIndex(0);
                        return streamCodec.decode(serializer);
                    };
                } catch (Exception ignored1) {
                    try {
                        // try with the json accessor
                        Object serializer = TRICKED_DATA_SERIALIZER_JSON.get();
                        streamCodec.decode(serializer); // throwaway instance, for testing

                        // method is working
                        return () -> streamCodec.decode(serializer);
                    } catch (Exception ignored2) {
                        // shrug, fall back to default behaviour
                    }
                }
            }
        }

        // prefer construction via PacketDataSerializer constructor on 1.17 and above
        if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
            ConstructorAccessor serializerAccessor = Accessors.getConstructorAccessorOrNull(
                clazz,
                MinecraftReflection.getPacketDataSerializerClass());
            if (serializerAccessor != null) {
                // check if the method is possible
                if (tryInitTrickDataSerializer()) {
                    try {
                        // first try with the base accessor
                        Object serializer = TRICKED_DATA_SERIALIZER_BASE.apply(new ZeroBuffer());
                        serializerAccessor.invoke(serializer); // throwaway instance, for testing

                        // method is working
                        return () -> serializerAccessor.invoke(serializer);
                    } catch (Exception ignored) {
                        try {
                            // try with the json accessor
                            Object serializer = TRICKED_DATA_SERIALIZER_JSON.get();
                            serializerAccessor.invoke(serializer); // throwaway instance, for testing

                            // method is working
                            return () -> serializerAccessor.invoke(serializer);
                        } catch (Exception ignored1) {
                            // shrug, fall back to default behaviour
                        }
                    }
                }
            }
        }

        Object instance = DefaultInstances.DEFAULT.create(clazz);
        if (instance == null) {
            return null;
        }

        return () -> DefaultInstances.DEFAULT.create(clazz);
    }

    /**
     * Creates an empty Minecraft packet of the given type.
     *
     * @param type - packet type.
     * @return Created packet.
     */
    public static Object newPacket(PacketType type) {
        return newPacket(PacketRegistry.getPacketClassFromType(type));
    }

    /**
     * Retrieve a cached structure modifier given a packet type.
     *
     * @param packetType - packet type.
     * @return A structure modifier.
     * @deprecated use {@link #getStructure(PacketType)} instead.
     */
    @Deprecated
    public static StructureModifier<Object> getStructure(Class<?> packetType) {
        // Get the ID from the class
        PacketType type = PacketRegistry.getPacketType(packetType);
        Preconditions.checkNotNull(type, "No packet type associated with " + packetType);
        return getStructure(type);
    }

    /**
     * Retrieve a cached structure modifier for the given packet type.
     *
     * @param packetType - packet type.
     * @return A structure modifier.
     */
    public static StructureModifier<Object> getStructure(final PacketType packetType) {
        Preconditions.checkNotNull(packetType, "type cannot be null");

        return STRUCTURE_MODIFIER_CACHE.computeIfAbsent(packetType, type -> {
            Class<?> packetClass = PacketRegistry.getPacketClassFromType(type);

            // We need to map the Bundle Delimiter to the synthetic bundle packet which contains a list of all packets in a bundle
            if (MinecraftReflection.isBundleDelimiter(packetClass)) {
                packetClass = MinecraftReflection.getPackedBundlePacketClass().get();
            }

            return new StructureModifier<>(packetClass, MinecraftReflection.getPacketClass(), true);
        });
    }

    /**
     * Returns a new mocked null data serializer instance, if possible.
     *
     * @return a new null data serializer instance.
     */
    public static Object newNullDataSerializer() {
        tryInitTrickDataSerializer();
        return TRICKED_DATA_SERIALIZER_BASE.apply(new ZeroBuffer());
    }

    static void initTrickDataSerializer() {
        Optional<Class<?>> registryByteBuf = MinecraftReflection.getRegistryFriendlyByteBufClass();

        // create an empty instance of a nbt tag compound / text compound that we can re-use when needed
        Object textCompound = WrappedChatComponent.fromText("").getHandle();
        Object compound = Accessors.getConstructorAccessor(MinecraftReflection.getNBTCompoundClass()).invoke();

        // base builder which intercepts a few methods
        DynamicType.Builder<?> baseBuilder = ByteBuddyFactory.getInstance()
            .createSubclass(registryByteBuf.orElse(MinecraftReflection.getPacketDataSerializerClass()))
            .name(MinecraftMethods.class.getPackage().getName() + ".ProtocolLibTricksNmsDataSerializerBase")
            .method(ElementMatchers.takesArguments(MinecraftReflection.getNBTReadLimiterClass())
                .and(ElementMatchers.returns(ElementMatchers.isSubTypeOf(MinecraftReflection.getNBTBaseClass()))))
            .intercept(FixedValue.value(compound))
            .method(ElementMatchers.returns(MinecraftReflection.getIChatBaseComponentClass()))
            .intercept(FixedValue.value(textCompound))
            .method(ElementMatchers.returns(PublicKey.class).and(ElementMatchers.takesNoArguments()))
            .intercept(FixedValue.nullValue());
        Class<?> serializerBase = baseBuilder.make()
            .load(ByteBuddyFactory.getInstance().getClassLoader(), Default.INJECTION)
            .getLoaded();

        if (registryByteBuf.isPresent()) {
            ConstructorAccessor accessor = Accessors.getConstructorAccessor(FuzzyReflection.fromClass(serializerBase, true).getConstructor(FuzzyMethodContract.newBuilder()
                    .parameterDerivedOf(ByteBuf.class)
                    .parameterDerivedOf(MinecraftReflection.getRegistryAccessClass())
                    .build()));
            TRICKED_DATA_SERIALIZER_BASE = (buf) -> accessor.invoke(buf, MinecraftRegistryAccess.get());
        } else {
            ConstructorAccessor accessor = Accessors.getConstructorAccessor(serializerBase, ByteBuf.class);
            TRICKED_DATA_SERIALIZER_BASE = accessor::invoke;
        }

        //xtended builder which intercepts the read string method as well
        Class<?> withStringIntercept = baseBuilder
            .name(MinecraftMethods.class.getPackage().getName() + ".ProtocolLibTricksNmsDataSerializerJson")
            .method(ElementMatchers.returns(String.class).and(ElementMatchers.takesArguments(int.class)))
            .intercept(FixedValue.value("{}"))
            .make()
            .load(ByteBuddyFactory.getInstance().getClassLoader(), Default.INJECTION)
            .getLoaded();

        if (registryByteBuf.isPresent()) {
            ConstructorAccessor accessor = Accessors.getConstructorAccessor(FuzzyReflection.fromClass(withStringIntercept).getConstructor(FuzzyMethodContract.newBuilder()
                    .parameterDerivedOf(ByteBuf.class)
                    .parameterDerivedOf(MinecraftReflection.getRegistryAccessClass())
                    .build()));
            TRICKED_DATA_SERIALIZER_JSON = () -> accessor.invoke(new ZeroBuffer(), MinecraftRegistryAccess.get());
        } else {
            ConstructorAccessor accessor = Accessors.getConstructorAccessor(withStringIntercept, ByteBuf.class);
            TRICKED_DATA_SERIALIZER_JSON = () -> accessor.invoke(new ZeroBuffer());
        }
    }

    /**
     * Creates a packet data serializer sub-class if needed to allow the fixed read of a NbtTagCompound because of a
     * null check in the MapChunk packet constructor.
     */
    public static boolean tryInitTrickDataSerializer() {
        if (TRICK_TRIED) {
            return TRICKED_DATA_SERIALIZER_BASE != null;
        }

        synchronized (TRICK_INIT_LOCK) {
            if (TRICK_TRIED) {
                return TRICKED_DATA_SERIALIZER_BASE != null;
            }

            try {
                initTrickDataSerializer();
                return true;
            } catch (Exception ignored) {
            } finally {
                TRICK_TRIED = true;
            }

            return false;
        }
    }
}
