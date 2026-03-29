package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundContainerClosePacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int containerId} – ID of the container the client is closing</li>
 * </ul>
 */
public class WrappedServerboundContainerClosePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CLOSE_WINDOW;

    public WrappedServerboundContainerClosePacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundContainerClosePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the ID of the container being closed.
     */
    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the ID of the container being closed.
     */
    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }
}
