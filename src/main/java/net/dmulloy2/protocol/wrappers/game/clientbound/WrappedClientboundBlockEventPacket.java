package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.Material;

/**
 * Wrapper for {@code ClientboundBlockEventPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code BlockPosition pos} – position of the block</li>
 *   <li>{@code int actionId} – block action ID (block-type dependent)</li>
 *   <li>{@code int actionParam} – block action parameter (block-type dependent)</li>
 *   <li>{@code Material blockType} – type of block at the position</li>
 * </ul>
 */
public class WrappedClientboundBlockEventPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.BLOCK_ACTION;

    public WrappedClientboundBlockEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundBlockEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public int getActionId() {
        return handle.getIntegers().read(0);
    }

    public void setActionId(int actionId) {
        handle.getIntegers().write(0, actionId);
    }

    public int getActionParam() {
        return handle.getIntegers().read(1);
    }

    public void setActionParam(int actionParam) {
        handle.getIntegers().write(1, actionParam);
    }

    public Material getBlockType() {
        return handle.getBlocks().read(0);
    }

    public void setBlockType(Material blockType) {
        handle.getBlocks().write(0, blockType);
    }
}
