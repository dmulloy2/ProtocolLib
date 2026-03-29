package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.util.Vector;

/**
 * Wrapper for {@code ServerboundInteractPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – ID of the entity being interacted with</li>
 *   <li>{@code InteractionHand hand} – the hand used for the interaction</li>
 *   <li>{@code Vec3 location} – hit location relative to the entity (interact-at only)</li>
 *   <li>{@code boolean usingSecondaryAction} – whether the player is sneaking</li>
 * </ul>
 */
public class WrappedServerboundInteractPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.USE_ENTITY;

    public WrappedServerboundInteractPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundInteractPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the ID of the entity being interacted with.
     */
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the ID of the entity being interacted with.
     */
    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    /**
     * Returns whether the player is sneaking (using secondary action).
     */
    public boolean isUsingSecondaryAction() {
        return handle.getBooleans().read(0);
    }

    /**
     * Sets whether the player is sneaking (using secondary action).
     */
    public void setUsingSecondaryAction(boolean usingSecondaryAction) {
        handle.getBooleans().write(0, usingSecondaryAction);
    }

    /**
     * Returns the hand used for the interaction.
     */
    public EnumWrappers.Hand getHand() {
        return handle.getHands().read(0);
    }

    /**
     * Sets the hand used for the interaction.
     */
    public void setHand(EnumWrappers.Hand hand) {
        handle.getHands().write(0, hand);
    }

    /**
     * Returns the hit location relative to the entity (interact-at only).
     */
    public Vector getLocation() {
        return handle.getVectors().read(0);
    }

    /**
     * Sets the hit location relative to the entity (interact-at only).
     */
    public void setLocation(Vector location) {
        handle.getVectors().write(0, location);
    }
}
