package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundUseItemPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code InteractionHand hand} – the hand used to activate the item</li>
 *   <li>{@code int sequence} – sequence number for client-side prediction acknowledgement</li>
 *   <li>{@code float yRot} – yaw of the player's look direction at time of use</li>
 *   <li>{@code float xRot} – pitch of the player's look direction at time of use</li>
 * </ul>
 */
public class WrappedServerboundUseItemPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.USE_ITEM;

    public WrappedServerboundUseItemPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedServerboundUseItemPacket(EnumWrappers.Hand hand, int sequence, float yRot, float xRot) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { EnumWrappers.getHandClass(), int.class, float.class, float.class }).createPacket(EnumWrappers.getHandConverter().getGeneric(hand), sequence, yRot, xRot));
    }

    public WrappedServerboundUseItemPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the hand used to activate the item.
     */
    public EnumWrappers.Hand getHand() {
        return handle.getHands().read(0);
    }

    /**
     * Sets the hand used to activate the item.
     */
    public void setHand(EnumWrappers.Hand hand) {
        handle.getHands().write(0, hand);
    }

    /**
     * Returns the sequence number for client-side prediction acknowledgement.
     */
    public int getSequence() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the sequence number for client-side prediction acknowledgement.
     */
    public void setSequence(int sequence) {
        handle.getIntegers().write(0, sequence);
    }

    /**
     * Returns the yaw of the player's look direction at time of use.
     */
    public float getYRot() {
        return handle.getFloat().read(0);
    }

    /**
     * Sets the yaw of the player's look direction at time of use.
     */
    public void setYRot(float yRot) {
        handle.getFloat().write(0, yRot);
    }

    /**
     * Returns the pitch of the player's look direction at time of use.
     */
    public float getXRot() {
        return handle.getFloat().read(1);
    }

    /**
     * Sets the pitch of the player's look direction at time of use.
     */
    public void setXRot(float xRot) {
        handle.getFloat().write(1, xRot);
    }
}
