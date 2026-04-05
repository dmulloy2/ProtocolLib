package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundMovePlayerPacket$Rot} (game phase, serverbound).
 *
 * <p>This is the {@code Rot} subpacket sent when the client updates rotation only
 * (hasPos=false, hasRot=true); position is unchanged from the previous packet.
 */
public class WrappedServerboundMovePlayerRotPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.LOOK;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(float.class)
            .withParam(float.class)
            .withParam(boolean.class)
            .withParam(boolean.class);

    public WrappedServerboundMovePlayerRotPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundMovePlayerRotPacket(float yRot, float xRot, boolean onGround, boolean horizontalCollision) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(yRot, xRot, onGround, horizontalCollision)));
    }

    public WrappedServerboundMovePlayerRotPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public float getYRot() {
        return handle.getFloat().read(0);
    }

    public void setYRot(float yRot) {
        handle.getFloat().write(0, yRot);
    }

    public float getXRot() {
        return handle.getFloat().read(1);
    }

    public void setXRot(float xRot) {
        handle.getFloat().write(1, xRot);
    }

    public boolean isOnGround() {
        return handle.getBooleans().read(0);
    }

    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(0, onGround);
    }

    public boolean isHorizontalCollision() {
        return handle.getBooleans().read(1);
    }

    public void setHorizontalCollision(boolean horizontalCollision) {
        handle.getBooleans().write(1, horizontalCollision);
    }
}
