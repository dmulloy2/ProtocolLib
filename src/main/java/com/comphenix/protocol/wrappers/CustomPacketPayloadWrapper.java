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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * A wrapper for the CustomPacketPayload class in 1.20.2. Due to the nature of the class, not all types are supported
 * by default. Constructing a new wrapper instance will give out a handle to a completely new implemented type, that
 * allows to set a key and some kind of data of any choice.
 * <p>
 * Constructing this class from a generic handle requires either retained raw byte data, such as {@code DiscardedPayload},
 * or a writable serialization method on the concrete payload type.
 *
 * @author Pasqual Koschmieder
 */
public final class CustomPacketPayloadWrapper {

    private static final Class<?> MINECRAFT_KEY_CLASS;
    private static final Class<?> CUSTOM_PACKET_PAYLOAD_CLASS;

    private static final ConstructorAccessor PAYLOAD_WRAPPER_CONSTRUCTOR;

    private static final MethodAccessor GET_ID_PAYLOAD_METHOD;
    private static final MethodAccessor GET_ID_HOLDER_PAYLOAD_METHOD;
    private static final MethodAccessor GET_ID_PAYLOAD_HOLDER_METHOD;
    private static final MethodAccessor SERIALIZE_PAYLOAD_METHOD;
    private static final Class<?> PAYLOAD_SERIALIZER_CLASS;
    private static final ConcurrentHashMap<Class<?>, Optional<MethodAccessor>> SERIALIZE_PAYLOAD_METHODS =
            new ConcurrentHashMap<>();

    /** True when running on the nested-ID API where DiscardedPayload is used instead of the ByteBuddy proxy. */
    private static final boolean USE_DISCARDED_PAYLOAD;
    private static final boolean DISCARDED_PAYLOAD_USES_BYTE_ARRAY;

    private static final EquivalentConverter<CustomPacketPayloadWrapper> CONVERTER;

