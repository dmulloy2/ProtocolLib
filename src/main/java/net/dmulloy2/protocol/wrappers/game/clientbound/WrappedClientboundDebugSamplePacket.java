package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
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

    public WrappedClientboundDebugSamplePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDebugSamplePacket(long[] sample, DebugSampleType debugSampleType) {
        this();
        setSample(sample);
        setDebugSampleType(debugSampleType);
    }

    public WrappedClientboundDebugSamplePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the array of debug timing samples.
     */
    public long[] getSample() {
        return handle.getSpecificModifier(long[].class).read(0);
    }

    /**
     * Sets the array of debug timing samples.
     */
    public void setSample(long[] sample) {
        handle.getSpecificModifier(long[].class).write(0, sample);
    }

    /**
     * Returns the debug sample type (field at global index 1 in the NMS packet).
     */
    public DebugSampleType getDebugSampleType() {
        return handle.getEnumModifier(DebugSampleType.class, 1).read(0);
    }

    /**
     * Sets the debug sample type.
     */
    public void setDebugSampleType(DebugSampleType debugSampleType) {
        handle.getEnumModifier(DebugSampleType.class, 1).write(0, debugSampleType);
    }
}
