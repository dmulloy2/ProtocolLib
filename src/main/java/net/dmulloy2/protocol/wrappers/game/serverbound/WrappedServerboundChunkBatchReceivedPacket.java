package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChunkBatchReceivedPacket} (Play phase, serverbound).
 *
 * <p>Sent by the client to acknowledge a chunk batch and report the desired chunk throughput.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code float desiredChunksPerTick} – how many chunks the client wants to receive per tick</li>
 * </ul>
 */
public class WrappedServerboundChunkBatchReceivedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHUNK_BATCH_RECEIVED;

    public WrappedServerboundChunkBatchReceivedPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundChunkBatchReceivedPacket(float desiredChunksPerTick) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { float.class }).createPacket(desiredChunksPerTick));
    }

    public WrappedServerboundChunkBatchReceivedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public float getDesiredChunksPerTick() {
        return handle.getFloat().read(0);
    }

    public void setDesiredChunksPerTick(float desiredChunksPerTick) {
        handle.getFloat().write(0, desiredChunksPerTick);
    }
}
