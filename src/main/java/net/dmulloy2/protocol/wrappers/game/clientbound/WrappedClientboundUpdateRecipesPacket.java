package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundUpdateRecipesPacket} (game phase, clientbound).
 */
public class WrappedClientboundUpdateRecipesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.RECIPE_UPDATE;

    public WrappedClientboundUpdateRecipesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundUpdateRecipesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'itemSets' (NMS type: Map<ResourceKey<RecipePropertySet>, RecipePropertySet>)
    // TODO: missing field 'stonecutterRecipes' (NMS type: SelectableRecipe.SingleInputSet<StonecutterRecipe>)
    //   No ProtocolLib accessors exist for these recipe structures.
    //   Use handle.getModifier().read(0) / read(1) to access the raw objects.
}
