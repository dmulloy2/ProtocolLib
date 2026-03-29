package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPaddleBoatPacket} (Play phase, serverbound).
 *
 * <p>Sent each tick while the client is riding a boat, reporting which paddles are being pressed.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code boolean left} – whether the left paddle is being pressed</li>
 *   <li>{@code boolean right} – whether the right paddle is being pressed</li>
 * </ul>
 */
public class WrappedServerboundPaddleBoatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.BOAT_MOVE;

    public WrappedServerboundPaddleBoatPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundPaddleBoatPacket(boolean left, boolean right) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { boolean.class, boolean.class }).createPacket(left, right));
    }

    public WrappedServerboundPaddleBoatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isLeft() {
        return handle.getBooleans().read(0);
    }

    public void setLeft(boolean left) {
        handle.getBooleans().write(0, left);
    }

    public boolean isRight() {
        return handle.getBooleans().read(1);
    }

    public void setRight(boolean right) {
        handle.getBooleans().write(1, right);
    }
}
