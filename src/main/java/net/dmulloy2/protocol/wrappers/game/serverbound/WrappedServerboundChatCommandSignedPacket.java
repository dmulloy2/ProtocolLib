package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedLastSeenMessagesUpdate;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChatCommandSignedPacket} (game phase, serverbound).
 */
public class WrappedServerboundChatCommandSignedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHAT_COMMAND_SIGNED;

    private static final Class<?> ARGUMENT_SIGNATURES_CLASS =
            MinecraftReflection.getMinecraftClass("commands.arguments.ArgumentSignatures");
    private static final Class<?> ARGUMENT_SIGNATURE_CLASS =
            MinecraftReflection.getMinecraftClass("commands.arguments.ArgumentSignatures$Entry");
    private static final Class<?> LAST_SEEN_MESSAGES_UPDATE_CLASS =
            MinecraftReflection.getMinecraftClass("network.chat.LastSeenMessages$Update");

    private static final ConstructorAccessor ARGUMENT_SIGNATURE_CONSTRUCTOR =
            Accessors.getConstructorAccessor(
                    ARGUMENT_SIGNATURE_CLASS,
                    String.class,
                    MinecraftReflection.getMessageSignatureClass());
    private static final MethodAccessor GET_ARGUMENT_NAME =
            Accessors.getMethodAccessor(ARGUMENT_SIGNATURE_CLASS, "name");
    private static final MethodAccessor GET_ARGUMENT_SIGNATURE =
            Accessors.getMethodAccessor(ARGUMENT_SIGNATURE_CLASS, "signature");

    private static final EquivalentConverter<ArgumentSignature> ARGUMENT_SIGNATURE_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(ArgumentSignature specific) {
                    return ARGUMENT_SIGNATURE_CONSTRUCTOR.invoke(
                            specific.name,
                            BukkitConverters.getWrappedMessageSignatureConverter().getGeneric(specific.signature));
                }

                @Override
                public ArgumentSignature getSpecific(Object generic) {
                    return new ArgumentSignature(
                            (String) GET_ARGUMENT_NAME.invoke(generic),
                            BukkitConverters.getWrappedMessageSignatureConverter()
                                    .getSpecific(GET_ARGUMENT_SIGNATURE.invoke(generic)));
                }

                @Override
                public Class<ArgumentSignature> getSpecificType() {
                    return ArgumentSignature.class;
                }
            });

    private static final ConstructorAccessor ARGUMENT_SIGNATURES_CONSTRUCTOR =
            Accessors.getConstructorAccessor(ARGUMENT_SIGNATURES_CLASS, List.class);
    private static final MethodAccessor GET_ARGUMENT_SIGNATURES =
            Accessors.getMethodAccessor(ARGUMENT_SIGNATURES_CLASS, "entries");

    private static final EquivalentConverter<ArgumentSignatures> ARGUMENT_SIGNATURES_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(ArgumentSignatures specific) {
                    return ARGUMENT_SIGNATURES_CONSTRUCTOR.invoke(
                            BukkitConverters.getListConverter(ARGUMENT_SIGNATURE_CONVERTER).getGeneric(specific.entries));
                }

                @Override
                public ArgumentSignatures getSpecific(Object generic) {
                    return new ArgumentSignatures(
                            BukkitConverters.getListConverter(ARGUMENT_SIGNATURE_CONVERTER)
                                    .getSpecific(GET_ARGUMENT_SIGNATURES.invoke(generic)));
                }

                @Override
                public Class<ArgumentSignatures> getSpecificType() {
                    return ArgumentSignatures.class;
                }
            });

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(String.class)
            .withParam(Instant.class)
            .withParam(long.class)
            .withParam(ARGUMENT_SIGNATURES_CLASS, ARGUMENT_SIGNATURES_CONVERTER)
            .withParam(LAST_SEEN_MESSAGES_UPDATE_CLASS, WrappedLastSeenMessagesUpdate.CONVERTER);

    public WrappedServerboundChatCommandSignedPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundChatCommandSignedPacket(String command, long salt, Instant timeStamp) {
        this(command, salt, timeStamp, new ArgumentSignatures(List.of()),
                new WrappedLastSeenMessagesUpdate(0, new BitSet(), (byte) 0));
    }

    public WrappedServerboundChatCommandSignedPacket(String command, long salt, Instant timeStamp,
            ArgumentSignatures argumentSignatures, WrappedLastSeenMessagesUpdate lastSeenMessages) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(command, timeStamp, salt, argumentSignatures, lastSeenMessages)));
    }

    public WrappedServerboundChatCommandSignedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getCommand() {
        return handle.getStrings().read(0);
    }

    public void setCommand(String command) {
        handle.getStrings().write(0, command);
    }

    public long getSalt() {
        return handle.getLongs().read(0);
    }

    public void setSalt(long salt) {
        handle.getLongs().write(0, salt);
    }

    public Instant getTimeStamp() {
        return handle.getInstants().read(0);
    }

    public void setTimeStamp(Instant timeStamp) {
        handle.getInstants().write(0, timeStamp);
    }

    public ArgumentSignatures getArgumentSignatures() {
        return handle.getModifier()
                .withType(ARGUMENT_SIGNATURES_CLASS, ARGUMENT_SIGNATURES_CONVERTER)
                .read(0);
    }

    public void setArgumentSignatures(ArgumentSignatures argumentSignatures) {
        handle.getModifier()
                .withType(ARGUMENT_SIGNATURES_CLASS, ARGUMENT_SIGNATURES_CONVERTER)
                .write(0, argumentSignatures);
    }

    public WrappedLastSeenMessagesUpdate getLastSeenMessages() {
        return handle.getModifier()
                .withType(LAST_SEEN_MESSAGES_UPDATE_CLASS, WrappedLastSeenMessagesUpdate.CONVERTER)
                .read(0);
    }

    public void setLastSeenMessages(WrappedLastSeenMessagesUpdate lastSeenMessages) {
        handle.getModifier()
                .withType(LAST_SEEN_MESSAGES_UPDATE_CLASS, WrappedLastSeenMessagesUpdate.CONVERTER)
                .write(0, lastSeenMessages);
    }

    /** Wrapper for {@code ArgumentSignatures}. */
    public static final class ArgumentSignatures {
        private final List<ArgumentSignature> entries;

        public ArgumentSignatures(List<ArgumentSignature> entries) {
            this.entries = List.copyOf(entries);
        }

        public List<ArgumentSignature> getEntries() {
            return new ArrayList<>(entries);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ArgumentSignatures that)) {
                return false;
            }
            return Objects.equals(entries, that.entries);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entries);
        }
    }

    /** Wrapper for {@code ArgumentSignatures.Entry}. */
    public static final class ArgumentSignature {
        private final String name;
        private final WrappedMessageSignature signature;

        public ArgumentSignature(String name, WrappedMessageSignature signature) {
            this.name = name;
            this.signature = signature;
        }

        public String getName() {
            return name;
        }

        public WrappedMessageSignature getSignature() {
            return signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ArgumentSignature that)) {
                return false;
            }
            return Objects.equals(name, that.name)
                    && Arrays.equals(bytes(signature), bytes(that.signature));
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hash(name) + Arrays.hashCode(bytes(signature));
        }

        private static byte[] bytes(WrappedMessageSignature signature) {
            return signature != null ? signature.getBytes() : null;
        }
    }
}
