package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPlayerActionPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code BlockPos pos} – position of the block being acted upon</li>
 *   <li>{@code Direction direction} – face of the block targeted</li>
 *   <li>{@code Action action} – the dig/drop/release action type</li>
 *   <li>{@code int sequence} – sequence number for client-side prediction acknowledgement</li>
 * </ul>
 */
public class WrappedServerboundPlayerActionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.BLOCK_DIG;

    public WrappedServerboundPlayerActionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPlayerActionPacket(BlockPosition pos, EnumWrappers.Direction direction, EnumWrappers.PlayerDigType action, int sequence) {
        this();
        setPos(pos);
        setDirection(direction);
        setAction(action);
        setSequence(sequence);
    }

    public WrappedServerboundPlayerActionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the position of the block being acted upon.
     */
    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    /**
     * Sets the position of the block being acted upon.
     */
    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    /**
     * Returns the face of the block targeted.
     */
    public EnumWrappers.Direction getDirection() {
        return handle.getDirections().read(0);
    }

    /**
     * Sets the face of the block targeted.
     */
    public void setDirection(EnumWrappers.Direction direction) {
        handle.getDirections().write(0, direction);
    }

    /**
     * Returns the dig/drop/release action type.
     */
    public EnumWrappers.PlayerDigType getAction() {
        return handle.getEnumModifier(EnumWrappers.PlayerDigType.class, EnumWrappers.getPlayerDigTypeClass()).read(0);
    }

    /**
     * Sets the dig/drop/release action type.
     */
    public void setAction(EnumWrappers.PlayerDigType action) {
        handle.getEnumModifier(EnumWrappers.PlayerDigType.class, EnumWrappers.getPlayerDigTypeClass()).write(0, action);
    }

    /**
     * Returns the sequence number for client-side prediction acknowledgement.
     */
    public int getSequence() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the sequence number for client-side prediction acknowledgement.
     */
    public void setSequence(int sequence) {
        handle.getIntegers().write(0, sequence);
    }
}
