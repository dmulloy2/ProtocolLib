package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetCommandMinecartPacket} (game phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity ID of the command minecart</li>
 *   <li>{@code String command} – the command to set (without leading slash)</li>
 *   <li>{@code boolean trackOutput} – whether the minecart should track output</li>
 * </ul>
 */
public class WrappedServerboundSetCommandMinecartPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_COMMAND_MINECART;

    public WrappedServerboundSetCommandMinecartPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetCommandMinecartPacket(int entityId, String command, boolean trackOutput) {
        this();
        setEntityId(entityId);
        setCommand(command);
        setTrackOutput(trackOutput);
    }

    public WrappedServerboundSetCommandMinecartPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
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
}
