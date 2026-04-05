package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
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
public class WrappedClientboundPlayerRotationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_ROTATION;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(float.class)
            .withParam(boolean.class)
            .withParam(float.class)
            .withParam(boolean.class);

    public WrappedClientboundPlayerRotationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPlayerRotationPacket(float yaw, boolean relativeY, float pitch, boolean relativeX) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(yaw, relativeY, pitch, relativeX)));
    }

    public WrappedClientboundPlayerRotationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public float getYaw() {
        return handle.getFloat().read(0);
    }

    public void setYaw(float yaw) {
        handle.getFloat().write(0, yaw);
    }

    public boolean isRelativeY() {
        return handle.getBooleans().read(0);
    }

    public void setRelativeY(boolean relativeY) {
        handle.getBooleans().write(0, relativeY);
    }

    public float getPitch() {
        return handle.getFloat().read(1);
    }

    public void setPitch(float pitch) {
        handle.getFloat().write(1, pitch);
    }

    public boolean isRelativeX() {
        return handle.getBooleans().read(1);
    }

    public void setRelativeX(boolean relativeX) {
        handle.getBooleans().write(1, relativeX);
    }
}
