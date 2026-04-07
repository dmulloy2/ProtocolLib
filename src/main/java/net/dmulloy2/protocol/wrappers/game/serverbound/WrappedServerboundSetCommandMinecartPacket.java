package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
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

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(String.class)
            .withParam(boolean.class);

    public WrappedServerboundSetCommandMinecartPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetCommandMinecartPacket(int entityId, String command, boolean trackOutput) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(entityId, command, trackOutput)));
    }

    public WrappedServerboundSetCommandMinecartPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().writeSafely(0, entityId);
    }

    public String getCommand() {
        return handle.getStrings().readSafely(0);
    }

    public void setCommand(String command) {
        handle.getStrings().writeSafely(0, command);
    }

    public boolean isTrackOutput() {
        return handle.getBooleans().readSafely(0);
    }

    public void setTrackOutput(boolean trackOutput) {
        handle.getBooleans().writeSafely(0, trackOutput);
    }
}
