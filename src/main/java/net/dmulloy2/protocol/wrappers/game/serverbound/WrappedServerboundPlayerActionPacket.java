package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPlayerActionPacket} (Play phase, serverbound).
 *
 * <p>NMS constructor order: {@code (Action action, BlockPos pos, Direction direction, int sequence)}.
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

    // NMS constructor order: (Action, BlockPos, Direction, int)
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(EnumWrappers.getPlayerDigTypeClass(), EnumWrappers.getPlayerDiggingActionConverter())
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(EnumWrappers.getDirectionClass(), EnumWrappers.getDirectionConverter())
            .withParam(int.class);

    public WrappedServerboundPlayerActionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPlayerActionPacket(EnumWrappers.PlayerDigType action, BlockPosition pos, EnumWrappers.Direction direction, int sequence) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(action, pos, direction, sequence)));
    }

    public WrappedServerboundPlayerActionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().readSafely(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().writeSafely(0, pos);
    }

    public EnumWrappers.Direction getDirection() {
        return handle.getDirections().readSafely(0);
    }

    public void setDirection(EnumWrappers.Direction direction) {
        handle.getDirections().writeSafely(0, direction);
    }

    public EnumWrappers.PlayerDigType getAction() {
        return handle.getEnumModifier(EnumWrappers.PlayerDigType.class, EnumWrappers.getPlayerDigTypeClass()).readSafely(0);
    }

    public void setAction(EnumWrappers.PlayerDigType action) {
        handle.getEnumModifier(EnumWrappers.PlayerDigType.class, EnumWrappers.getPlayerDigTypeClass()).writeSafely(0, action);
    }

    public int getSequence() {
        return handle.getIntegers().readSafely(0);
    }

    public void setSequence(int sequence) {
        handle.getIntegers().writeSafely(0, sequence);
    }
}
