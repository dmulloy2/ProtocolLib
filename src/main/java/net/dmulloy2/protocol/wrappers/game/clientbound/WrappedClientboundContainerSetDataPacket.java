package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundContainerSetDataPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int windowId} – container window ID</li>
 *   <li>{@code int property} – property index (container-type dependent)</li>
 *   <li>{@code int value} – new value of the property</li>
 * </ul>
 */
public class WrappedClientboundContainerSetDataPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.WINDOW_DATA;

    public WrappedClientboundContainerSetDataPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundContainerSetDataPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getWindowId() {
        return handle.getIntegers().read(0);
    }

    public void setWindowId(int windowId) {
        handle.getIntegers().write(0, windowId);
    }

    public int getProperty() {
        return handle.getIntegers().read(1);
    }

    public void setProperty(int property) {
        handle.getIntegers().write(1, property);
    }

    public int getValue() {
        return handle.getIntegers().read(2);
    }

    public void setValue(int value) {
        handle.getIntegers().write(2, value);
    }
}
