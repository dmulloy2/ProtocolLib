package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDebugBlockValuePacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code BlockPos blockPos} – the block position associated with this debug value</li>
 *   <li>{@code DebugSubscription.Update<?> update} – the debug update data (opaque, no ProtocolLib accessor)</li>
 * </ul>
 */
public class WrappedClientboundDebugBlockValuePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DEBUG_BLOCK_VALUE;

    public WrappedClientboundDebugBlockValuePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDebugBlockValuePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getBlockPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setBlockPos(BlockPosition blockPos) {
        handle.getBlockPositionModifier().write(0, blockPos);
    }
}
