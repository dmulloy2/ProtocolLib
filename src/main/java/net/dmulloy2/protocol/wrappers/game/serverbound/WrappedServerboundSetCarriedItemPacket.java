package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetCarriedItemPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int slot} – hotbar slot index (0–8) the client selected</li>
 * </ul>
 */
public class WrappedServerboundSetCarriedItemPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.HELD_ITEM_SLOT;

    public WrappedServerboundSetCarriedItemPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundSetCarriedItemPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the hotbar slot index (0–8) the client selected.
     */
    public int getSlot() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the hotbar slot index (0–8) the client selected.
     */
    public void setSlot(int slot) {
        handle.getIntegers().write(0, slot);
    }
}
