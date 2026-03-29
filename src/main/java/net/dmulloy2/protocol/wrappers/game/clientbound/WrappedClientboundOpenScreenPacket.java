package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundOpenScreenPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int containerId} – ID assigned to this container session</li>
 *   <li>{@code MenuType<?> type} – inventory menu type (not exposed; use the raw modifier)</li>
 *   <li>{@code Component title} – display title shown in the inventory screen</li>
 * </ul>
 */
public class WrappedClientboundOpenScreenPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.OPEN_WINDOW;

    public WrappedClientboundOpenScreenPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundOpenScreenPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }

    public WrappedChatComponent getTitle() {
        return handle.getChatComponents().read(0);
    }

    public void setTitle(WrappedChatComponent title) {
        handle.getChatComponents().write(0, title);
    }
}
