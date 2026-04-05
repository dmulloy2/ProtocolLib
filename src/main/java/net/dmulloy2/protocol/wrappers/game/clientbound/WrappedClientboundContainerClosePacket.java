package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundContainerClosePacket} (Play phase, clientbound).
 *
 * <p>Instructs the client to close the given container window.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int windowId} – ID of the window to close (0 = player inventory)</li>
 * </ul>
 */
public class WrappedClientboundContainerClosePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CLOSE_WINDOW;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedClientboundContainerClosePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundContainerClosePacket(int windowId) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(windowId)));
    }

    public WrappedClientboundContainerClosePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getWindowId() {
        return handle.getIntegers().read(0);
    }

    public void setWindowId(int windowId) {
        handle.getIntegers().write(0, windowId);
    }
}
