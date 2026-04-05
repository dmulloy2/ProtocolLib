package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetJigsawBlockPacket} (game phase, serverbound).
 */
public class WrappedServerboundSetJigsawBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_JIGSAW;

    public WrappedServerboundSetJigsawBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetJigsawBlockPacket(String finalState, int selectionPriority, int placementPriority, BlockPosition pos) {
        this();
        setFinalState(finalState);
        setSelectionPriority(selectionPriority);
        setPlacementPriority(placementPriority);
        setPos(pos);
    }

    public WrappedServerboundSetJigsawBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getFinalState() {
        return handle.getStrings().read(0);
    }

    public void setFinalState(String finalState) {
        handle.getStrings().write(0, finalState);
    }

    public int getSelectionPriority() {
        return handle.getIntegers().read(0);
    }

    public void setSelectionPriority(int selectionPriority) {
        handle.getIntegers().write(0, selectionPriority);
    }

    public int getPlacementPriority() {
        return handle.getIntegers().read(1);
    }

    public void setPlacementPriority(int placementPriority) {
        handle.getIntegers().write(1, placementPriority);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }
}
