package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundTakeItemEntityPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int collectedEntityId} – entity ID of the item that was collected</li>
 *   <li>{@code int collectorEntityId} – entity ID of the collector (player or hopper minecart)</li>
 *   <li>{@code int pickupItemCount} – number of items picked up</li>
 * </ul>
 */
public class WrappedClientboundTakeItemEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.COLLECT;

    public WrappedClientboundTakeItemEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundTakeItemEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getCollectedEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setCollectedEntityId(int collectedEntityId) {
        handle.getIntegers().write(0, collectedEntityId);
    }

    public int getCollectorEntityId() {
        return handle.getIntegers().read(1);
    }

    public void setCollectorEntityId(int collectorEntityId) {
        handle.getIntegers().write(1, collectorEntityId);
    }

    public int getPickupItemCount() {
        return handle.getIntegers().read(2);
    }

    public void setPickupItemCount(int pickupItemCount) {
        handle.getIntegers().write(2, pickupItemCount);
    }
}
