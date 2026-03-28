package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper for {@code ClientboundContainerSetSlotPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int windowId} – container window ID; 0 = player inventory, -1 = cursor slot,
 *       -2 = all slots</li>
 *   <li>{@code int stateId} – container state ID for synchronisation</li>
 *   <li>{@code int slot} – slot index within the container</li>
 *   <li>{@code ItemStack item} – item to place in the slot</li>
 * </ul>
 */
public class WrapperGameClientboundSetSlot extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_SLOT;

    public WrapperGameClientboundSetSlot() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundSetSlot(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getWindowId() {
        return handle.getIntegers().read(0);
    }

    public void setWindowId(int windowId) {
        handle.getIntegers().write(0, windowId);
    }

    public int getStateId() {
        return handle.getIntegers().read(1);
    }

    public void setStateId(int stateId) {
        handle.getIntegers().write(1, stateId);
    }

    public int getSlot() {
        return handle.getIntegers().read(2);
    }

    public void setSlot(int slot) {
        handle.getIntegers().write(2, slot);
    }

    public ItemStack getItem() {
        return handle.getItemModifier().read(0);
    }

    public void setItem(ItemStack item) {
        handle.getItemModifier().write(0, item);
    }
}
