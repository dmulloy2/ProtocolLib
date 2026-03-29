package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundMovePlayerPacket$Pos} (Play phase, serverbound).
 *
 * <p>This is the {@code Pos} subpacket sent when the client updates position only
 * (hasPos=true, hasRot=false); rotation is unchanged from the previous packet.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code double x} – absolute X coordinate</li>
 *   <li>{@code double y} – absolute Y coordinate (feet position)</li>
 *   <li>{@code double z} – absolute Z coordinate</li>
 *   <li>{@code boolean onGround} – whether the player is on the ground</li>
 *   <li>{@code boolean horizontalCollision} – whether the player has a horizontal collision</li>
 * </ul>
 */
public class WrappedServerboundMovePlayerPosPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.POSITION;

    public WrappedServerboundMovePlayerPosPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundMovePlayerPosPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the absolute X coordinate.
     */
    public double getX() {
        return handle.getDoubles().read(0);
    }

    /**
     * Sets the absolute X coordinate.
     */
    public void setX(double x) {
        handle.getDoubles().write(0, x);
    }

    /**
     * Returns the absolute Y coordinate (feet position).
     */
    public double getY() {
        return handle.getDoubles().read(1);
    }

    /**
     * Sets the absolute Y coordinate (feet position).
     */
    public void setY(double y) {
        handle.getDoubles().write(1, y);
    }

    /**
     * Returns the absolute Z coordinate.
     */
    public double getZ() {
        return handle.getDoubles().read(2);
    }

    /**
     * Sets the absolute Z coordinate.
     */
    public void setZ(double z) {
        handle.getDoubles().write(2, z);
    }

    /**
     * Returns whether the player is on the ground.
     */
    public boolean isOnGround() {
        return handle.getBooleans().read(0);
    }

    /**
     * Sets whether the player is on the ground.
     */
    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(0, onGround);
    }

    /**
     * Returns whether the player has a horizontal collision.
     */
    public boolean isHorizontalCollision() {
        return handle.getBooleans().read(1);
    }

    /**
     * Sets whether the player has a horizontal collision.
     */
    public void setHorizontalCollision(boolean horizontalCollision) {
        handle.getBooleans().write(1, horizontalCollision);
    }
}
