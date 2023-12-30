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
    private static final MethodAccessor SERIALIZE_PAYLOAD_METHOD;

    private static final EquivalentConverter<CustomPacketPayloadWrapper> CONVERTER;

    static {
        try {
            MINECRAFT_KEY_CLASS = MinecraftReflection.getMinecraftKeyClass();
            CUSTOM_PACKET_PAYLOAD_CLASS = MinecraftReflection.getMinecraftClass("network.protocol.common.custom.CustomPacketPayload");

            Method getPayloadId = FuzzyReflection.fromClass(CUSTOM_PACKET_PAYLOAD_CLASS).getMethod(FuzzyMethodContract.newBuilder()
                    .banModifier(Modifier.STATIC)
                    .returnTypeExact(MINECRAFT_KEY_CLASS)
                    .parameterCount(0)
                    .build());
            GET_ID_PAYLOAD_METHOD = Accessors.getMethodAccessor(getPayloadId);

            Method serializePayloadData = FuzzyReflection.fromClass(CUSTOM_PACKET_PAYLOAD_CLASS).getMethod(FuzzyMethodContract.newBuilder()
                    .banModifier(Modifier.STATIC)
                    .returnTypeVoid()
                    .parameterCount(1)
                    .parameterDerivedOf(ByteBuf.class, 0)
                    .build());
            SERIALIZE_PAYLOAD_METHOD = Accessors.getMethodAccessor(serializePayloadData);

            Constructor<?> payloadWrapperConstructor = makePayloadWrapper();
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
                .orElseGet(() -> {
                    ByteBuf buffer = Unpooled.buffer();
                    Object serializer = MinecraftReflection.getPacketDataSerializer(buffer);
                    SERIALIZE_PAYLOAD_METHOD.invoke(payload, serializer);
                    return StreamSerializer.getDefault().getBytesAndRelease(buffer);
                });

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
     * Handles interception of the ProtocolLib specific CustomPayloadWrapper implementation. For internal use only.
     */
    @SuppressWarnings("unused")
    static final class CustomPacketPayloadInterceptionHandler {
        public static void intercept(@FieldValue("payload") byte[] payload, @Argument(0) Object packetBuffer) {
            ((ByteBuf) packetBuffer).writeBytes(payload);
        }
    }
}
