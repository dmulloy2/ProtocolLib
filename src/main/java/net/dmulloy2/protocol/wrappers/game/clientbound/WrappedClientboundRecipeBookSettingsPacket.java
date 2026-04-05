package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundRecipeBookSettingsPacket} (game phase, clientbound).
 * Sends recipe book settings. The settings field has no ProtocolLib accessor.
 */
public class WrappedClientboundRecipeBookSettingsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.RECIPE_BOOK_SETTINGS;

    public WrappedClientboundRecipeBookSettingsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRecipeBookSettingsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'bookSettings' (NMS type: RecipeBookSettings — holds open/filter booleans per recipe book type)
    //   No ProtocolLib accessor exists. RecipeBookSettings serialises as 2 booleans per RecipeBookType (4 types × 2 = 8 booleans).
    //   Use AutoWrapper or a dedicated WrappedRecipeBookSettings class to expose the individual open/filter flags.
}
