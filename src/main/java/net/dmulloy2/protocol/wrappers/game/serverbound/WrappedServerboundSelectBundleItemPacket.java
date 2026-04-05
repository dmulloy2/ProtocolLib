package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSelectBundleItemPacket} (game phase, serverbound).
 */
public class WrappedServerboundSelectBundleItemPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SELECT_BUNDLE_ITEM;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(int.class);

    public WrappedServerboundSelectBundleItemPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSelectBundleItemPacket(int slotId, int selectedItemIndex) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(slotId, selectedItemIndex)));
    }

    public WrappedServerboundSelectBundleItemPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getSlotId() {
        return handle.getIntegers().read(0);
    }

    public void setSlotId(int slotId) {
        handle.getIntegers().write(0, slotId);
    }

    public int getSelectedItemIndex() {
        return handle.getIntegers().read(1);
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        handle.getIntegers().write(1, selectedItemIndex);
    }
}
