package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundRecipeBookAddPacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code List<Entry> entries} – recipe display entries (opaque, no ProtocolLib accessor)</li>
 *   <li>{@code boolean replace} – if true, replaces all known recipes; otherwise appends</li>
 * </ul>
 */
public class WrappedClientboundRecipeBookAddPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.RECIPE_BOOK_ADD;

    public WrappedClientboundRecipeBookAddPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRecipeBookAddPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isReplace() {
        return handle.getBooleans().read(0);
    }

    public void setReplace(boolean replace) {
        handle.getBooleans().write(0, replace);
    }
}
