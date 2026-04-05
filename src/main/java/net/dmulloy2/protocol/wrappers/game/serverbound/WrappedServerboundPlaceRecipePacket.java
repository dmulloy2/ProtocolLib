package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPlaceRecipePacket} (game phase, serverbound).
 */
public class WrappedServerboundPlaceRecipePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.AUTO_RECIPE;

    public WrappedServerboundPlaceRecipePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPlaceRecipePacket(int containerId, boolean useMaxItems) {
        this();
        setContainerId(containerId);
        setUseMaxItems(useMaxItems);
    }

    public WrappedServerboundPlaceRecipePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }

    public boolean isUseMaxItems() {
        return handle.getBooleans().read(0);
    }

    public void setUseMaxItems(boolean useMaxItems) {
        handle.getBooleans().write(0, useMaxItems);
    }
}
