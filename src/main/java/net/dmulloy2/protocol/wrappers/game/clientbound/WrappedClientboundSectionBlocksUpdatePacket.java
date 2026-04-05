package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSectionBlocksUpdatePacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code SectionPos sectionPos} – chunk section containing the changed blocks</li>
 *   <li>{@code short[] positions} – packed relative positions within the section</li>
 *   <li>{@code BlockState[] states} – new block states for each position</li>
 * </ul>
 *
 * <p>Note: The NMS constructor takes {@code (SectionPos, ShortSet, BlockState[])}; however,
 * fields are stored as {@code short[] positions} and {@code BlockState[] states}, which are
 * accessible via the standard ProtocolLib modifiers after packet construction via {@code this()}.
 */
public class WrappedClientboundSectionBlocksUpdatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.MULTI_BLOCK_CHANGE;

    public WrappedClientboundSectionBlocksUpdatePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSectionBlocksUpdatePacket(BlockPosition sectionPos, short[] positions, WrappedBlockData[] states) {
        this();
        setSectionPos(sectionPos);
        setPositions(positions);
        setStates(states);
    }

    public WrappedClientboundSectionBlocksUpdatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the section position (chunk section origin). */
    public BlockPosition getSectionPos() {
        return handle.getSectionPositions().read(0);
    }

    public void setSectionPos(BlockPosition sectionPos) {
        handle.getSectionPositions().write(0, sectionPos);
    }

    /** Returns the packed relative positions within the section. */
    public short[] getPositions() {
        return handle.getShortArrays().read(0);
    }

    public void setPositions(short[] positions) {
        handle.getShortArrays().write(0, positions);
    }

    /** Returns the new block states corresponding to each position. */
    public WrappedBlockData[] getStates() {
        return handle.getBlockDataArrays().readSafely(0);
    }

    public void setStates(WrappedBlockData[] states) {
        handle.getBlockDataArrays().writeSafely(0, states);
    }
}
