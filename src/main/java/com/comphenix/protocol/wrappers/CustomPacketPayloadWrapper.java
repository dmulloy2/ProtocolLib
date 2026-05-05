package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.ByteBuddyFactory;
import com.comphenix.protocol.utility.ByteBuddyGenerated;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.StreamSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * A wrapper for the CustomPacketPayload class in 1.20.2. Due to the nature of the class, not all types are supported
 * by default. Constructing a new wrapper instance will give out a handle to a completely new implemented type, that
 * allows to set a key and some kind of data of any choice.
 * <p>
 * Note that constructing this class from a generic handle is only possible for the spigot-specific UnknownPayload type.
 * All other payloads should be accessed via a structure modifier directly.
 *
 * @author Pasqual Koschmieder
 */
public final class CustomPacketPayloadWrapper {

    private static final Class<?> MINECRAFT_KEY_CLASS;
    private static final Class<?> CUSTOM_PACKET_PAYLOAD_CLASS;

    private static final ConstructorAccessor PAYLOAD_WRAPPER_CONSTRUCTOR;

    private static final MethodAccessor GET_ID_PAYLOAD_METHOD;
    /**
     * When non-null, all payloads use this interface write method; otherwise resolved per concrete class (Paper 26+).
     */
    private static final MethodAccessor SERIALIZE_PAYLOAD_METHOD;

    private static final ConcurrentHashMap<Class<?>, Optional<MethodAccessor>> SERIALIZE_BY_CONCRETE_PAYLOAD =
            new ConcurrentHashMap<>();

    private static volatile Class<?> serializerArgumentClassCache;

    /**
     * Constructor for {@code CustomPacketPayload.Id} (record); null on versions where the payload key is returned directly.
     */
    private static final ConstructorAccessor PAYLOAD_ID_RECORD_CONSTRUCTOR;

    private static final EquivalentConverter<CustomPacketPayloadWrapper> CONVERTER;

