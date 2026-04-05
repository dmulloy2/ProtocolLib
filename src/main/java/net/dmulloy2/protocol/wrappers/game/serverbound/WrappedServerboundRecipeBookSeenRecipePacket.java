package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundRecipeBookSeenRecipePacket} (game phase, serverbound).
 */
public class WrappedServerboundRecipeBookSeenRecipePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.RECIPE_DISPLAYED;

    public WrappedServerboundRecipeBookSeenRecipePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundRecipeBookSeenRecipePacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
