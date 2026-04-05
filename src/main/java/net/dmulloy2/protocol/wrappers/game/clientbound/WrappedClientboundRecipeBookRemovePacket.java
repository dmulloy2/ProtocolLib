package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundRecipeBookRemovePacket} (game phase, clientbound).
 * Removes recipes from the client recipe book. Recipes field has no ProtocolLib accessor.
 */
public class WrappedClientboundRecipeBookRemovePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.RECIPE_BOOK_REMOVE;

    public WrappedClientboundRecipeBookRemovePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRecipeBookRemovePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'recipes' (NMS type: List<RecipeDisplayId> — list of integer-backed recipe IDs)
    //   RecipeDisplayId wraps an int; use getLists(Converters.passthrough(Integer.class)).read(0)
    //   or getSpecificModifier(List.class).read(0) to access the raw list.
}
