package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDebugSamplePacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code long[] sample} – array of debug timing samples</li>
 *   <li>{@code DebugSampleType debugSampleType} – the type of debug data being sampled</li>
 * </ul>
 */
public class WrappedClientboundDebugSamplePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DEBUG_SAMPLE;

    /**
     * The type of debug sample data being sent.
     * Matches {@code net.minecraft.util.debugchart.RemoteDebugSampleType}.
     */
    public enum DebugSampleType {
        TICK_TIME
    }

    private static final Class<?> REMOTE_DEBUG_SAMPLE_TYPE_CLASS =
            MinecraftReflection.getMinecraftClass("util.debugchart.RemoteDebugSampleType");

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(long[].class)
            .withParam(REMOTE_DEBUG_SAMPLE_TYPE_CLASS,
                    new EnumWrappers.EnumConverter<>(REMOTE_DEBUG_SAMPLE_TYPE_CLASS, DebugSampleType.class));

    public WrappedClientboundDebugSamplePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDebugSamplePacket(long[] sample, DebugSampleType debugSampleType) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(sample, debugSampleType)));
    }

    public WrappedClientboundDebugSamplePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public long[] getSample() {
        return handle.getSpecificModifier(long[].class).readSafely(0);
    }

    public void setSample(long[] sample) {
        handle.getSpecificModifier(long[].class).writeSafely(0, sample);
    }

    public DebugSampleType getDebugSampleType() {
        return handle.getEnumModifier(DebugSampleType.class, 1).readSafely(0);
    }

    public void setDebugSampleType(DebugSampleType debugSampleType) {
        handle.getEnumModifier(DebugSampleType.class, 1).writeSafely(0, debugSampleType);
    }
}
