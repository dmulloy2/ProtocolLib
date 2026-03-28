package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetHeldSlotPacket} (Play phase, clientbound).
 *
 * <p>Tells the client which hotbar slot to select (0–8).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int slot} – hotbar slot index (0–8)</li>
 * </ul>
 */
public class WrapperGameClientboundHeldItemSlot extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.HELD_ITEM_SLOT;

    public WrapperGameClientboundHeldItemSlot() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundHeldItemSlot(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the selected hotbar slot (0–8). */
    public int getSlot() {
        return handle.getIntegers().read(0);
    }

    public void setSlot(int slot) {
        handle.getIntegers().write(0, slot);
    }
}
