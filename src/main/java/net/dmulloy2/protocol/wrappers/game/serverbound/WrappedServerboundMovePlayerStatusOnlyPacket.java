package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundMovePlayerPacket$StatusOnly} (game phase, serverbound).
 *
 * <p>This is the {@code StatusOnly} subpacket sent when the client updates only
 * the on-ground and horizontal-collision flags (hasPos=false, hasRot=false).
 */
public class WrappedServerboundMovePlayerStatusOnlyPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.GROUND;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(boolean.class)
            .withParam(boolean.class);

    public WrappedServerboundMovePlayerStatusOnlyPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundMovePlayerStatusOnlyPacket(boolean onGround, boolean horizontalCollision) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(onGround, horizontalCollision)));
    }

    public WrappedServerboundMovePlayerStatusOnlyPacket(PacketContainer packet) {
        super(packet, TYPE);
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
