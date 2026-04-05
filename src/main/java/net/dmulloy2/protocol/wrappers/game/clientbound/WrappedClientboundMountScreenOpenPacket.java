package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundMountScreenOpenPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int windowId} – window ID for the horse inventory</li>
 *   <li>{@code int containerSize} – number of slots in the horse container</li>
 *   <li>{@code int entityId} – entity ID of the horse</li>
 * </ul>
 */
public class WrappedClientboundMountScreenOpenPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.OPEN_WINDOW_HORSE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(int.class)
            .withParam(int.class);

    public WrappedClientboundMountScreenOpenPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundMountScreenOpenPacket(int windowId, int containerSize, int entityId) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(windowId, containerSize, entityId)));
    }

    public WrappedClientboundMountScreenOpenPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getWindowId() {
        return handle.getIntegers().read(0);
    }

    public void setWindowId(int windowId) {
        handle.getIntegers().write(0, windowId);
    }

    public int getContainerSize() {
        return handle.getIntegers().read(1);
    }

    public void setContainerSize(int containerSize) {
        handle.getIntegers().write(1, containerSize);
    }

    public int getEntityId() {
        return handle.getIntegers().read(2);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(2, entityId);
    }
}
