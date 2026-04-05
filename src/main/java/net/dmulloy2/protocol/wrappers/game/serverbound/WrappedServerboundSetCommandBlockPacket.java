package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetCommandBlockPacket} (game phase, serverbound).
 *
 * <p>NMS field order: {@code pos, command, trackOutput, conditional, automatic, mode}
 */
public class WrappedServerboundSetCommandBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_COMMAND_BLOCK;

    /**
     * Mirrors {@code CommandBlockEntity.Mode}: constants must match NMS names exactly.
     * Global field index of {@code mode} is 5.
     */
    public enum Mode { SEQUENCE, AUTO, REDSTONE }

    public WrappedServerboundSetCommandBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetCommandBlockPacket(BlockPosition pos, String command,
            boolean trackOutput, boolean conditional, boolean automatic, Mode mode) {
        this();
        setPos(pos);
        setCommand(command);
        setTrackOutput(trackOutput);
        setConditional(conditional);
        setAutomatic(automatic);
        setMode(mode);
    }

    public WrappedServerboundSetCommandBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public String getCommand() {
        return handle.getStrings().read(0);
    }

    public void setCommand(String command) {
        handle.getStrings().write(0, command);
    }

    public boolean isTrackOutput() {
        return handle.getBooleans().read(0);
    }

    public void setTrackOutput(boolean trackOutput) {
        handle.getBooleans().write(0, trackOutput);
    }

    public boolean isConditional() {
        return handle.getBooleans().read(1);
    }

    public void setConditional(boolean conditional) {
        handle.getBooleans().write(1, conditional);
    }

    public boolean isAutomatic() {
        return handle.getBooleans().read(2);
    }

    public void setAutomatic(boolean automatic) {
        handle.getBooleans().write(2, automatic);
    }

    /** Returns the command block mode (global field index 5). */
    public Mode getMode() {
        return handle.getEnumModifier(Mode.class, 5).read(0);
    }

    public void setMode(Mode mode) {
        handle.getEnumModifier(Mode.class, 5).write(0, mode);
    }
}
