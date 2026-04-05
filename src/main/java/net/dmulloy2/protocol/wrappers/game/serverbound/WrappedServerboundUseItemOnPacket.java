package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundUseItemOnPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code InteractionHand hand} – the hand used to interact</li>
 *   <li>{@code BlockHitResult blockHit} – the targeted block face and hit location</li>
 *   <li>{@code int sequence} – sequence number for client-side prediction acknowledgement</li>
 * </ul>
 */
public class WrappedServerboundUseItemOnPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.USE_ITEM_ON;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(EnumWrappers.getHandClass(), EnumWrappers.getHandConverter())
            .withParam(MovingObjectPositionBlock.getNmsClass(), MovingObjectPositionBlock.getConverter())
            .withParam(int.class);

    public WrappedServerboundUseItemOnPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundUseItemOnPacket(EnumWrappers.Hand hand, MovingObjectPositionBlock blockHit, int sequence) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(hand, blockHit, sequence)));
    }

    public WrappedServerboundUseItemOnPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the hand used to interact.
     */
    public EnumWrappers.Hand getHand() {
        return handle.getHands().read(0);
    }

    /**
     * Sets the hand used to interact.
     */
    public void setHand(EnumWrappers.Hand hand) {
        handle.getHands().write(0, hand);
    }

    /**
     * Returns the targeted block face and hit location.
     */
    public MovingObjectPositionBlock getBlockHit() {
        return handle.getMovingBlockPositions().read(0);
    }

    /**
     * Sets the targeted block face and hit location.
     */
    public void setBlockHit(MovingObjectPositionBlock blockHit) {
        handle.getMovingBlockPositions().write(0, blockHit);
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
}
