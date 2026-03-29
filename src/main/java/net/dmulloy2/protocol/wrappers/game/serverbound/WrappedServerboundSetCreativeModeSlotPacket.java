package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper for {@code ServerboundSetCreativeModeSlotPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code short slotNum} – inventory slot index being set</li>
 *   <li>{@code ItemStack itemStack} – the item placed into the slot</li>
 * </ul>
 */
public class WrappedServerboundSetCreativeModeSlotPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_CREATIVE_SLOT;

    public WrappedServerboundSetCreativeModeSlotPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedServerboundSetCreativeModeSlotPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the inventory slot index being set.
     */
    public short getSlotNum() {
        return handle.getShorts().read(0);
    }

    /**
     * Sets the inventory slot index being set.
     */
    public void setSlotNum(short slotNum) {
        handle.getShorts().write(0, slotNum);
    }

    /**
     * Returns the item placed into the slot.
     */
    public ItemStack getItemStack() {
        return handle.getItemModifier().read(0);
    }

    /**
     * Sets the item placed into the slot.
     */
    public void setItemStack(ItemStack itemStack) {
        handle.getItemModifier().write(0, itemStack);
    }
}
