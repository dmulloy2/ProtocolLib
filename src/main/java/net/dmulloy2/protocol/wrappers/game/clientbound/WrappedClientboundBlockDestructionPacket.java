package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundBlockDestructionPacket} (Play phase, clientbound).
 *
 * <p>Controls the cracking/break-progress animation overlay on a block.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – unique ID for this break action (used to track multiple simultaneous breakers)</li>
 *   <li>{@code BlockPos pos} – position of the block being broken</li>
 *   <li>{@code int destroyStage} – break progress stage (0–9; 10+ removes the overlay)</li>
 * </ul>
 */
public class WrappedClientboundBlockDestructionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.BLOCK_BREAK_ANIMATION;

    public WrappedClientboundBlockDestructionPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundBlockDestructionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the unique action ID used to differentiate concurrent break animations. */
    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    /** Returns the destroy stage (0–9). Values ≥ 10 remove the animation overlay. */
    public int getDestroyStage() {
        return handle.getIntegers().read(1);
    }

    public void setDestroyStage(int destroyStage) {
        handle.getIntegers().write(1, destroyStage);
    }
}