    static {
        try {
            MINECRAFT_KEY_CLASS = MinecraftReflection.getMinecraftKeyClass();
            CUSTOM_PACKET_PAYLOAD_CLASS = MinecraftReflection.getMinecraftClass("network.protocol.common.custom.CustomPacketPayload");

            KeyAccessorResolution keyResolution = resolvePayloadKeyAccessor(CUSTOM_PACKET_PAYLOAD_CLASS, MINECRAFT_KEY_CLASS);
            GET_ID_PAYLOAD_METHOD = keyResolution.accessor;

            Method serializePayloadData = null;
            try {
                serializePayloadData = FuzzyReflection.fromClass(CUSTOM_PACKET_PAYLOAD_CLASS).getMethod(FuzzyMethodContract.newBuilder()
                        .banModifier(Modifier.STATIC)
                        .returnTypeVoid()
                        .parameterCount(1)
                        .parameterDerivedOf(ByteBuf.class, 0)
                        .build());
            } catch (RuntimeException ignored) {
                // 1.21.11+ / Paper 26.x: CustomPacketPayload has no void(ByteBuf); use FriendlyByteBuf-like arg on impl
            }
            if (serializePayloadData == null) {
                serializePayloadData = findWritablePayloadMethod(CUSTOM_PACKET_PAYLOAD_CLASS);
            }
            SERIALIZE_PAYLOAD_METHOD = serializePayloadData != null ? Accessors.getMethodAccessor(serializePayloadData) : null;

            Class<?> payloadIdClass = findDeclaredNested(CUSTOM_PACKET_PAYLOAD_CLASS, "Id");
            ConstructorAccessor idRecordCtor = null;
            if (payloadIdClass != null) {
                try {
                    Constructor<?> idCtor = payloadIdClass.getConstructor(MINECRAFT_KEY_CLASS);
                    idRecordCtor = Accessors.getConstructorAccessor(idCtor);
                } catch (NoSuchMethodException ignored) {
                    // older mappings without Id record
                }
            }
            PAYLOAD_ID_RECORD_CONSTRUCTOR = idRecordCtor;

            boolean useIdRecordReturn = keyResolution.viaPayloadIdRecord && idRecordCtor != null;
            if (keyResolution.viaPayloadIdRecord && idRecordCtor == null) {
                throw new IllegalStateException("CustomPacketPayload exposes Id record but no Id(Identifier) constructor was found");
            }

            Constructor<?> payloadWrapperConstructor = makePayloadWrapper(payloadIdClass, useIdRecordReturn);
            PAYLOAD_WRAPPER_CONSTRUCTOR = Accessors.getConstructorAccessor(payloadWrapperConstructor);

            CONVERTER = new EquivalentConverter<CustomPacketPayloadWrapper>() {
                @Override
                public Object getGeneric(CustomPacketPayloadWrapper specific) {
                    return specific.newHandle();
                }

                @Override
                public CustomPacketPayloadWrapper getSpecific(Object generic) {
                    return fromUnknownPayload(generic);
                }

                @Override
                public Class<CustomPacketPayloadWrapper> getSpecificType() {
                    return CustomPacketPayloadWrapper.class;
                }
            };
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static Constructor<?> makePayloadWrapper(Class<?> payloadIdClass, boolean useIdRecordReturn) throws Exception {
        net.bytebuddy.dynamic.DynamicType.Builder<?> buddy = new ByteBuddy()
                .subclass(Object.class)
                .name("com.comphenix.protocol.wrappers.ProtocolLibCustomPacketPayload")
                .implement(CUSTOM_PACKET_PAYLOAD_CLASS, ByteBuddyGenerated.class)
                .defineField("payload", byte[].class, Modifier.PRIVATE | Modifier.FINAL)
                .defineField("id", MinecraftReflection.getMinecraftKeyClass(), Modifier.PRIVATE | Modifier.FINAL)
                .defineConstructor(Modifier.PUBLIC)
                .withParameters(MinecraftReflection.getMinecraftKeyClass(), byte[].class)
                .intercept(MethodCall.invoke(Object.class.getConstructor())
                        .andThen(FieldAccessor.ofField("id").setsArgumentAt(0))
                        .andThen(FieldAccessor.ofField("payload").setsArgumentAt(1)));

        if (useIdRecordReturn) {
            buddy = buddy
                    .method(ElementMatchers.returns(payloadIdClass).and(ElementMatchers.takesNoArguments()))
                    .intercept(MethodDelegation.to(IdRecordInterceptor.class));
        } else {
            buddy = buddy
                    .method(ElementMatchers.returns(MinecraftReflection.getMinecraftKeyClass()).and(ElementMatchers.takesNoArguments()))
                    .intercept(FieldAccessor.ofField("id"));
        }

        return buddy
                .method(ElementMatchers.returns(void.class).and(ElementMatchers.takesArguments(MinecraftReflection.getPacketDataSerializerClass())))
                .intercept(MethodDelegation.to(CustomPacketPayloadInterceptionHandler.class))
                .make()
                .load(ByteBuddyFactory.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded()
                .getConstructor(MinecraftReflection.getMinecraftKeyClass(), byte[].class);
    }

    private static final class ChainedMethodAccessor implements MethodAccessor {
        private final MethodAccessor outer;
        private final MethodAccessor inner;
        private final Method representative;

        private ChainedMethodAccessor(MethodAccessor outer, MethodAccessor inner, Method representative) {
            this.outer = outer;
            this.inner = inner;
            this.representative = representative;
        }

        @Override
        public Object invoke(Object target, Object... args) {
            return this.inner.invoke(this.outer.invoke(target, args));
        }

        @Override
        public Method getMethod() {
            return this.representative;
        }
    }

    private static final class KeyAccessorResolution {
        final MethodAccessor accessor;
        final boolean viaPayloadIdRecord;

        private KeyAccessorResolution(MethodAccessor accessor, boolean viaPayloadIdRecord) {
            this.accessor = accessor;
            this.viaPayloadIdRecord = viaPayloadIdRecord;
        }
    }

    private static KeyAccessorResolution resolvePayloadKeyAccessor(Class<?> payloadClass, Class<?> keyClass) {
        try {
            Method direct = FuzzyReflection.fromClass(payloadClass).getMethod(FuzzyMethodContract.newBuilder()
                    .banModifier(Modifier.STATIC)
                    .returnTypeExact(keyClass)
                    .parameterCount(0)
                    .build());
            return new KeyAccessorResolution(Accessors.getMethodAccessor(direct), false);
        } catch (RuntimeException ignored) {
            // 1.21.11+ / Paper 26.x: getId() returns CustomPacketPayload.Id, Identifier is nested
        }

        for (Method outer : collectCandidateKeyBridgeMethods(payloadClass)) {
            Class<?> ret = outer.getReturnType();
            if (keyClass.isAssignableFrom(ret)) {
                return new KeyAccessorResolution(Accessors.getMethodAccessor(outer), false);
            }
            Method inner = findSingleZeroArgInstanceMethodReturning(ret, keyClass);
            if (inner != null) {
                MethodAccessor outerAcc = Accessors.getMethodAccessor(outer);
                MethodAccessor innerAcc = Accessors.getMethodAccessor(inner);
                return new KeyAccessorResolution(new ChainedMethodAccessor(outerAcc, innerAcc, outer), true);
            }
        }

        throw new IllegalArgumentException("Unable to resolve CustomPacketPayload -> " + keyClass.getName() + " accessor");
    }

    private static List<Method> collectCandidateKeyBridgeMethods(Class<?> payloadClass) {
        List<Method> idNamedReturns = new ArrayList<>();
        List<Method> other = new ArrayList<>();
        for (Method method : payloadClass.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                continue;
            }
            Class<?> ret = method.getReturnType();
            if (ret == void.class || ret == Void.class) {
                continue;
            }
            if ("Id".equals(ret.getSimpleName())) {
                idNamedReturns.add(method);
            } else {
                other.add(method);
            }
        }
        idNamedReturns.addAll(other);
        return idNamedReturns;
    }

    private static Method findSingleZeroArgInstanceMethodReturning(Class<?> holder, Class<?> keyClass) {
        List<Method> matches = new ArrayList<>();
        for (Method method : holder.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (!keyClass.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            matches.add(method);
        }
        for (Method method : matches) {
            if ("id".equals(method.getName())) {
                return method;
            }
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private static Class<?> findDeclaredNested(Class<?> outer, String simpleName) {
        for (Class<?> nested : outer.getDeclaredClasses()) {
            if (simpleName.equals(nested.getSimpleName())) {
                return nested;
            }
        }
        return null;
    }

    private static Class<?> serializerArgumentClass() {
        Class<?> c = serializerArgumentClassCache;
        if (c == null) {
            synchronized (CustomPacketPayloadWrapper.class) {
                c = serializerArgumentClassCache;
                if (c == null) {
                    Object sample = MinecraftReflection.getPacketDataSerializer(Unpooled.buffer());
                    serializerArgumentClassCache = c = sample.getClass();
                }
            }
        }
        return c;
    }

    /**
     * Breadth-first search for {@code void write(FriendlyByteBuf-like)} (or legacy {@code ByteBuf}) on a type and its supertypes.
     */
    private static Method findWritablePayloadMethod(Class<?> start) {
        Deque<Class<?>> queue = new ArrayDeque<>();
        Set<Class<?>> seen = new HashSet<>();
        queue.add(start);
        Class<?> serializerArg = serializerArgumentClass();
        while (!queue.isEmpty()) {
            Class<?> type = queue.poll();
            if (type == null || type == Object.class || !seen.add(type)) {
                continue;
            }
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (method.getReturnType() != void.class) {
                    continue;
                }
                if (method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> p0 = method.getParameterTypes()[0];
                if (p0 == Object.class) {
                    continue;
                }
                // Must accept what getPacketDataSerializer(netty Buffer) returns (e.g. FriendlyByteBuf)
                if (!p0.isAssignableFrom(serializerArg)) {
                    continue;
                }
                method.setAccessible(true);
                return method;
            }
            if (type.getSuperclass() != null) {
                queue.add(type.getSuperclass());
            }
            for (Class<?> itf : type.getInterfaces()) {
                queue.add(itf);
            }
        }
        return null;
    }

    private static MethodAccessor resolveSerializeForPayloadInstance(Object payload) {
        if (SERIALIZE_PAYLOAD_METHOD != null) {
            return SERIALIZE_PAYLOAD_METHOD;
        }
        return SERIALIZE_BY_CONCRETE_PAYLOAD
                .computeIfAbsent(payload.getClass(), CustomPacketPayloadWrapper::lookupSerializeAccessorForConcrete)
                .orElse(null);
    }

    private static Optional<MethodAccessor> lookupSerializeAccessorForConcrete(Class<?> concretePayload) {
        Method method = findWritablePayloadMethod(concretePayload);
        return Optional.ofNullable(method == null ? null : Accessors.getMethodAccessor(method));
    }

    private static byte[] extractPayloadBodyWithoutDirectBuf(Object payload) {
        MethodAccessor serialize = resolveSerializeForPayloadInstance(payload);
        if (serialize != null) {
            ByteBuf buffer = Unpooled.buffer();
            Object serializer = MinecraftReflection.getPacketDataSerializer(buffer);
            serialize.invoke(payload, serializer);
            return StreamSerializer.getDefault().getBytesAndRelease(buffer);
        }
        StructureModifier<Object> mod = new StructureModifier<>(payload.getClass()).withTarget(payload);
        try {
            return (byte[]) mod.withType(byte[].class).read(0);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot extract CustomPacketPayload body from " + payload.getClass().getName(), e);
        }
    }

    // ====== api methods ======

    /**
     * The wrapped payload in the message.
     */
    private final byte[] payload;
    /**
     * The wrapped key of the message.
     */
    private final MinecraftKey id;
    /**
     * The generic id of the message, lazy initialized when needed.
     */
    private Object genericId;

    /**
     * Constructs a new payload wrapper instance using the given message payload and id.
     *
     * @param payload the payload of the message.
     * @param id      the id of the message.
     * @throws NullPointerException if the given payload or id is null.
     */
    public CustomPacketPayloadWrapper(byte[] payload, MinecraftKey id) {
        this.payload = Objects.requireNonNull(payload, "payload");
        this.id = Objects.requireNonNull(id, "id");
    }

    /**
     * Get the CustomPacketPayload class that is backing this wrapper (available since Minecraft 1.20.2).
     *
     * @return the CustomPacketPayload class.
     */
    public static Class<?> getCustomPacketPayloadClass() {
        return CUSTOM_PACKET_PAYLOAD_CLASS;
    }

    /**
     * Get a converter to convert this wrapper to a generic handle and an UnknownPayload type to this wrapper.
     *
     * @return a converter for this wrapper.
     */
    public static EquivalentConverter<CustomPacketPayloadWrapper> getConverter() {
        return CONVERTER;
    }

    /**
     * Constructs this wrapper from any CustomPayload type.
     * <p>
     * Note: the buffer of the given payload (if any) will <strong>NOT</strong> be released by this operation. Make sure
     * to release the buffer manually if you discard the packet to prevent memory leaks.
     *
     * @param payload the instance of the custom payload to convert to this wrapper.
     * @return a wrapper holding the minecraft key and payload of the given custom payload instance.
     */
    public static CustomPacketPayloadWrapper fromUnknownPayload(Object payload) {
        Object messageId = GET_ID_PAYLOAD_METHOD.invoke(payload);
        MinecraftKey id = MinecraftKey.getConverter().getSpecific(messageId);

        // we read and retain the underlying buffer in case the class uses a buffer to store the data
        // this way, when passing the packet to further handling, the buffer is not released and can be re-used
        StructureModifier<Object> modifier = new StructureModifier<>(payload.getClass()).withTarget(payload);
        byte[] messagePayload = modifier.withType(ByteBuf.class).optionRead(0)
                .map(buffer -> {
                    ByteBuf buf = (ByteBuf) buffer;
                    byte[] data = StreamSerializer.getDefault().getBytesAndRelease(buf.markReaderIndex().retain());
                    buf.resetReaderIndex();
                    return data;
                })
                .orElseGet(() -> extractPayloadBodyWithoutDirectBuf(payload));

        return new CustomPacketPayloadWrapper(messagePayload, id);
    }

    /**
     * Get the generic id of the wrapped message id.
     *
     * @return the generic key id.
     */
    private Object getGenericId() {
        if (this.genericId == null) {
            this.genericId = MinecraftKey.getConverter().getGeneric(this.id);
        }
        return this.genericId;
    }

    /**
     * Get the message payload of this wrapper. Changes made to the returned array will be reflected into this wrapper.
     *
     * @return the message payload.
     */
    public byte[] getPayload() {
        return this.payload;
    }

    /**
     * Get the message id of this wrapper.
     *
     * @return the message id of this wrapper.
     */
    public MinecraftKey getId() {
        return this.id;
    }

    /**
     * Constructs a <strong>NEW</strong> handle instance of a payload wrapper to use in a CustomPayload packet.
     *
     * @return a new payload wrapper instance using the provided message id and payload.
     */
    public Object newHandle() {
        return PAYLOAD_WRAPPER_CONSTRUCTOR.invoke(this.getGenericId(), this.payload);
    }

    /**
     * Builds {@link CustomPacketPayload}'s Id record from the stored Mojang key for {@link #newHandle()}.
     */
    @SuppressWarnings("unused")
    static final class IdRecordInterceptor {
        @RuntimeType
        public static Object intercept(@FieldValue("id") Object nmsKey) {
            return PAYLOAD_ID_RECORD_CONSTRUCTOR.invoke(nmsKey);
        }
    }

    /**
     * Handles interception of the ProtocolLib specific CustomPayloadWrapper implementation. For internal use only.
     */
    @SuppressWarnings("unused")
    static final class CustomPacketPayloadInterceptionHandler {
        public static void intercept(@FieldValue("payload") byte[] payload, @Argument(0) Object packetBuffer) {
            ((ByteBuf) packetBuffer).writeBytes(payload);
        }
    }
}
