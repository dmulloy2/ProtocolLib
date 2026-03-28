package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerRotationPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code float yaw} – yaw angle in degrees</li>
 *   <li>{@code float pitch} – pitch angle in degrees</li>
 * </ul>
 */
public class WrapperGameClientboundPlayerRotation extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_ROTATION;

    public WrapperGameClientboundPlayerRotation() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundPlayerRotation(PacketContainer packet) {
        super(packet, TYPE);
    }

    public float getYaw() {
        return handle.getFloat().read(0);
    }

    public void setYaw(float yaw) {
        handle.getFloat().write(0, yaw);
    }

    public float getPitch() {
        return handle.getFloat().read(1);
    }

    public void setPitch(float pitch) {
        handle.getFloat().write(1, pitch);
    }
}
