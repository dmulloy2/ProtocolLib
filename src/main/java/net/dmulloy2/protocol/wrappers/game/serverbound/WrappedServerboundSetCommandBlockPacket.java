package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetCommandBlockPacket} (game phase, serverbound).
 */
public class WrappedServerboundSetCommandBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_COMMAND_BLOCK;

    public WrappedServerboundSetCommandBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetCommandBlockPacket(String command, boolean trackOutput, boolean conditional, boolean automatic, BlockPosition pos) {
        this();
        setCommand(command);
        setTrackOutput(trackOutput);
        setConditional(conditional);
        setAutomatic(automatic);
        setPos(pos);
    }

    public WrappedServerboundSetCommandBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
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

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }
}
