package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundLevelEventPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int type} – event type ID</li>
 *   <li>{@code BlockPosition pos} – position of the event</li>
 *   <li>{@code int data} – event-specific data</li>
 *   <li>{@code boolean broadcastToAll} – if {@code true}, the event is broadcast to all nearby players</li>
 * </ul>
 */
public class WrappedClientboundLevelEventPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.WORLD_EVENT;

    public WrappedClientboundLevelEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundLevelEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getType() {
        return handle.getIntegers().read(0);
    }

    public void setType(int type) {
        handle.getIntegers().write(0, type);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    public int getData() {
        return handle.getIntegers().read(1);
    }

    public void setData(int data) {
        handle.getIntegers().write(1, data);
    }

    public boolean isBroadcastToAll() {
        return handle.getBooleans().read(0);
    }

    public void setBroadcastToAll(boolean broadcastToAll) {
        handle.getBooleans().write(0, broadcastToAll);
    }
}
