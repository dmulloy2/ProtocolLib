package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.util.Vector;

/**
 * Wrapper for {@code ServerboundMoveVehiclePacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Vec3 position} – the new vehicle position</li>
 *   <li>{@code float yRot} – yaw rotation of the vehicle</li>
 *   <li>{@code float xRot} – pitch rotation of the vehicle</li>
 *   <li>{@code boolean onGround} – whether the vehicle is on the ground</li>
 * </ul>
 */
public class WrappedServerboundMoveVehiclePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.VEHICLE_MOVE;

    public WrappedServerboundMoveVehiclePacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedServerboundMoveVehiclePacket(Vector position, float yRot, float xRot, boolean onGround) {
        this();
        setPosition(position);
        setYRot(yRot);
        setXRot(xRot);
        setOnGround(onGround);
    }

    public WrappedServerboundMoveVehiclePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the new vehicle position.
     */
    public Vector getPosition() {
        return handle.getVectors().read(0);
    }

    /**
     * Sets the new vehicle position.
     */
    public void setPosition(Vector position) {
        handle.getVectors().write(0, position);
    }

    /**
     * Returns the yaw rotation of the vehicle.
     */
    public float getYRot() {
        return handle.getFloat().read(0);
    }

    /**
     * Sets the yaw rotation of the vehicle.
     */
    public void setYRot(float yRot) {
        handle.getFloat().write(0, yRot);
    }

    /**
     * Returns the pitch rotation of the vehicle.
     */
    public float getXRot() {
        return handle.getFloat().read(1);
    }

    /**
     * Sets the pitch rotation of the vehicle.
     */
    public void setXRot(float xRot) {
        handle.getFloat().write(1, xRot);
    }

    /**
     * Returns whether the vehicle is on the ground.
     */
    public boolean isOnGround() {
        return handle.getBooleans().read(0);
    }

    /**
     * Sets whether the vehicle is on the ground.
     */
    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(0, onGround);
    }
}
