package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetGameRulePacket} (game phase, serverbound).
 * Sets game rules. The entries field has no ProtocolLib accessor.
 */
public class WrappedServerboundSetGameRulePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_GAME_RULE;

    public WrappedServerboundSetGameRulePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetGameRulePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'entries' (NMS type: List<ServerboundSetGameRulePacket.Entry> where Entry = (ResourceKey<GameRule<?>>, String))
    //   No ProtocolLib accessor exists for this list of game-rule key/value pairs.
    //   Use handle.getModifier().read(0) to access the raw List<Entry>, or add a dedicated wrapper.
}
