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
}
