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

    // TODO: missing field 'recipeDisplay' (NMS type: RecipeDisplay — sealed interface with multiple subtypes)
    //   RecipeDisplay covers crafting table, furnace, stonecutter, smithing-transform, and smithing-trim layouts.
    //   No ProtocolLib accessor exists; use handle.getModifier().read(1) for the raw RecipeDisplay object.
}
