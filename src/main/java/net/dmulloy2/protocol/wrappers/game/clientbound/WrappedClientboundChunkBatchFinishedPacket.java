package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundChunkBatchFinishedPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int batchSize} – number of chunks in the completed batch</li>
 * </ul>
 */
public class WrappedClientboundChunkBatchFinishedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CHUNK_BATCH_FINISHED;

    public WrappedClientboundChunkBatchFinishedPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundChunkBatchFinishedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getBatchSize() {
        return handle.getIntegers().read(0);
    }

    public void setBatchSize(int batchSize) {
        handle.getIntegers().write(0, batchSize);
    }
}
