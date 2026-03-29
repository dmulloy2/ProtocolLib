package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSwingPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code InteractionHand hand} – the hand performing the swing animation</li>
 * </ul>
 */
public class WrappedServerboundSwingPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.ARM_ANIMATION;

    public WrappedServerboundSwingPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundSwingPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the hand performing the swing animation.
     */
    public EnumWrappers.Hand getHand() {
        return handle.getHands().read(0);
    }

    /**
     * Sets the hand performing the swing animation.
     */
    public void setHand(EnumWrappers.Hand hand) {
        handle.getHands().write(0, hand);
    }
}
