package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundChunkBatchStartPacket} (Play phase, clientbound).
 *
 * <p>This is an empty packet with no fields. It signals the start of a chunk batch.
 */
public class WrappedClientboundChunkBatchStartPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CHUNK_BATCH_START;

    public WrappedClientboundChunkBatchStartPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundChunkBatchStartPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
