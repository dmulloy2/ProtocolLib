package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.util.BitSet;
import java.util.Objects;

/**
 * Wrapper for {@code LastSeenMessages.Update}.
 */
public final class WrappedLastSeenMessagesUpdate {

    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getMinecraftClass("network.chat.LastSeenMessages$Update");
    private static final ConstructorAccessor CONSTRUCTOR = Accessors.getConstructorAccessor(
            HANDLE_TYPE,
            int.class,
            BitSet.class,
            byte.class);
    private static final MethodAccessor GET_OFFSET = Accessors.getMethodAccessor(HANDLE_TYPE, "offset");
    private static final MethodAccessor GET_ACKNOWLEDGED = Accessors.getMethodAccessor(HANDLE_TYPE, "acknowledged");
    private static final MethodAccessor GET_CHECKSUM = Accessors.getMethodAccessor(HANDLE_TYPE, "checksum");

    public static final EquivalentConverter<WrappedLastSeenMessagesUpdate> CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(WrappedLastSeenMessagesUpdate specific) {
                    return CONSTRUCTOR.invoke(specific.offset, specific.getAcknowledged(), specific.checksum);
                }

                @Override
                public WrappedLastSeenMessagesUpdate getSpecific(Object generic) {
                    return new WrappedLastSeenMessagesUpdate(
                            (int) GET_OFFSET.invoke(generic),
                            (BitSet) GET_ACKNOWLEDGED.invoke(generic),
                            (byte) GET_CHECKSUM.invoke(generic));
                }

                @Override
                public Class<WrappedLastSeenMessagesUpdate> getSpecificType() {
                    return WrappedLastSeenMessagesUpdate.class;
                }
            });

    private final int offset;
    private final BitSet acknowledged;
    private final byte checksum;

    public WrappedLastSeenMessagesUpdate(int offset, BitSet acknowledged, byte checksum) {
        this.offset = offset;
        this.acknowledged = (BitSet) acknowledged.clone();
        this.checksum = checksum;
    }

    public int getOffset() {
        return offset;
    }

    public BitSet getAcknowledged() {
        return (BitSet) acknowledged.clone();
    }

    public byte getChecksum() {
        return checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WrappedLastSeenMessagesUpdate that)) {
            return false;
        }
        return offset == that.offset
                && checksum == that.checksum
                && Objects.equals(acknowledged, that.acknowledged);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, acknowledged, checksum);
    }

    @Override
    public String toString() {
        return "WrappedLastSeenMessagesUpdate{"
                + "offset=" + offset
                + ", acknowledged=" + acknowledged
                + ", checksum=" + checksum
                + '}';
    }
}
