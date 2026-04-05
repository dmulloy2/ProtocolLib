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
}
