package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Wrapper for {@code ClientboundContainerSetContentPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int containerId} – ID of the container whose contents are being set</li>
 *   <li>{@code int stateId} – state ID used for sync with the server</li>
 *   <li>{@code List<ItemStack> items} – full slot contents</li>
 *   <li>{@code ItemStack carriedItem} – item currently held on the cursor</li>
 * </ul>
 */
public class WrappedClientboundContainerSetContentPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.WINDOW_ITEMS;

    public WrappedClientboundContainerSetContentPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundContainerSetContentPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }

    public int getStateId() {
        return handle.getIntegers().read(1);
    }

    public void setStateId(int stateId) {
        handle.getIntegers().write(1, stateId);
    }

    public List<ItemStack> getItems() {
        return handle.getItemListModifier().read(0);
    }

    public void setItems(List<ItemStack> items) {
        handle.getItemListModifier().write(0, items);
    }

    public ItemStack getCarriedItem() {
        return handle.getItemModifier().read(0);
    }

    public void setCarriedItem(ItemStack carriedItem) {
        handle.getItemModifier().write(0, carriedItem);
    }
}
