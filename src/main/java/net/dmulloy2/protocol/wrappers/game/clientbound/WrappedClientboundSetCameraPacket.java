package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetCameraPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int cameraEntityId} – entity ID whose perspective the player views</li>
 * </ul>
 */
public class WrappedClientboundSetCameraPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CAMERA;

    public WrappedClientboundSetCameraPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundSetCameraPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getCameraEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setCameraEntityId(int cameraEntityId) {
        handle.getIntegers().write(0, cameraEntityId);
    }
}