    static {
        try {
            MINECRAFT_KEY_CLASS = MinecraftReflection.getMinecraftKeyClass();
            CUSTOM_PACKET_PAYLOAD_CLASS = MinecraftReflection.getMinecraftClass("network.protocol.common.custom.CustomPacketPayload");

            Method getPayloadId = null;
            Method getPayloadIdHolder = null;
            Method getPayloadIdFromHolder = null;
            boolean useDiscardedPayload = false;
            try {
                getPayloadId = FuzzyReflection.fromClass(CUSTOM_PACKET_PAYLOAD_CLASS).getMethod(FuzzyMethodContract.newBuilder()
                        .banModifier(Modifier.STATIC)
                        .returnTypeExact(MINECRAFT_KEY_CLASS)
                        .parameterCount(0)
                        .build());
            } catch (IllegalArgumentException ignored) {
                // Paper 26: the payload exposes its identifier through one intermediate record.
                Method[] accessors = findChainedIdAccessors(CUSTOM_PACKET_PAYLOAD_CLASS, MINECRAFT_KEY_CLASS);
                getPayloadIdHolder = accessors[0];
                getPayloadIdFromHolder = accessors[1];
                useDiscardedPayload = true;
            }
            GET_ID_PAYLOAD_METHOD = getPayloadId != null ? Accessors.getMethodAccessor(getPayloadId) : null;
            GET_ID_HOLDER_PAYLOAD_METHOD = getPayloadIdHolder != null
                    ? Accessors.getMethodAccessor(getPayloadIdHolder) : null;
            GET_ID_PAYLOAD_HOLDER_METHOD = getPayloadIdFromHolder != null
                    ? Accessors.getMethodAccessor(getPayloadIdFromHolder) : null;
            USE_DISCARDED_PAYLOAD = useDiscardedPayload;

            Method serializePayloadData = null;
            try {
                serializePayloadData = FuzzyReflection.fromClass(CUSTOM_PACKET_PAYLOAD_CLASS).getMethod(FuzzyMethodContract.newBuilder()
                        .banModifier(Modifier.STATIC)
                        .returnTypeVoid()
                        .parameterCount(1)
                        .parameterDerivedOf(ByteBuf.class, 0)
                        .build());
            } catch (IllegalArgumentException ignored) {
                // Paper 26 declares the write method on each concrete payload instead.
            }
            SERIALIZE_PAYLOAD_METHOD = serializePayloadData != null
                    ? Accessors.getMethodAccessor(serializePayloadData) : null;
            if (serializePayloadData == null) {
                ByteBuf buffer = Unpooled.buffer();
                try {
                    PAYLOAD_SERIALIZER_CLASS = MinecraftReflection.getPacketDataSerializer(buffer).getClass();
                } finally {
                    if (buffer.refCnt() > 0) {
                        buffer.release();
                    }
                }
            } else {
                PAYLOAD_SERIALIZER_CLASS = null;
            }

            Constructor<?> payloadWrapperConstructor;
            boolean discardedPayloadUsesByteArray = false;
            if (useDiscardedPayload) {
                Class<?> discardedPayloadClass = MinecraftReflection.getMinecraftClass(
                        "network.protocol.common.custom.DiscardedPayload");
                try {
                    payloadWrapperConstructor = discardedPayloadClass.getConstructor(
                            MINECRAFT_KEY_CLASS, byte[].class);
                    discardedPayloadUsesByteArray = true;
                } catch (NoSuchMethodException exception) {
                    payloadWrapperConstructor = discardedPayloadClass.getConstructor(
                            MINECRAFT_KEY_CLASS, ByteBuf.class);
                }
            } else {
                payloadWrapperConstructor = makePayloadWrapper();
            }
            DISCARDED_PAYLOAD_USES_BYTE_ARRAY = discardedPayloadUsesByteArray;
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

    private static Method[] findChainedIdAccessors(Class<?> payloadClass, Class<?> keyClass) {
        Method payloadAccessor = null;
        Method keyAccessor = null;
        FuzzyMethodContract keyAccessorContract = FuzzyMethodContract.newBuilder()
                .banModifier(Modifier.STATIC)
                .returnTypeExact(keyClass)
                .parameterCount(0)
                .build();

        for (Method candidate : payloadClass.getMethods()) {
            if (Modifier.isStatic(candidate.getModifiers()) || candidate.getParameterCount() != 0) {
                continue;
            }

            Method candidateKeyAccessor;
            try {
                candidateKeyAccessor = FuzzyReflection.fromClass(candidate.getReturnType(), true)
                        .getMethod(keyAccessorContract);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            if (payloadAccessor != null) {
                throw new IllegalArgumentException("Multiple CustomPacketPayload identifier accessors found");
            }
            payloadAccessor = candidate;
            keyAccessor = candidateKeyAccessor;
        }

        if (payloadAccessor == null) {
            throw new IllegalArgumentException("Unable to find a CustomPacketPayload identifier accessor");
        }
        return new Method[]{payloadAccessor, keyAccessor};
    }

    private static Constructor<?> makePayloadWrapper() throws Exception {
        return new ByteBuddy()
                .subclass(Object.class)
                .name("com.comphenix.protocol.wrappers.ProtocolLibCustomPacketPayload")
                .implement(CUSTOM_PACKET_PAYLOAD_CLASS, ByteBuddyGenerated.class)
                .defineField("payload", byte[].class, Modifier.PRIVATE | Modifier.FINAL)
                .defineField("id", MinecraftReflection.getMinecraftKeyClass(), Modifier.PRIVATE | Modifier.FINAL)
                .defineConstructor(Modifier.PUBLIC)
                .withParameters(MinecraftReflection.getMinecraftKeyClass(), byte[].class)
                .intercept(MethodCall.invoke(Object.class.getConstructor())
                        .andThen(FieldAccessor.ofField("id").setsArgumentAt(0))
                        .andThen(FieldAccessor.ofField("payload").setsArgumentAt(1)))
                .method(ElementMatchers.returns(MinecraftReflection.getMinecraftKeyClass()).and(ElementMatchers.takesNoArguments()))
                .intercept(FieldAccessor.ofField("id"))
                .method(ElementMatchers.returns(void.class).and(ElementMatchers.takesArguments(MinecraftReflection.getPacketDataSerializerClass())))
                .intercept(MethodDelegation.to(CustomPacketPayloadInterceptionHandler.class))
                .make()
                .load(ByteBuddyFactory.getInstance().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded()
                .getConstructor(MinecraftReflection.getMinecraftKeyClass(), byte[].class);
    }

    private static Optional<MethodAccessor> findSerializePayloadMethod(Class<?> payloadClass) {
        Method serializeMethod = null;
        for (Method candidate : FuzzyReflection.fromClass(payloadClass, true).getMethods()) {
            if (Modifier.isStatic(candidate.getModifiers())
                    || candidate.getReturnType() != void.class
                    || candidate.getParameterCount() != 1) {
                continue;
            }

            Class<?> parameterClass = candidate.getParameterTypes()[0];
            if (parameterClass == Object.class || !parameterClass.isAssignableFrom(PAYLOAD_SERIALIZER_CLASS)) {
                continue;
            }
            if (serializeMethod != null) {
                throw new IllegalArgumentException("Multiple CustomPacketPayload serialization methods found on "
                        + payloadClass.getName());
            }
            serializeMethod = candidate;
        }

        if (serializeMethod == null) {
            return Optional.empty();
        }
        serializeMethod.setAccessible(true);
        return Optional.of(Accessors.getMethodAccessor(serializeMethod));
    }

    private static MethodAccessor getSerializePayloadMethod(Object payload) {
        if (SERIALIZE_PAYLOAD_METHOD != null) {
            return SERIALIZE_PAYLOAD_METHOD;
        }
        return SERIALIZE_PAYLOAD_METHODS
                .computeIfAbsent(payload.getClass(), CustomPacketPayloadWrapper::findSerializePayloadMethod)
                .orElse(null);
    }

    private static byte[] serializePayload(Object payload) {
        MethodAccessor serializeMethod = getSerializePayloadMethod(payload);
        if (serializeMethod == null) {
            throw new UnsupportedOperationException(
                    "Cannot extract raw custom payload bytes from " + payload.getClass().getName()
                            + ": payload has no byte array, ByteBuf, or serialization method");
        }

        ByteBuf buffer = Unpooled.buffer();
        try {
            Object serializer = MinecraftReflection.getPacketDataSerializer(buffer);
            serializeMethod.invoke(payload, serializer);
            byte[] result = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), result);
            return result;
        } finally {
            if (buffer.refCnt() > 0) {
                buffer.release();
            }
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
        MinecraftKey id = getPayloadId(payload);

        // we read and retain the underlying buffer in case the class uses a buffer to store the data
        // this way, when passing the packet to further handling, the buffer is not released and can be re-used
        StructureModifier<Object> modifier = new StructureModifier<>(payload.getClass()).withTarget(payload);
        byte[] messagePayload = (byte[]) modifier.withType(byte[].class).readSafely(0);
        if (messagePayload != null) {
            messagePayload = messagePayload.clone();
        } else {
            messagePayload = modifier.withType(ByteBuf.class).optionRead(0)
                    .map(buffer -> {
                        ByteBuf buf = (ByteBuf) buffer;
                        byte[] data = StreamSerializer.getDefault().getBytesAndRelease(buf.markReaderIndex().retain());
                        buf.resetReaderIndex();
                        return data;
                    })
                    .orElseGet(() -> serializePayload(payload));
        }

        return new CustomPacketPayloadWrapper(messagePayload, id);
    }

    private static MinecraftKey getPayloadId(Object payload) {
        Object messageId;
        if (GET_ID_PAYLOAD_METHOD != null) {
            messageId = GET_ID_PAYLOAD_METHOD.invoke(payload);
        } else {
            Object idHolder = GET_ID_HOLDER_PAYLOAD_METHOD.invoke(payload);
            messageId = GET_ID_PAYLOAD_HOLDER_METHOD.invoke(idHolder);
        }
        return MinecraftKey.getConverter().getSpecific(messageId);
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
        if (USE_DISCARDED_PAYLOAD) {
            if (DISCARDED_PAYLOAD_USES_BYTE_ARRAY) {
                return PAYLOAD_WRAPPER_CONSTRUCTOR.invoke(this.getGenericId(), this.payload);
            }
            ByteBuf buf = Unpooled.copiedBuffer(this.payload);
            return PAYLOAD_WRAPPER_CONSTRUCTOR.invoke(this.getGenericId(), buf);
        }
        return PAYLOAD_WRAPPER_CONSTRUCTOR.invoke(this.getGenericId(), this.payload);
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
