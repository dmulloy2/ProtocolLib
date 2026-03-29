package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundBlockUpdatePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code BlockPosition pos} – position of the changed block</li>
 *   <li>{@code WrappedBlockData blockData} – new block state</li>
 * </ul>
 */
public class WrappedClientboundBlockUpdatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.BLOCK_CHANGE;

    public WrappedClientboundBlockUpdatePacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundBlockUpdatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public WrappedBlockData getBlockData() {
        return handle.getBlockData().read(0);
    }

    public void setBlockData(WrappedBlockData blockData) {
        handle.getBlockData().write(0, blockData);
    }
}
