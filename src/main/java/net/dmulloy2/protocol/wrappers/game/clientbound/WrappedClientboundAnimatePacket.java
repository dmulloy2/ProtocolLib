package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundAnimatePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity ID performing the animation</li>
 *   <li>{@code int animationId} – animation ID:
 *     <ul>
 *       <li>0 = swing main arm</li>
 *       <li>1 = hurt</li>
 *       <li>2 = wake up</li>
 *       <li>3 = swing off hand</li>
 *       <li>4 = critical hit</li>
 *       <li>5 = magic critical hit</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class WrappedClientboundAnimatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ANIMATION;

    public WrappedClientboundAnimatePacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundAnimatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public int getAnimationId() {
        return handle.getIntegers().read(1);
    }

    public void setAnimationId(int animationId) {
        handle.getIntegers().write(1, animationId);
    }
}
