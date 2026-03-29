package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Wrapper for {@code ClientboundSetEquipmentPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity whose equipment is changing</li>
 *   <li>{@code List<Pair<ItemSlot, ItemStack>> slots} – equipment slot and item pairs</li>
 * </ul>
 */
public class WrappedClientboundSetEquipmentPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_EQUIPMENT;

    public WrappedClientboundSetEquipmentPacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundSetEquipmentPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public List<Pair<EnumWrappers.ItemSlot, ItemStack>> getSlots() {
        return handle.getSlotStackPairLists().read(0);
    }

    public void setSlots(List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots) {
        handle.getSlotStackPairLists().write(0, slots);
    }
}
