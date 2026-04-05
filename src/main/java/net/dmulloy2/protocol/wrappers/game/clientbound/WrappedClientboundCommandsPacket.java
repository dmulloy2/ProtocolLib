package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCommandsPacket} (game phase, clientbound).
 */
public class WrappedClientboundCommandsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.COMMANDS;

    public WrappedClientboundCommandsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCommandsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field — Brigadier command tree (RootCommandNode<SharedSuggestionProvider>)
    //   No ProtocolLib accessor exists for the serialised command tree.
    //   Use handle.getModifier().read(0) to access the raw RootCommandNode object.
}
