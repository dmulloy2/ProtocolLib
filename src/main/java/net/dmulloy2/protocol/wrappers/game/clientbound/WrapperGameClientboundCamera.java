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
public class WrapperGameClientboundCamera extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CAMERA;

    public WrapperGameClientboundCamera() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundCamera(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getCameraEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setCameraEntityId(int cameraEntityId) {
        handle.getIntegers().write(0, cameraEntityId);
    }
}
