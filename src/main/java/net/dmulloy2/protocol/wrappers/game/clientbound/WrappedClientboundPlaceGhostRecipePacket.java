package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlaceGhostRecipePacket} (game phase, clientbound).
 */
public class WrappedClientboundPlaceGhostRecipePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.AUTO_RECIPE;

    public WrappedClientboundPlaceGhostRecipePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPlaceGhostRecipePacket(int containerId) {
        this();
        setContainerId(containerId);
    }

    public WrappedClientboundPlaceGhostRecipePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }
}
