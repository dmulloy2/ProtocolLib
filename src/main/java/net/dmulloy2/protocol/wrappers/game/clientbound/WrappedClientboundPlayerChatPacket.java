package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedChatTypeBound;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerChatPacket} (game phase, clientbound).
 */
public class WrappedClientboundPlayerChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CHAT;

    private static final Class<?> SIGNED_MESSAGE_BODY_PACKED_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.SignedMessageBody$Packed");
    private static final Class<?> LAST_SEEN_MESSAGES_PACKED_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.LastSeenMessages$Packed");
    private static final Class<?> MESSAGE_SIGNATURE_PACKED_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.MessageSignature$Packed");
    private static final Class<?> FILTER_MASK_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.FilterMask");
    private static final Class<?> FILTER_MASK_TYPE_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.FilterMask$Type");
    private static final Class<?> CHAT_TYPE_BOUND_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.ChatType$Bound");

    private static final ConstructorAccessor MESSAGE_SIGNATURE_PACKED_CONSTRUCTOR =
            Accessors.getConstructorAccessor(
                    MESSAGE_SIGNATURE_PACKED_CLASS,
                    int.class,
                    MinecraftReflection.getMessageSignatureClass());
    private static final MethodAccessor GET_MESSAGE_SIGNATURE_PACKED_ID =
            Accessors.getMethodAccessor(MESSAGE_SIGNATURE_PACKED_CLASS, "id");
    private static final MethodAccessor GET_MESSAGE_SIGNATURE_PACKED_FULL_SIGNATURE =
            Accessors.getMethodAccessor(MESSAGE_SIGNATURE_PACKED_CLASS, "fullSignature");

    private static final EquivalentConverter<MessageSignaturePacked> MESSAGE_SIGNATURE_PACKED_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(MessageSignaturePacked specific) {
                    return MESSAGE_SIGNATURE_PACKED_CONSTRUCTOR.invoke(
                            specific.id,
                            BukkitConverters.getWrappedMessageSignatureConverter()
                                    .getGeneric(specific.fullSignature));
                }

                @Override
                public MessageSignaturePacked getSpecific(Object generic) {
                    return new MessageSignaturePacked(
                            (int) GET_MESSAGE_SIGNATURE_PACKED_ID.invoke(generic),
                            BukkitConverters.getWrappedMessageSignatureConverter()
                                    .getSpecific(GET_MESSAGE_SIGNATURE_PACKED_FULL_SIGNATURE.invoke(generic)));
                }

                @Override
                public Class<MessageSignaturePacked> getSpecificType() {
                    return MessageSignaturePacked.class;
                }
            });

    private static final ConstructorAccessor LAST_SEEN_MESSAGES_PACKED_CONSTRUCTOR =
            Accessors.getConstructorAccessor(LAST_SEEN_MESSAGES_PACKED_CLASS, List.class);
    private static final MethodAccessor GET_LAST_SEEN_MESSAGES_PACKED_ENTRIES =
            Accessors.getMethodAccessor(LAST_SEEN_MESSAGES_PACKED_CLASS, "entries");

    private static final EquivalentConverter<LastSeenMessagesPacked> LAST_SEEN_MESSAGES_PACKED_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(LastSeenMessagesPacked specific) {
                    return LAST_SEEN_MESSAGES_PACKED_CONSTRUCTOR.invoke(
                            BukkitConverters.getListConverter(MESSAGE_SIGNATURE_PACKED_CONVERTER)
                                    .getGeneric(specific.entries));
                }

                @Override
                public LastSeenMessagesPacked getSpecific(Object generic) {
                    return new LastSeenMessagesPacked(
                            BukkitConverters.getListConverter(MESSAGE_SIGNATURE_PACKED_CONVERTER)
                                    .getSpecific(GET_LAST_SEEN_MESSAGES_PACKED_ENTRIES.invoke(generic)));
                }

                @Override
                public Class<LastSeenMessagesPacked> getSpecificType() {
                    return LastSeenMessagesPacked.class;
                }
            });

    private static final ConstructorAccessor SIGNED_MESSAGE_BODY_PACKED_CONSTRUCTOR =
            Accessors.getConstructorAccessor(
                    SIGNED_MESSAGE_BODY_PACKED_CLASS,
                    String.class,
                    Instant.class,
                    long.class,
                    LAST_SEEN_MESSAGES_PACKED_CLASS);
    private static final MethodAccessor GET_SIGNED_MESSAGE_BODY_PACKED_CONTENT =
            Accessors.getMethodAccessor(SIGNED_MESSAGE_BODY_PACKED_CLASS, "content");
    private static final MethodAccessor GET_SIGNED_MESSAGE_BODY_PACKED_TIMESTAMP =
            Accessors.getMethodAccessor(SIGNED_MESSAGE_BODY_PACKED_CLASS, "timeStamp");
    private static final MethodAccessor GET_SIGNED_MESSAGE_BODY_PACKED_SALT =
            Accessors.getMethodAccessor(SIGNED_MESSAGE_BODY_PACKED_CLASS, "salt");
    private static final MethodAccessor GET_SIGNED_MESSAGE_BODY_PACKED_LAST_SEEN =
            Accessors.getMethodAccessor(SIGNED_MESSAGE_BODY_PACKED_CLASS, "lastSeen");

    private static final EquivalentConverter<SignedMessageBodyPacked> SIGNED_MESSAGE_BODY_PACKED_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(SignedMessageBodyPacked specific) {
                    return SIGNED_MESSAGE_BODY_PACKED_CONSTRUCTOR.invoke(
                            specific.content,
                            specific.timeStamp,
                            specific.salt,
                            LAST_SEEN_MESSAGES_PACKED_CONVERTER.getGeneric(specific.lastSeen));
                }

                @Override
                public SignedMessageBodyPacked getSpecific(Object generic) {
                    return new SignedMessageBodyPacked(
                            (String) GET_SIGNED_MESSAGE_BODY_PACKED_CONTENT.invoke(generic),
                            (Instant) GET_SIGNED_MESSAGE_BODY_PACKED_TIMESTAMP.invoke(generic),
                            (long) GET_SIGNED_MESSAGE_BODY_PACKED_SALT.invoke(generic),
                            LAST_SEEN_MESSAGES_PACKED_CONVERTER
                                    .getSpecific(GET_SIGNED_MESSAGE_BODY_PACKED_LAST_SEEN.invoke(generic)));
                }

                @Override
                public Class<SignedMessageBodyPacked> getSpecificType() {
                    return SignedMessageBodyPacked.class;
                }
            });

    private static final ConstructorAccessor FILTER_MASK_CONSTRUCTOR =
            Accessors.getConstructorAccessor(FILTER_MASK_CLASS, BitSet.class, FILTER_MASK_TYPE_CLASS);
    private static final FieldAccessor FILTER_MASK_PASS_THROUGH =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(FILTER_MASK_CLASS, "PASS_THROUGH", FILTER_MASK_CLASS));
    private static final FieldAccessor FILTER_MASK_FULLY_FILTERED =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(FILTER_MASK_CLASS, "FULLY_FILTERED", FILTER_MASK_CLASS));
    private static final FieldAccessor FILTER_MASK_MASK =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(FILTER_MASK_CLASS, "mask", BitSet.class));
    private static final FieldAccessor FILTER_MASK_TYPE =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(FILTER_MASK_CLASS, "type", FILTER_MASK_TYPE_CLASS));

    private static final EquivalentConverter<FilterMask> FILTER_MASK_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(FilterMask specific) {
                    return switch (specific.type) {
                        case PASS_THROUGH -> FILTER_MASK_PASS_THROUGH.get(null);
                        case FULLY_FILTERED -> FILTER_MASK_FULLY_FILTERED.get(null);
                        case PARTIALLY_FILTERED -> FILTER_MASK_CONSTRUCTOR.invoke(
                                specific.getMask(), filterMaskType(specific.type));
                    };
                }

                @Override
                public FilterMask getSpecific(Object generic) {
                    return new FilterMask(
                            FilterMaskType.valueOf(((Enum<?>) FILTER_MASK_TYPE.get(generic)).name()),
                            (BitSet) FILTER_MASK_MASK.get(generic));
                }

                @Override
                public Class<FilterMask> getSpecificType() {
                    return FilterMask.class;
                }
            });

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(UUID.class)
            .withParam(int.class)
            .withParam(MinecraftReflection.getMessageSignatureClass(), BukkitConverters.getWrappedMessageSignatureConverter())
            .withParam(SIGNED_MESSAGE_BODY_PACKED_CLASS, SIGNED_MESSAGE_BODY_PACKED_CONVERTER)
            .withParam(MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter())
            .withParam(FILTER_MASK_CLASS, FILTER_MASK_CONVERTER)
            .withParam(CHAT_TYPE_BOUND_CLASS, WrappedChatTypeBound.CONVERTER);

    public WrappedClientboundPlayerChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPlayerChatPacket(int globalIndex, int index, UUID sender, WrappedChatComponent unsignedContent, WrappedMessageSignature signature) {
        this(globalIndex, index, sender, unsignedContent, signature,
                new SignedMessageBodyPacked("", Instant.EPOCH, 0L, LastSeenMessagesPacked.empty()),
                FilterMask.passThrough(),
                defaultChatType());
    }

    public WrappedClientboundPlayerChatPacket(int globalIndex, int index, UUID sender,
            WrappedChatComponent unsignedContent, WrappedMessageSignature signature,
            SignedMessageBodyPacked body, FilterMask filterMask, WrappedChatTypeBound chatType) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(
                globalIndex,
                sender,
                index,
                signature,
                body,
                unsignedContent,
                filterMask,
                chatType)));
    }

    public WrappedClientboundPlayerChatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getGlobalIndex() {
        return handle.getIntegers().read(0);
    }

    public void setGlobalIndex(int globalIndex) {
        handle.getIntegers().write(0, globalIndex);
    }

    public int getIndex() {
        return handle.getIntegers().read(1);
    }

    public void setIndex(int index) {
        handle.getIntegers().write(1, index);
    }

    public UUID getSender() {
        return handle.getUUIDs().read(0);
    }

    public void setSender(UUID sender) {
        handle.getUUIDs().write(0, sender);
    }

    public WrappedChatComponent getUnsignedContent() {
        return handle.getChatComponents().read(0);
    }

    public void setUnsignedContent(WrappedChatComponent unsignedContent) {
        handle.getChatComponents().write(0, unsignedContent);
    }

    public WrappedMessageSignature getSignature() {
        return handle.getMessageSignatures().read(0);
    }

    public void setSignature(WrappedMessageSignature signature) {
        handle.getMessageSignatures().write(0, signature);
    }

    public SignedMessageBodyPacked getBody() {
        return handle.getModifier()
                .withType(SIGNED_MESSAGE_BODY_PACKED_CLASS, SIGNED_MESSAGE_BODY_PACKED_CONVERTER)
                .read(0);
    }

    public void setBody(SignedMessageBodyPacked body) {
        handle.getModifier()
                .withType(SIGNED_MESSAGE_BODY_PACKED_CLASS, SIGNED_MESSAGE_BODY_PACKED_CONVERTER)
                .write(0, body);
    }

    public FilterMask getFilterMask() {
        return handle.getModifier()
                .withType(FILTER_MASK_CLASS, FILTER_MASK_CONVERTER)
                .read(0);
    }

    public void setFilterMask(FilterMask filterMask) {
        handle.getModifier()
                .withType(FILTER_MASK_CLASS, FILTER_MASK_CONVERTER)
                .write(0, filterMask);
    }

    public WrappedChatTypeBound getChatType() {
        return handle.getModifier()
                .withType(CHAT_TYPE_BOUND_CLASS, WrappedChatTypeBound.CONVERTER)
                .read(0);
    }

    public void setChatType(WrappedChatTypeBound chatType) {
        handle.getModifier()
                .withType(CHAT_TYPE_BOUND_CLASS, WrappedChatTypeBound.CONVERTER)
                .write(0, chatType);
    }

    private static WrappedChatTypeBound defaultChatType() {
        return new WrappedChatTypeBound(new MinecraftKey("chat"), WrappedChatComponent.fromText(""), Optional.empty());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object filterMaskType(FilterMaskType type) {
        return Enum.valueOf((Class) FILTER_MASK_TYPE_CLASS, type.name());
    }

    /** Wrapper for {@code SignedMessageBody.Packed}. */
    public static final class SignedMessageBodyPacked {
        private final String content;
        private final Instant timeStamp;
        private final long salt;
        private final LastSeenMessagesPacked lastSeen;

        public SignedMessageBodyPacked(String content, Instant timeStamp, long salt,
                LastSeenMessagesPacked lastSeen) {
            this.content = content;
            this.timeStamp = timeStamp;
            this.salt = salt;
            this.lastSeen = lastSeen;
        }

        public String getContent() {
            return content;
        }

        public Instant getTimeStamp() {
            return timeStamp;
        }

        public long getSalt() {
            return salt;
        }

        public LastSeenMessagesPacked getLastSeen() {
            return lastSeen;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SignedMessageBodyPacked that)) {
                return false;
            }
            return salt == that.salt
                    && Objects.equals(content, that.content)
                    && Objects.equals(timeStamp, that.timeStamp)
                    && Objects.equals(lastSeen, that.lastSeen);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content, timeStamp, salt, lastSeen);
        }
    }

    /** Wrapper for {@code LastSeenMessages.Packed}. */
    public static final class LastSeenMessagesPacked {
        private final List<MessageSignaturePacked> entries;

        public LastSeenMessagesPacked(List<MessageSignaturePacked> entries) {
            this.entries = List.copyOf(entries);
        }

        public static LastSeenMessagesPacked empty() {
            return new LastSeenMessagesPacked(List.of());
        }

        public List<MessageSignaturePacked> getEntries() {
            return new ArrayList<>(entries);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LastSeenMessagesPacked that)) {
                return false;
            }
            return Objects.equals(entries, that.entries);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entries);
        }
    }

    /** Wrapper for {@code MessageSignature.Packed}. */
    public static final class MessageSignaturePacked {
        public static final int FULL_SIGNATURE = -1;

        private final int id;
        private final WrappedMessageSignature fullSignature;

        public MessageSignaturePacked(int id, WrappedMessageSignature fullSignature) {
            this.id = id;
            this.fullSignature = fullSignature;
        }

        public MessageSignaturePacked(int id) {
            this(id, null);
        }

        public MessageSignaturePacked(WrappedMessageSignature fullSignature) {
            this(FULL_SIGNATURE, fullSignature);
        }

        public int getId() {
            return id;
        }

        public WrappedMessageSignature getFullSignature() {
            return fullSignature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MessageSignaturePacked that)) {
                return false;
            }
            return id == that.id
                    && Arrays.equals(bytes(fullSignature), bytes(that.fullSignature));
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hash(id) + Arrays.hashCode(bytes(fullSignature));
        }

        private static byte[] bytes(WrappedMessageSignature signature) {
            return signature != null ? signature.getBytes() : null;
        }
    }

    public enum FilterMaskType {
        PASS_THROUGH, FULLY_FILTERED, PARTIALLY_FILTERED
    }

    /** Wrapper for {@code FilterMask}. */
    public static final class FilterMask {
        private final FilterMaskType type;
        private final BitSet mask;

        public FilterMask(FilterMaskType type, BitSet mask) {
            this.type = type;
            this.mask = (BitSet) mask.clone();
        }

        public static FilterMask passThrough() {
            return new FilterMask(FilterMaskType.PASS_THROUGH, new BitSet());
        }

        public static FilterMask fullyFiltered() {
            return new FilterMask(FilterMaskType.FULLY_FILTERED, new BitSet());
        }

        public static FilterMask partiallyFiltered(BitSet mask) {
            return new FilterMask(FilterMaskType.PARTIALLY_FILTERED, mask);
        }

        public FilterMaskType getType() {
            return type;
        }

        public BitSet getMask() {
            return (BitSet) mask.clone();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FilterMask that)) {
                return false;
            }
            return type == that.type && Objects.equals(mask, that.mask);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, mask);
        }
    }
}
